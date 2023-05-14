package it.macgood.vkfilemanager.presentation.filemanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.macgood.domain.model.FileChecksum
import it.macgood.domain.model.SortBy
import it.macgood.domain.usecase.InsertAllFilesUseCase
import it.macgood.domain.usecase.SelectAllFilesUseCase
import it.macgood.domain.usecase.SortFilesUseCase
import it.macgood.vkfilemanager.presentation.filemanager.mapper.FileMapper
import it.macgood.vkfilemanager.presentation.utils.FileUtils
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val externalStoragePath: String,
    private val selectAllFilesUseCase: SelectAllFilesUseCase,
    private val insertAllFilesUseCase: InsertAllFilesUseCase,
    private val sortFilesUseCase: SortFilesUseCase
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
        _rootFiles.postValue(_rootFiles.value?.let { sortFilesUseCase.execute(it, sortBy) })
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
                closedTime = closedTime,
                depth = 0
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

    fun saveStorageFilesOnFirstOpenApp(
        onComplete: (List<File>) -> Unit,
        externalDir: File
    ) = viewModelScope.launch {
        val fileList = mutableListOf<File>()
        try {
            val readJob = async(Dispatchers.IO) {
                FileUtils.apacheReadDirectory(externalDir, fileList)
            }
            val storageFilesList = FileMapper.toFileChecksums(readJob.await())
            val modifiedFilesList: MutableList<File> = mutableListOf()

            insertAll(storageFilesList)
            modifiedList.postValue(modifiedFilesList)
            withContext(Dispatchers.Main) {
                onComplete(modifiedFilesList)
            }

        } catch (e: AccessDeniedException) { }
    }

    fun saveStorageFilesWithLimits(
        onComplete: (List<File>) -> Unit,
        externalDir: File
    ) = viewModelScope.launch {
        val fileList = mutableListOf<File>()
        try {
            val readJob = async(Dispatchers.IO) {
                FileUtils.readDirectory(externalDir, fileList)
            }
            readJob.join()
            val storageFilesList = FileMapper.toFileChecksums(fileList)
            val modifiedFilesList: MutableList<File> = mutableListOf()

            insertAll(storageFilesList)
            modifiedList.postValue(modifiedFilesList)
            withContext(Dispatchers.Main) {
                onComplete(modifiedFilesList)
            }

        } catch (e: AccessDeniedException) { }
    }


    private fun insertAll(filesChecksum: List<FileChecksum>) = viewModelScope.launch {
        insertAllFilesUseCase.execute(filesChecksum)
    }
}