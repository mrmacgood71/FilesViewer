package it.macgood.vkfilemanager.domain.usecase

import it.macgood.vkfilemanager.domain.model.FileChecksum
import it.macgood.vkfilemanager.data.repository.FileRepositoryImpl
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class InsertAllFilesUseCase @Inject constructor(
    private val repository: FileRepositoryImpl
) {
    suspend fun execute(fileChecksums: List<FileChecksum>) = repository.insertAll(fileChecksums)
}