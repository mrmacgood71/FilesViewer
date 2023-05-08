package it.macgood.vkfilemanager.presentation.filemanager.mapper

import it.macgood.vkfilemanager.domain.model.FileChecksum
import it.macgood.vkfilemanager.utils.Md5Provider
import java.io.File

class FileMapper {

    companion object {
        fun toFileChecksum(files: List<File>): List<FileChecksum> {
            return files.map { file ->
                FileChecksum(
                    path = file.path,
                    checksum = Md5Provider.getMd5Checksum(file.path)
                )
            }
        }

        fun toFileChecksum(file: File) : FileChecksum {
            return FileChecksum(path = file.path, checksum = Md5Provider.getMd5Checksum(file.path))
        }

        fun toFile(fileChecksum: FileChecksum) : File {
            return File(fileChecksum.path)
        }
    }

}