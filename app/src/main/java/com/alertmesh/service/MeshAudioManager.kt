package com.alertmesh.service

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class MeshAudioManager {
    private val sampleRate = 8000
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSizeIn = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)
    private val bufferSizeOut = AudioTrack.getMinBufferSize(sampleRate, channelConfigOut, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val recordingScope = CoroutineScope(Dispatchers.IO)
    private val outputStream = ByteArrayOutputStream()

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording) return
        isRecording = true
        outputStream.reset()

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfigIn,
            audioFormat,
            bufferSizeIn
        )

        audioRecord?.startRecording()

        recordingScope.launch {
            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AMR_NB)
            val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AMR_NB, sampleRate, 1).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, 12200)
            }
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            val buffer = ByteArray(bufferSizeIn)
            val info = MediaCodec.BufferInfo()

            while (isRecording) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    val inputBufferIndex = codec.dequeueInputBuffer(10000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                        inputBuffer?.clear()
                        inputBuffer?.put(buffer, 0, readSize)
                        codec.queueInputBuffer(inputBufferIndex, 0, readSize, System.nanoTime() / 1000, 0)
                    }

                    var outputBufferIndex = codec.dequeueOutputBuffer(info, 10000)
                    while (outputBufferIndex >= 0) {
                        val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                        val outData = ByteArray(info.size)
                        outputBuffer?.get(outData)
                        outputStream.write(outData)
                        codec.releaseOutputBuffer(outputBufferIndex, false)
                        outputBufferIndex = codec.dequeueOutputBuffer(info, 10000)
                    }
                }
            }
            codec.stop()
            codec.release()
        }
    }

    fun stopRecording(): ByteArray {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        return outputStream.toByteArray()
    }

    suspend fun playAudio(data: ByteArray) = withContext(Dispatchers.IO) {
        val audioTrack = AudioTrack(
            AudioManager.STREAM_VOICE_CALL,
            sampleRate,
            channelConfigOut,
            audioFormat,
            bufferSizeOut,
            AudioTrack.MODE_STREAM
        )
        audioTrack.play()

        val codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AMR_NB)
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AMR_NB, sampleRate, 1)
        codec.configure(format, null, null, 0)
        codec.start()

        val info = MediaCodec.BufferInfo()
        var offset = 0

        while (offset < data.size) {
            val inputBufferIndex = codec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                val chunkSize = minOf(data.size - offset, inputBuffer?.capacity() ?: 0)
                inputBuffer?.put(data, offset, chunkSize)
                codec.queueInputBuffer(inputBufferIndex, 0, chunkSize, 0, 0)
                offset += chunkSize
            }

            var outputBufferIndex = codec.dequeueOutputBuffer(info, 10000)
            while (outputBufferIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                val pcmData = ByteArray(info.size)
                outputBuffer?.get(pcmData)
                outputBuffer?.clear()
                audioTrack.write(pcmData, 0, pcmData.size)
                codec.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = codec.dequeueOutputBuffer(info, 10000)
            }
        }
        codec.stop()
        codec.release()
        audioTrack.stop()
        audioTrack.release()
    }

    fun isLiveMode(hopCount: Int): Boolean = hopCount <= 1
}
