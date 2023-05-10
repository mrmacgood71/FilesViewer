package it.macgood.vkfilemanager.presentation.utils

import java.io.File

class DirectoryNode(val file: File, val parent: DirectoryNode?, val children: MutableList<DirectoryNode>) {
    fun hasChangedSince(lastModified: Long): Boolean {
        if (file.lastModified() > lastModified) {
            return true
        }

        for (child in children) {
            if (child.hasChangedSince(lastModified)) {
                return true
            }
        }
        return false
    }
}

fun buildDirectoryTree(rootDir: File): DirectoryNode {
    val children = mutableListOf<DirectoryNode>()

    rootDir.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            children.add(buildDirectoryTree(file))
        }
    }

    return DirectoryNode(rootDir, null, children)
}