package it.macgood.vkfilemanager.presentation

data class FileInfo(
    val id: Long,
    val name: String,
    val size: Long,
    val dateOfCreation: String,
    val icon: Int
) {
}