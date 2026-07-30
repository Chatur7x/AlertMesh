package com.alertmesh.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alertmesh.data.db.dao.MessageDao
import com.alertmesh.data.db.dao.PeerDao
import com.alertmesh.data.db.entity.MessageEntity
import com.alertmesh.data.db.entity.PeerEntity

@Database(entities = [MessageEntity::class, PeerEntity::class], version = 1, exportSchema = false)
abstract class MeshDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun peerDao(): PeerDao
}
