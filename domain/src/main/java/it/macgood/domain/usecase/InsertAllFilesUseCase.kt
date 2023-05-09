package it.macgood.domain.usecase

import it.macgood.domain.model.FileChecksum
import it.macgood.domain.repository.FileRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class InsertAllFilesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend fun execute(fileChecksums: List<FileChecksum>) = repository.insertAll(fileChecksums)
}