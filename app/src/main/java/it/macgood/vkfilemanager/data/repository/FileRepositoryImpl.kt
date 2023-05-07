package it.macgood.vkfilemanager.data.repository

import androidx.lifecycle.LiveData
import it.macgood.vkfilemanager.data.model.FileChecksum
import it.macgood.vkfilemanager.data.database.FileDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val database: FileDatabase
) {
    suspend fun insertAll(fileChecksums: List<FileChecksum>) = database.getFileDao().insertAll(fileChecksums)

    suspend fun insert(fileChecksum: FileChecksum) = database.getFileDao().insert(fileChecksum)

    fun selectAll(): LiveData<List<FileChecksum>> = database.getFileDao().selectAll()

    fun selectAllChecksums() : LiveData<List<String>> = database.getFileDao().selectChecksum()

}