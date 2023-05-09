package it.macgood.vkfilemanager.presentation.utils

import it.macgood.domain.model.FileChecksum
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object FileUtils {

    fun countFileSize(file: File): Long {
        val path = Paths.get(file.absolutePath)
        var totalSize = 0L
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                totalSize += attrs.size()
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {
                return FileVisitResult.CONTINUE
            }
        })
        return totalSize / 1024
    }

    fun apacheReadDirectory(directory: File, fileList: MutableList<File>) {
        val files = FileUtils.listFiles(directory, null, true)
        fileList.addAll(files)
    }

    fun readStorageForFindModifiedFiles(
        directory: File,
        fileList: MutableList<FileChecksum>,
        closedTime: Long,
        onFileFound: () -> Unit
    ) {
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                if (file.lastModified() > closedTime) {
                    readStorageForFindModifiedFiles(file, fileList, closedTime,  onFileFound)
                }
            } else {
                if (file.lastModified() > closedTime) {
                    fileList.add(
                        FileChecksum(
                            path = file.path,
                            checksum = Md5Provider.getMd5Checksum(file.absolutePath)
                        )
                    )
                    onFileFound()
                }
            }
        }
    }
}
