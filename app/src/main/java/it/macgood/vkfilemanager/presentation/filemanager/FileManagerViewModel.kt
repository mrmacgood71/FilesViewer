package it.macgood.vkfilemanager.presentation.filemanager

import android.content.Context
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.macgood.domain.model.FileChecksum
import it.macgood.domain.usecase.InsertAllFilesUseCase
import it.macgood.domain.usecase.SelectAllFilesUseCase
import it.macgood.vkfilemanager.R
import it.macgood.vkfilemanager.presentation.MainActivity
import it.macgood.vkfilemanager.presentation.filemanager.mapper.FileMapper
import it.macgood.vkfilemanager.presentation.filemanager.model.SortBy
import it.macgood.vkfilemanager.presentation.utils.FileUtils
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val externalStoragePath: String,
    private val selectAllFilesUseCase: SelectAllFilesUseCase,
    private val insertAllFilesUseCase: InsertAllFilesUseCase
) : ViewModel() {

    private val _parentPath: MutableLiveData<String> = MutableLiveData()
    val parentPath: LiveData<String> = _parentPath

    private val _rootFiles: MutableLiveData<List<File>> = MutableLiveData()
    val rootFiles: LiveData<List<File>> = _rootFiles

    private val _isReadingIsWorking: MutableLiveData<Boolean> = MutableLiveData()
    val isReadingIsWorking: LiveData<Boolean> = _isReadingIsWorking

    val modifiedList: MutableLiveData<List<File>> = MutableLiveData()

    init {
        val root = File(externalStoragePath)
        _parentPath.postValue(externalStoragePath)
        _rootFiles.postValue(root.listFiles()?.toList())
        _isReadingIsWorking.postValue(false)
    }

    fun setParentPath(path: String) {
        _parentPath.postValue(path)
    }

    fun setRootFiles(files: List<File>?) {
        if (files != null) {
            _rootFiles.postValue(files!!)
        }
    }

    fun setIsReadingIsWorking(isWorking: Boolean) {
        _isReadingIsWorking.postValue(isWorking)
    }

    fun sortFilesBy(sortBy: SortBy) {
        when (sortBy) {
            SortBy.FILENAME_ASC -> {
                _rootFiles.postValue(_rootFiles.value?.sortedBy { it.name.lowercase() })
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

    @OptIn(DelicateCoroutinesApi::class)
    fun checkModifiedFilesOnNotFirstOpenApp(
        onComplete: (List<File>) -> Unit,
        externalDir: File,
        closedTime: Long
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val databaseJob = async(Dispatchers.IO) {
                selectAllFilesUseCase.execute()
            }
            val fileList = mutableListOf<FileChecksum>()

            val databaseFiles = databaseJob.await()
            val modifiedFilesList: MutableList<File> = mutableListOf()
            FileUtils.readStorageForFindModifiedFiles(
                directory = externalDir,
                fileList = fileList,
                closedTime = closedTime
            ) {
                val last = fileList.last()
                val newFile = databaseFiles.find { it.path == last.path }
                if (newFile == null) {
                    modifiedFilesList.add(FileMapper.toFile(last))
                } else {
                    val fileMap = databaseFiles.associateBy { it.path }
                    for (last in fileList) {
                        val file = fileMap[last.path]
                        if (file != null && file.checksum != last.checksum) {
                            modifiedFilesList.add(FileMapper.toFile(last))
                        }
                    }
                }
            }
            insertAll(fileList)
            modifiedList.postValue(modifiedFilesList)
            withContext(Dispatchers.Main) {
                onComplete(modifiedFilesList)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveStorageFilesOnFirstOpenApp(
        onComplete: (List<File>) -> Unit,
        externalDir: File
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val fileList = mutableListOf<File>()

            val readJob = launch(Dispatchers.IO) {
                FileUtils.apacheReadDirectory(externalDir, fileList)
            }

            val databaseJob = async(Dispatchers.IO) {
                selectAllFilesUseCase.execute()
            }
            readJob.join()
            val storageFilesList = FileMapper.toFileChecksums(fileList)
            val modifiedFilesList: MutableList<File> = mutableListOf()
            val databaseFilesList = databaseJob.await()
            val storageFilesMap = storageFilesList.associateBy { it.path }

            for (file in databaseFilesList) {
                val storageFile = storageFilesMap[file.path]
                if (storageFile != null && storageFile.checksum != file.checksum) {
                    modifiedFilesList.add(FileMapper.toFile(file))
                }
            }
            insertAll(storageFilesList)
            modifiedList.postValue(modifiedFilesList)
            withContext(Dispatchers.Main) {
                onComplete(modifiedFilesList)
            }
        }
    }

    private fun insertAll(filesChecksum: List<FileChecksum>) = viewModelScope.launch {
        insertAllFilesUseCase.execute(filesChecksum)
    }
}