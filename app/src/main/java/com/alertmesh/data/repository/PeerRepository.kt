package com.alertmesh.data.repository

import android.util.Base64
import com.alertmesh.data.db.dao.PeerDao
import com.alertmesh.data.db.entity.PeerEntity
import com.alertmesh.protocol.model.PresencePayload
import kotlinx.coroutines.flow.Flow

class PeerRepository(private val peerDao: PeerDao) {
    fun getAllPeers(): Flow<List<PeerEntity>> {
        return peerDao.getAllPeers()
    }

    suspend fun updatePeer(presence: PresencePayload, publicKey: ByteArray) {
        val deviceId = presence.deviceId
        val entity = PeerEntity(
            deviceId = deviceId,
            fingerprint = Base64.encodeToString(publicKey.take(16).toByteArray(), Base64.NO_WRAP),
            publicKey = Base64.encodeToString(publicKey, Base64.NO_WRAP),
            lastSeen = System.currentTimeMillis(),
            hopCount = 0,
            batteryPercent = presence.batteryPercent,
            isOnline = true
        )
        peerDao.upsert(entity)
    }

    suspend fun getOnlineCount(): Int {
        return peerDao.getOnlinePeerCount()
    }

    suspend fun markStale(timeoutMs: Long = 60_000) {
        val threshold = System.currentTimeMillis() - timeoutMs
        peerDao.markStale(threshold)
    }
}
