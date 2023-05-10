package it.macgood.domain.usecase

import it.macgood.domain.model.FileChecksum
import it.macgood.domain.repository.FileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectAllFilesUseCase @Inject constructor(
    private val repository: FileRepository
){
    suspend fun execute(): List<FileChecksum> = repository.selectAll()

}