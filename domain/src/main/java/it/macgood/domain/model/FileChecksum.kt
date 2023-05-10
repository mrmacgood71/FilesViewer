package it.macgood.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fileChecksum",
    indices = [Index(value = ["path"], unique = true)]
)
data class FileChecksum(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "path")
    val path: String,
    val checksum: String
) : java.io.Serializable