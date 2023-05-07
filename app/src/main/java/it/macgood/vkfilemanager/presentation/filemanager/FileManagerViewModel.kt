package it.macgood.vkfilemanager.presentation.filemanager

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.macgood.vkfilemanager.data.model.FileChecksum
import it.macgood.vkfilemanager.domain.usecase.SelectAllFilesUseCase
import it.macgood.vkfilemanager.presentation.model.SortBy
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val selectAllFilesUseCase: SelectAllFilesUseCase
): ViewModel() {

    private val _parentPath: MutableLiveData<String> = MutableLiveData()
    val parentPath: LiveData<String> = _parentPath

    private val _rootFiles: MutableLiveData<List<File>> = MutableLiveData()
    val rootFiles: LiveData<List<File>> = _rootFiles

    val databaseFileChecksums: MutableLiveData<List<FileChecksum>> = MutableLiveData()

    init {
        val path = Environment.getExternalStorageDirectory().path
        val root = File(path)
        _rootFiles.postValue(root.listFiles().toList().sortedBy { it.name.lowercase() })
    }

    fun setParentPath(path: String) {
        _parentPath.postValue(path)
    }

    fun setRootFiles(files: List<File>?) {
        if (files != null) {
            _rootFiles.postValue(files!!)
        }
    }

    fun selectAllFiles() {
        databaseFileChecksums.postValue(selectAllFilesUseCase.execute().value)
    }

    fun sortFilesBy(sortBy: SortBy) {
        when(sortBy) {
            SortBy.FILENAME_ASC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedBy { it.name.lowercase() })
            }
            SortBy.FILENAME_DESC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedByDescending { it.name.lowercase() })
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