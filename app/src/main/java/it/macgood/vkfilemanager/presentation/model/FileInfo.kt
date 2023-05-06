package it.macgood.vkfilemanager.presentation.model

data class FileInfo(
    val id: Long,
    val name: String,
    val size: Long,
    val dateOfCreation: String,
    val icon: Int
) {
}