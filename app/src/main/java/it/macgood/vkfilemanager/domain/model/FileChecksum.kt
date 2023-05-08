package it.macgood.vkfilemanager.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import it.macgood.vkfilemanager.utils.Md5Provider
import java.io.File

@Entity(
    tableName = "fileChecksum",
    indices = [Index(value = ["path"], unique = true)]
)
data class FileChecksum(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    //TODO: make unique
    @ColumnInfo(name = "path")
    val path: String,
    val checksum: String
) : java.io.Serializable