package com.alertmesh.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alertmesh.data.db.entity.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(peer: PeerEntity)

    @Query("SELECT * FROM peers ORDER BY lastSeen DESC")
    fun getAllPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE deviceId = :deviceId")
    suspend fun getPeer(deviceId: String): PeerEntity?

    @Query("SELECT COUNT(*) FROM peers WHERE isOnline = 1")
    suspend fun getOnlinePeerCount(): Int

    @Query("UPDATE peers SET isOnline = 0 WHERE lastSeen < :threshold")
    suspend fun markStale(threshold: Long)
}
