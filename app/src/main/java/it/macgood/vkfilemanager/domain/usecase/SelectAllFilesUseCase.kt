package it.macgood.vkfilemanager.domain.usecase

import androidx.lifecycle.LiveData
import it.macgood.vkfilemanager.domain.model.FileChecksum
import it.macgood.vkfilemanager.data.repository.FileRepositoryImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectAllFilesUseCase @Inject constructor(
    private val repository: FileRepositoryImpl
){
    suspend fun execute(): List<FileChecksum> = repository.selectAll()

}