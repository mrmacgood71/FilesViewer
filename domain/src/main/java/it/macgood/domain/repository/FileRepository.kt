package it.macgood.domain.repository

import androidx.lifecycle.LiveData
import it.macgood.domain.model.FileChecksum
import javax.inject.Singleton

@Singleton
interface FileRepository {

    suspend fun insertAll(fileChecksums: List<FileChecksum>)

    suspend fun insert(fileChecksum: FileChecksum) : Long

    suspend fun selectAll(): List<FileChecksum>

    fun selectAllChecksums() : LiveData<List<String>>
}