package com.alertmesh.protocol.crypto

import com.alertmesh.protocol.model.NodeIdentity

interface CryptoProvider {
    fun generateIdentity(): NodeIdentity
    fun sign(data: ByteArray): ByteArray
    fun verify(publicKey: ByteArray, data: ByteArray, signature: ByteArray): Boolean
    fun encryptAead(plaintext: ByteArray, associatedData: ByteArray): ByteArray
    fun decryptAead(ciphertext: ByteArray, associatedData: ByteArray): ByteArray
    fun getPublicKey(): ByteArray
    fun getFingerprint(): String
    fun getDeviceId(): String
    fun deriveSessionKey(peerPublicKey: ByteArray): ByteArray
    fun encryptForRecipient(plaintext: ByteArray, recipientPublicKey: ByteArray): ByteArray
    fun decryptFromSender(ciphertext: ByteArray): ByteArray
}
