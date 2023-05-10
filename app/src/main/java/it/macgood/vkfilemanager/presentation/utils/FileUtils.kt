package it.macgood.vkfilemanager.presentation.utils

import it.macgood.domain.model.FileChecksum
import it.macgood.vkfilemanager.presentation.filemanager.mapper.FileMapper
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun Long.determineFileSize() = when {
        this > 1024L * 1024L * 1024L -> "%.2f GB".format(this.toDouble() / (1024L * 1024L * 1024L))
        this > 1024 * 1024 -> "%.2f MB".format(this.toDouble() / (1024 * 1024))
        this > 1024 -> "%.2f KB".format(this.toDouble() / 1024)
        else -> "$this B"
    }

    fun Long.convertTime(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(this))
    }

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
        return totalSize
    }

    fun apacheReadDirectory(directory: File, fileList: MutableList<File>) {
        val files = FileUtils.listFiles(directory, null, true)
        fileList.addAll(files)
    }

//    fun readStorageForFindModifiedFiles(
//        directory: File,
//        fileList: MutableList<FileChecksum>,
//        closedTime: Long,
//        onFileFound: () -> Unit
//    ) {
//        val files = directory.listFiles() ?: return
//
//        for (file in files) {
//            if (file.isDirectory) {
//                val paths = Paths.get(file.path)
//                val attrs = Files.readAttributes(paths, BasicFileAttributes::class.java)
//                if (attrs.lastModifiedTime().toMillis() > closedTime) {
//                    readStorageForFindModifiedFiles(file, fileList, closedTime, onFileFound)
//                }
//            } else {
//                if (file.lastModified() > closedTime) {
//                    fileList.add(FileMapper.toFileChecksum(file))
//                    onFileFound()
//                }
//            }
//        }
//    }

    fun readStorageForFindModifiedFiles(
        directory: File,
        fileList: MutableList<FileChecksum>,
        closedTime: Long,
        depth: Int,
        onFileFound: () -> Unit
    ) {
        val files = directory.listFiles() ?: return

        var folderModified = false

        for (file in files) {
            if (file.isDirectory) {
                val paths = Paths.get(file.path)
                val attrs = Files.readAttributes(paths, BasicFileAttributes::class.java)

                if (attrs.lastModifiedTime().toMillis() > closedTime) {
                    folderModified = true
                    readStorageForFindModifiedFiles(file, fileList, closedTime, depth + 1, onFileFound)
                }
            } else {
                if (file.lastModified() > closedTime) {
                    fileList.add(FileMapper.toFileChecksum(file))
                    onFileFound()
                    folderModified = true
                }
            }
        }

        if (!folderModified && depth < 2) {
            for (file in files) {
                if (file.isDirectory) {
                    readStorageForFindModifiedFiles(file, fileList, closedTime, depth + 1, onFileFound)
                }
            }
        }
    }


}


