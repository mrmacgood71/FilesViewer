package it.macgood.vkfilemanager.data.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import it.macgood.vkfilemanager.domain.model.FileChecksum

@Database(
    entities = [FileChecksum::class],
    exportSchema = true,
    version = 1
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

        val migration2to3 = object : Migration(3, 4) {
//            CREATE UNIQUE INDEX index_file_path ON fileChecksum(path)
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Delete from fileChecksum")
            }
        }
    }
}