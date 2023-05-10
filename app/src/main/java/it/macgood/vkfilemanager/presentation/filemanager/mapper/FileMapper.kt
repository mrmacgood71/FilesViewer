package it.macgood.vkfilemanager.presentation.filemanager.mapper

import it.macgood.domain.model.FileChecksum
import it.macgood.vkfilemanager.presentation.utils.Md5Provider
import java.io.File

class FileMapper {

    companion object {

        fun toFileChecksum(file: File) : FileChecksum {
            return FileChecksum(path = file.path, checksum = Md5Provider.getMd5Checksum(file.path))
        }

        fun toFileChecksums(files: List<File>): List<FileChecksum> {
            return files.map { file ->
                FileChecksum(
                    path = file.path,
                    checksum = Md5Provider.getMd5Checksum(file.path)
                )
            }
        }
        fun toFile(fileChecksum: FileChecksum) : File {
            return File(fileChecksum.path)
        }

        fun toFiles(files: List<FileChecksum> ): List<File>{
            return files.map { file -> File(file.path) }
        }
    }
}