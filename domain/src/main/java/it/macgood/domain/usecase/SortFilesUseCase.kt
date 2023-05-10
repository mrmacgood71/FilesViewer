package it.macgood.domain.usecase

import it.macgood.domain.model.SortBy
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SortFilesUseCase @Inject constructor() {

    fun execute(list: List<File>, sortBy: SortBy): List<File> {
        when (sortBy) {
            SortBy.FILENAME_ASC -> {
                return list.sortedBy { it.name.lowercase() }
            }
            SortBy.SIZE_ASC -> {
                return list.sortedBy { it.length() }
            }
            SortBy.SIZE_DESC -> {
                return list.sortedByDescending { it.length() }
            }
            SortBy.DATE_OF_CREATION_ASC -> {
                return list.sortedBy { it.lastModified() }
            }
            SortBy.DATE_OF_CREATION_DESC -> {
                return list.sortedByDescending { it.lastModified() }
            }
            SortBy.EXTENSION_ASC -> {
                return list.sortedBy { it.extension }
            }
            SortBy.EXTENSION_DESC -> {
                return list.sortedByDescending { it.extension }
            }
        }
    }
}