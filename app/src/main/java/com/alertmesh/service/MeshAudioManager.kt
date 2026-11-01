package com.alertmesh.service

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioAttributes
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
import kotlinx.coroutines.Job
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
    @Volatile private var isRecording = false
    private var recordingJob: Job? = null
    private val recordingScope = CoroutineScope(Dispatchers.IO)
    private val outputStream = ByteArrayOutputStream()

    @SuppressLint("MissingPermission")
    fun startRecording(): Boolean {
        if (isRecording) return true
        if (bufferSizeIn <= 0) return false
        outputStream.reset()

        val recorder = try {
            AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfigIn, audioFormat, bufferSizeIn)
        } catch (_: Exception) { return false }
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return false
        }
        try {
            recorder.startRecording()
        } catch (_: Exception) {
            recorder.release()
            return false
        }
        audioRecord = recorder
        isRecording = true

        recordingJob = recordingScope.launch {
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
        return true
    }

    suspend fun stopRecording(): ByteArray {
        isRecording = false
        try { audioRecord?.stop() } catch (_: IllegalStateException) {}
        recordingJob?.join()
        audioRecord?.release()
        audioRecord = null
        return outputStream.toByteArray()
    }

    suspend fun playAudio(data: ByteArray) = withContext(Dispatchers.IO) {
        // AMR-NB's first byte identifies a 20 ms frame. Feed frames separately;
        // queuing the whole note as one codec buffer decodes only its first frame.
        val frameSizes = intArrayOf(13, 14, 16, 18, 20, 21, 27, 32, 6, 0, 0, 0, 0, 0, 0, 1)
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(sampleRate)
                .setChannelMask(channelConfigOut).setEncoding(audioFormat).build())
            .setBufferSizeInBytes(bufferSizeOut)
            .setTransferMode(AudioTrack.MODE_STREAM).build()
        val codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AMR_NB)
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AMR_NB, sampleRate, 1)
        codec.configure(format, null, null, 0)
        try {
            codec.start()
            audioTrack.play()
            val info = MediaCodec.BufferInfo()
            var offset = 0
            var frameIndex = 0L
            var idleCount = 0

            while (offset < data.size) {
                val frameType = (data[offset].toInt() and 0xff) shr 3 and 0x0f
                val frameSize = frameSizes[frameType]
                require(frameSize > 0 && offset + frameSize <= data.size) { "Invalid AMR voice frame" }
                val inputIndex = codec.dequeueInputBuffer(10_000)
                if (inputIndex < 0) {
                    if (++idleCount > 100) error("Voice decoder stalled")
                    continue
                }
                idleCount = 0
                codec.getInputBuffer(inputIndex)!!.apply {
                    clear()
                    put(data, offset, frameSize)
                }
                codec.queueInputBuffer(inputIndex, 0, frameSize, frameIndex++ * 20_000, 0)
                offset += frameSize
                while (true) {
                    val outputIndex = codec.dequeueOutputBuffer(info, 0)
                    if (outputIndex < 0) break
                    val output = codec.getOutputBuffer(outputIndex)!!
                    output.position(info.offset)
                    output.limit(info.offset + info.size)
                    while (output.hasRemaining()) {
                        check(audioTrack.write(output, output.remaining(), AudioTrack.WRITE_BLOCKING) > 0) {
                            "Voice playback output failed"
                        }
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                }
            }
            var inputIndex: Int
            do { inputIndex = codec.dequeueInputBuffer(10_000) } while (inputIndex < 0)
            codec.queueInputBuffer(inputIndex, 0, 0, frameIndex * 20_000, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            idleCount = 0
            while (true) {
                val outputIndex = codec.dequeueOutputBuffer(info, 10_000)
                if (outputIndex < 0) {
                    if (++idleCount > 100) error("Voice decoder did not finish")
                    continue
                }
                idleCount = 0
                val output = codec.getOutputBuffer(outputIndex)!!
                output.position(info.offset)
                output.limit(info.offset + info.size)
                while (output.hasRemaining()) {
                    check(audioTrack.write(output, output.remaining(), AudioTrack.WRITE_BLOCKING) > 0) {
                        "Voice playback output failed"
                    }
                }
                val finished = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                codec.releaseOutputBuffer(outputIndex, false)
                if (finished) break
            }
        } finally {
            codec.stop()
            codec.release()
            audioTrack.stop()
            audioTrack.release()
        }
    }

    fun isLiveMode(hopCount: Int): Boolean = hopCount <= 1
}
