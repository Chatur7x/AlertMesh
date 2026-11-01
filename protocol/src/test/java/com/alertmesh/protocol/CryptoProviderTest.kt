package com.alertmesh.protocol

import com.alertmesh.protocol.crypto.TinkCryptoProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoProviderTest {
    @Test
    fun testKeypairGeneration() {
        val provider = TinkCryptoProvider()
        val identity = provider.generateIdentity()
        assertNotNull(identity.deviceId)
        assertTrue(identity.publicKey.isNotEmpty())
        assertNotNull(identity.fingerprint)
    }

    @Test
    fun testSignVerifyRoundtrip() {
        val provider = TinkCryptoProvider()
        val identity = provider.generateIdentity()
        val data = "Hello, Mesh!".toByteArray()
        val signature = provider.sign(data)
        assertTrue(provider.verify(identity.publicKey, data, signature))
    }

    @Test
    fun testVerifyFailsWithWrongKey() {
        val provider1 = TinkCryptoProvider()
        provider1.generateIdentity()
        val data = "Hello".toByteArray()
        val signature = provider1.sign(data)
        
        val provider2 = TinkCryptoProvider()
        val identity2 = provider2.generateIdentity()
        assertFalse(provider1.verify(identity2.publicKey, data, signature))
    }

    @Test
    fun testAeadRoundtrip() {
        val provider = TinkCryptoProvider()
        provider.generateIdentity()
        provider.deriveSessionKey(ByteArray(0))
        
        val plaintext = "Secret Message".toByteArray()
        val aad = "Context".toByteArray()
        val ciphertext = provider.encryptAead(plaintext, aad)
        val decrypted = provider.decryptAead(ciphertext, aad)
        
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun testEciesRoundtrip() {
        val provider = TinkCryptoProvider()
        val identity = provider.generateIdentity()
        val plaintext = "Hello ECIES".toByteArray()
        val ciphertext = provider.encryptForRecipient(plaintext, identity.hybridPublicKey)
        val decrypted = provider.decryptFromSender(ciphertext, identity.hybridPublicKey)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun testCrossDeviceEciesRelayCannotDecrypt() {
        val alice = TinkCryptoProvider()
        val bob = TinkCryptoProvider()
        val mallory = TinkCryptoProvider()

        val bobIdentity = bob.generateIdentity()

        val plaintext = "Secret for Bob".toByteArray()

        // Alice encrypts for Bob using Bob's hybrid public key
        val ciphertext = alice.encryptForRecipient(plaintext, bobIdentity.hybridPublicKey)

        // Bob decrypts with his own private key
        val decryptedByBob = bob.decryptFromSender(ciphertext, bobIdentity.hybridPublicKey)
        assertArrayEquals(plaintext, decryptedByBob)

        // Mallory (relay) knows both public keys but only has Mallory's private key — cannot decrypt
        var decryptedByMallory: ByteArray? = null
        try {
            decryptedByMallory = mallory.decryptFromSender(ciphertext, mallory.generateIdentity().hybridPublicKey)
        } catch (_: Exception) {
            // expected
        }
        assertTrue(decryptedByMallory == null || !decryptedByMallory.contentEquals(plaintext))
    }
}
