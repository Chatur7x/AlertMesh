package com.alertmesh.protocol.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.HybridDecrypt
import com.google.crypto.tink.HybridEncrypt
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.hybrid.HybridConfig
import com.google.crypto.tink.hybrid.HybridKeyTemplates
import com.google.crypto.tink.signature.SignatureConfig
import com.google.crypto.tink.signature.SignatureKeyTemplates
import com.alertmesh.protocol.model.NodeIdentity
import java.security.MessageDigest
import java.io.ByteArrayOutputStream

class TinkCryptoProvider : CryptoProvider {
    init {
        SignatureConfig.register()
        AeadConfig.register()
        HybridConfig.register()
    }

    private val signKeysetHandle: KeysetHandle = KeysetHandle.generateNew(SignatureKeyTemplates.ED25519)
    private val hybridKeysetHandle: KeysetHandle = KeysetHandle.generateNew(HybridKeyTemplates.ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM)
    private var sessionAeadKeysetHandle: KeysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM)
    private var identity: NodeIdentity? = null

    override fun generateIdentity(): NodeIdentity {
        val pubKeysetHandle = signKeysetHandle.publicKeysetHandle
        val out = ByteArrayOutputStream()
        CleartextKeysetHandle.write(pubKeysetHandle, com.google.crypto.tink.JsonKeysetWriter.withOutputStream(out))
        val publicKeyBytes = out.toByteArray()
        
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(publicKeyBytes)
        val deviceId = (0..7).joinToString("") { "%02x".format(hash[it]) }
        
        val fingerprint = NodeIdentity.generateFingerprint(publicKeyBytes)
        
        identity = NodeIdentity(deviceId, publicKeyBytes, fingerprint)
        return identity!!
    }

    override fun sign(data: ByteArray): ByteArray {
        val signer = signKeysetHandle.getPrimitive(PublicKeySign::class.java)
        return signer.sign(data)
    }

    override fun verify(publicKey: ByteArray, data: ByteArray, signature: ByteArray): Boolean {
        return try {
            val pubHandle = CleartextKeysetHandle.read(com.google.crypto.tink.JsonKeysetReader.withBytes(publicKey))
            val verifier = pubHandle.getPrimitive(PublicKeyVerify::class.java)
            verifier.verify(signature, data)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun encryptAead(plaintext: ByteArray, associatedData: ByteArray): ByteArray {
        val aead = sessionAeadKeysetHandle.getPrimitive(Aead::class.java)
        return aead.encrypt(plaintext, associatedData)
    }

    override fun decryptAead(ciphertext: ByteArray, associatedData: ByteArray): ByteArray {
        val aead = sessionAeadKeysetHandle.getPrimitive(Aead::class.java)
        return aead.decrypt(ciphertext, associatedData)
    }

    override fun getPublicKey(): ByteArray {
        return identity?.publicKey ?: generateIdentity().publicKey
    }

    override fun getFingerprint(): String {
        return identity?.fingerprint ?: generateIdentity().fingerprint
    }

    override fun getDeviceId(): String {
        return identity?.deviceId ?: generateIdentity().deviceId
    }

    override fun deriveSessionKey(peerPublicKey: ByteArray): ByteArray {
        sessionAeadKeysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM)
        return ByteArray(0)
    }

    override fun encryptForRecipient(plaintext: ByteArray, recipientPublicKey: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        CleartextKeysetHandle.write(hybridKeysetHandle.publicKeysetHandle, com.google.crypto.tink.JsonKeysetWriter.withOutputStream(out))
        val pubHandle = CleartextKeysetHandle.read(com.google.crypto.tink.JsonKeysetReader.withBytes(out.toByteArray()))
        val encryptor = pubHandle.getPrimitive(HybridEncrypt::class.java)
        return encryptor.encrypt(plaintext, ByteArray(0))
    }

    override fun decryptFromSender(ciphertext: ByteArray): ByteArray {
        val decryptor = hybridKeysetHandle.getPrimitive(HybridDecrypt::class.java)
        return decryptor.decrypt(ciphertext, ByteArray(0))
    }
}
