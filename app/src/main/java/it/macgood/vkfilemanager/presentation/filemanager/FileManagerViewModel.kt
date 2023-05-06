package it.macgood.vkfilemanager.presentation.filemanager

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.macgood.vkfilemanager.presentation.model.SortBy
import java.io.File

class FileManagerViewModel : ViewModel() {

    // TODO: private livedatas
    private val _parentPath: MutableLiveData<String> = MutableLiveData()

    val parentPath: LiveData<String> = _parentPath
    val _rootFiles: MutableLiveData<List<File>> = MutableLiveData()
    val rootFiles: LiveData<List<File>> = _rootFiles

    init {
        val path = Environment.getExternalStorageDirectory().path
        val root = File(path)
        _rootFiles.postValue(root.listFiles().toList())
    }

    fun setParentPath(path: String) {
        _parentPath.postValue(path)
    }

    fun setRootFiles(files: List<File>?) {
        _rootFiles.postValue(files!!)
    }

    fun sortFilesBy(sortBy: SortBy) {
        when(sortBy) {
            SortBy.FILENAME_ASC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedBy { it.name })
            }
            SortBy.FILENAME_DESC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedByDescending { it.name })
            }
            SortBy.SIZE_ASC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedBy { it.length() })
            }
            SortBy.SIZE_DESC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedByDescending { it.length() })
            }
            SortBy.DATE_OF_CREATION_ASC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedBy { it.lastModified() })
            }
            SortBy.DATE_OF_CREATION_DESC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedByDescending { it.lastModified() })
            }
            SortBy.EXTENSION_ASC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedBy { it.extension })
            }
            SortBy.EXTENSION_DESC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedByDescending { it.extension })
            }

        }

    }

}