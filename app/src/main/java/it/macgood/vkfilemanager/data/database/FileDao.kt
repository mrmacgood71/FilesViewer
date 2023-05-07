package it.macgood.vkfilemanager.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.macgood.vkfilemanager.data.model.FileChecksum

@Dao
interface FileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fileChecksum: FileChecksum): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fileChecksum: List<FileChecksum>)

    @Query("SELECT * FROM fileChecksum")
    fun selectAll(): LiveData<List<FileChecksum>>

    @Query("SELECT checksum FROM fileChecksum")
    fun selectChecksum(): LiveData<List<String>>

}