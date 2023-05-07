package it.macgood.vkfilemanager.domain.usecase

import androidx.lifecycle.LiveData
import it.macgood.vkfilemanager.data.model.FileChecksum
import it.macgood.vkfilemanager.data.repository.FileRepositoryImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectAllFilesUseCase @Inject constructor(
    private val repository: FileRepositoryImpl
){
    fun execute(): LiveData<List<FileChecksum>> = repository.selectAll()

}