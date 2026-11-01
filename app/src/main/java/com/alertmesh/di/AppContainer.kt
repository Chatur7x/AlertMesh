package com.alertmesh.di

import android.content.Context
import androidx.room.Room
import com.alertmesh.data.db.MeshDatabase
import com.alertmesh.data.db.RoomSyncStore
import com.alertmesh.data.repository.MessageRepository
import com.alertmesh.data.repository.PeerRepository
import com.alertmesh.protocol.ProtocolEngine
import com.alertmesh.protocol.cache.DedupCache
import com.alertmesh.protocol.cache.ReplayGuard
import com.alertmesh.protocol.crypto.TinkCryptoProvider
import com.alertmesh.protocol.routing.RateLimiter
import com.alertmesh.protocol.routing.SuppressionTracker
import com.alertmesh.protocol.sync.DigestSync
import com.alertmesh.protocol.transport.SocketTransport
import com.alertmesh.protocol.transport.Transport
import com.alertmesh.service.MeshAudioManager
import com.alertmesh.service.MeshLocationManager
import com.alertmesh.service.NearbyTransport
import com.google.crypto.tink.hybrid.HybridKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.signature.SignatureKeyTemplates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val database: MeshDatabase by lazy {
        Room.databaseBuilder(context, MeshDatabase::class.java, "alertmesh-db")
            .addMigrations(MeshDatabase.MIGRATION_1_2)
            .build()
    }

    val cryptoProvider: TinkCryptoProvider by lazy {
        val masterKey = "android-keystore://alertmesh_master_key"
        val signKey = AndroidKeysetManager.Builder()
            .withSharedPref(context, "signing_keyset", "alertmesh_keys")
            .withMasterKeyUri(masterKey)
            .withKeyTemplate(SignatureKeyTemplates.ED25519)
            .build().keysetHandle
        val hybridKey = AndroidKeysetManager.Builder()
            .withSharedPref(context, "hybrid_keyset", "alertmesh_keys")
            .withMasterKeyUri(masterKey)
            .withKeyTemplate(HybridKeyTemplates.ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM)
            .build().keysetHandle
        TinkCryptoProvider(signKey, hybridKey)
    }

    val dedupCache: DedupCache by lazy {
        DedupCache()
    }

    val replayGuard: ReplayGuard by lazy {
        ReplayGuard()
    }

    val rateLimiter: RateLimiter by lazy {
        RateLimiter()
    }

    val suppressionTracker: SuppressionTracker by lazy {
        SuppressionTracker()
    }

    val messageRepository: MessageRepository by lazy {
        MessageRepository(database.messageDao())
    }

    val peerRepository: PeerRepository by lazy {
        PeerRepository(database.peerDao())
    }

    val isEmulator: Boolean = android.os.Build.FINGERPRINT.contains("generic") || android.os.Build.PRODUCT.contains("sdk")

    val transport: Transport by lazy {
        if (isEmulator) {
            SocketTransport(
                localPeerId = cryptoProvider.getDeviceId(),
                host = "10.0.2.2",
                port = 9999
            )
        } else {
            NearbyTransport(context, cryptoProvider)
        }
    }

    val digestSync: DigestSync by lazy {
        DigestSync(RoomSyncStore(database.messageDao()), cryptoProvider) { origin ->
            protocolEngine.knownPublicKey(origin)
        }
    }

    val protocolEngine: ProtocolEngine by lazy {
        ProtocolEngine(
            transport = transport,
            cryptoProvider = cryptoProvider,
            dedupCache = dedupCache,
            replayGuard = replayGuard,
            rateLimiter = rateLimiter,
            suppressionTracker = suppressionTracker,
            digestSync = digestSync,
            messageListener = { envelope ->
                scope.launch {
                    messageRepository.saveMessage(envelope, 0)
                }
            },
            peerPresenceListener = { presence, pubKey ->
                scope.launch {
                    peerRepository.updatePeer(presence, pubKey)
                }
            }
        )
    }

    val locationManager: MeshLocationManager by lazy {
        MeshLocationManager(context)
    }

    val audioManager: MeshAudioManager by lazy {
        MeshAudioManager()
    }

    val barometerManager: com.alertmesh.service.MeshBarometerManager by lazy {
        com.alertmesh.service.MeshBarometerManager(context)
    }
}
