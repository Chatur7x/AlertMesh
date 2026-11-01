package com.alertmesh.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alertmesh.data.db.dao.MessageDao
import com.alertmesh.data.db.dao.PeerDao
import com.alertmesh.data.db.entity.MessageEntity
import com.alertmesh.data.db.entity.PeerEntity

@Database(entities = [MessageEntity::class, PeerEntity::class], version = 2, exportSchema = false)
abstract class MeshDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun peerDao(): PeerDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS messages_new (msgId TEXT NOT NULL PRIMARY KEY, origin TEXT NOT NULL, counter INTEGER NOT NULL, type TEXT NOT NULL, channel INTEGER NOT NULL, payload TEXT NOT NULL, signature TEXT NOT NULL, timestamp INTEGER NOT NULL, hopCount INTEGER NOT NULL, status TEXT NOT NULL)")
                db.execSQL("INSERT INTO messages_new (msgId, origin, counter, type, channel, payload, signature, timestamp, hopCount, status) SELECT msgId, origin, counter, type, channel, payload, signature, timestamp, hopCount, CASE WHEN delivered = 1 THEN 'sent' ELSE 'queued' END FROM messages")
                db.execSQL("DROP TABLE messages")
                db.execSQL("ALTER TABLE messages_new RENAME TO messages")
            }
        }
    }
}
