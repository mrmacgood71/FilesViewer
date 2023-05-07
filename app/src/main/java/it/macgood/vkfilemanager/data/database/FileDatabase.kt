package it.macgood.vkfilemanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.macgood.vkfilemanager.data.model.FileChecksum

@Database(
    entities = [FileChecksum::class],
    exportSchema = true,
    version = 1,
)
abstract class FileDatabase : RoomDatabase() {

    abstract fun getFileDao(): FileDao

    companion object {
        @Volatile
        private var instance: FileDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also{ instance = it }
        }

        private fun createDatabase(applicationContext: Context) = Room.databaseBuilder(
            applicationContext,
            FileDatabase::class.java,
            "files_checksum_db.db"
        ).build()
    }
}