package it.macgood.vkfilemanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "fileChecksum"
)
data class FileChecksum(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val path: String,
    val checksum: String
) : java.io.Serializable