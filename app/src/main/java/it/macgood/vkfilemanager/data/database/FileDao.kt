package it.macgood.vkfilemanager.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.macgood.vkfilemanager.domain.model.FileChecksum

@Dao
interface FileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fileChecksum: FileChecksum): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fileChecksum: List<FileChecksum>)

    @Query("SELECT * FROM fileChecksum")
    suspend fun selectAll(): List<FileChecksum>

    @Query("SELECT checksum FROM fileChecksum")
    fun selectChecksum(): LiveData<List<String>>

}