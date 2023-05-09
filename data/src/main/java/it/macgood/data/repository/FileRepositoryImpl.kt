package it.macgood.data.repository

import androidx.lifecycle.LiveData
import it.macgood.domain.model.FileChecksum
import it.macgood.data.database.FileDatabase
import it.macgood.domain.repository.FileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val database: FileDatabase
) : FileRepository {
    override suspend fun insertAll(fileChecksums: List<FileChecksum>) = database.getFileDao().insertAll(fileChecksums)

    override suspend fun insert(fileChecksum: FileChecksum) = database.getFileDao().insert(fileChecksum)

    override suspend fun selectAll(): List<FileChecksum> = database.getFileDao().selectAll()

    override fun selectAllChecksums() : LiveData<List<String>> = database.getFileDao().selectChecksum()

}