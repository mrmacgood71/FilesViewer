package it.macgood.vkfilemanager.presentation.filemanager

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.macgood.vkfilemanager.domain.model.FileChecksum
import it.macgood.vkfilemanager.domain.usecase.InsertAllFilesUseCase
import it.macgood.vkfilemanager.domain.usecase.SelectAllFilesUseCase
import it.macgood.vkfilemanager.presentation.filemanager.model.SortBy
import it.macgood.vkfilemanager.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val selectAllFilesUseCase: SelectAllFilesUseCase,
    private val insertAllFilesUseCase: InsertAllFilesUseCase
): ViewModel() {

    private val _parentPath: MutableLiveData<String> = MutableLiveData()
    val parentPath: LiveData<String> = _parentPath

    private val _rootFiles: MutableLiveData<List<File>> = MutableLiveData()
    val rootFiles: LiveData<List<File>> = _rootFiles

    val selectAll: MutableLiveData<List<FileChecksum>> = MutableLiveData()

    init {
        val path = Environment.getExternalStorageDirectory().path
        val root = File(path)
        _parentPath.postValue(path)
        _rootFiles.postValue(root.listFiles()?.toList())
    }

    fun insertAll(filesChecksum: List<FileChecksum>) = viewModelScope.launch {
        insertAllFilesUseCase.execute(filesChecksum)
    }

    fun setParentPath(path: String) {
        _parentPath.postValue(path)
    }

    fun setRootFiles(files: List<File>?) {
        if (files != null) {
            _rootFiles.postValue(files!!)
        }
    }

    fun selectAllFiles() = viewModelScope.launch {
        selectAll.postValue(selectAllFilesUseCase.execute())
    }

    fun sortFilesBy(sortBy: SortBy) {
        when(sortBy) {
            SortBy.FILENAME_ASC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedBy { it.name.lowercase() })
            }
            SortBy.SIZE_ASC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedBy { FileUtils.countFileSize(it)})
            }
            SortBy.SIZE_DESC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedByDescending { FileUtils.countFileSize(it) })
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