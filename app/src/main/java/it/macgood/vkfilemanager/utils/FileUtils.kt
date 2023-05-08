package it.macgood.vkfilemanager.utils

import java.io.File
object FileUtils {
    fun countFileSize(file: File) =
        (file.walkTopDown().filter { it.isFile }.map { it.length() }
            .sum() / 1024).toString() + " kb"
}
