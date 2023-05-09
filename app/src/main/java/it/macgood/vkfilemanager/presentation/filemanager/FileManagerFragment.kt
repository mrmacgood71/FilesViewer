package it.macgood.vkfilemanager.presentation.filemanager

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import it.macgood.core.fragment.BaseFragment
import it.macgood.vkfilemanager.R
import it.macgood.vkfilemanager.databinding.FragmentFileManagerBinding
import it.macgood.vkfilemanager.databinding.PartSortRadioGroupBinding
import it.macgood.vkfilemanager.domain.model.FileChecksum
import it.macgood.vkfilemanager.domain.usecase.SelectAllFilesUseCase
import it.macgood.vkfilemanager.presentation.MainActivity
import it.macgood.vkfilemanager.presentation.filemanager.adapter.FileManagerAdapter
import it.macgood.vkfilemanager.presentation.filemanager.mapper.FileMapper
import it.macgood.vkfilemanager.presentation.filemanager.model.SortBy
import it.macgood.vkfilemanager.utils.Md5Provider
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class FileManagerFragment : BaseFragment() {

    val fileManagerViewModel by viewModels<FileManagerViewModel>()
    private val path = Environment.getExternalStorageDirectory().path
    private var rootFolder: File = File(path)
    private var filesAndFolders: Array<File>? = null

    @Inject
    lateinit var selectAllFilesUseCase: SelectAllFilesUseCase
    private lateinit var binding: FragmentFileManagerBinding
    private lateinit var fileAdapter: FileManagerAdapter
    private var isFirstOpenApp by Delegates.notNull<Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFileManagerBinding.inflate(inflater, container, false)

        fileAdapter = FileManagerAdapter(this)

        permission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        val preferences = requireActivity()
            .getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)

        isFirstOpenApp = preferences.getBoolean(MainActivity.FIRST_OPEN_APP_PREFERENCE, true)
        if (isFirstOpenApp) {
            preferences.edit().putBoolean(MainActivity.FIRST_OPEN_APP_PREFERENCE, false).apply()
        }

        binding.providePermissionButton.setOnClickListener {
            permission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return binding.root
    }

    //  TODO: mb do smth else
    val permission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    with(binding) {
                        providePermissionButton.visibility = View.GONE

                        enableSortButton.setOnClickListener {
                            if (buttonPanelMotionLayout.currentState == R.id.start) {
                                buttonPanelMotionLayout.transitionToEnd()
                            } else {
                                buttonPanelMotionLayout.transitionToStart()
                            }
                        }

                        filesAndFolders = rootFolder.listFiles()

                        showModifiedFilesButton.setOnClickListener {
                            fileAdapter.differ.submitList(listOf())
                            configEmptyFolderViewVisibility(listOf())
                            if (isFirstOpenApp) {
                                emptyFolderView.emptyFolderTextView.text =
                                    getString(R.string.first_write_to_database)
                            } else {
                                emptyFolderView.emptyFolderTextView.text =
                                    getString(R.string.storage_still_reading)
                            }
                        }

                        showStorageFilesButton.setOnClickListener {
                            fileManagerViewModel.sortFilesBy(SortBy.FILENAME_ASC)
                            backButton.visibility = View.VISIBLE
                        }

                        readExternalStorage { list ->
                            showModifiedFilesButton.setOnClickListener {
                                fileAdapter.differ.submitList(list)
                                configEmptyFolderViewVisibility(list)
                                emptyFolderView.emptyFolderTextView.text =
                                    getString(R.string.no_modified_files)
                                backButton.visibility = View.GONE
                            }
                            showStorageFilesButton.setOnClickListener {
                                fileManagerViewModel.sortFilesBy(SortBy.FILENAME_ASC)
                                backButton.visibility = View.VISIBLE
                            }
                        }

                        fileManagerViewModel.parentPath.observe(viewLifecycleOwner) { parentPath ->
                            (requireActivity() as MainActivity).supportActionBar?.title = parentPath
                            configBackButton(parentPath)
                            sortByLinearLayout.configSorting()

                            val root = File(parentPath)
                            val filesAndFolders = root.listFiles()
                            fileManagerViewModel.setRootFiles(
                                filesAndFolders?.toList()?.sortedBy { it.name.lowercase() })

                            fileManagerViewModel.rootFiles.observe(viewLifecycleOwner) { files ->
                                if (files != null) {
                                    configEmptyFolderViewVisibility(files)
                                    fileAdapter.differ.submitList(files)
                                }
                            }

                            fileRecyclerView.adapter = fileAdapter
                        }
                    }
                }
                !shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    makeSnackbar(binding.buttonPanelMotionLayout, getString(R.string.reading_needed))
                }
                else -> {
                    binding.configEmptyFolderViewVisibility(listOf())
                    binding.providePermissionButton.visibility = View.VISIBLE
                }
            }
        }

    //minimal
    private fun readExternalStorage(onComplete: (List<File>) -> Unit) {
        if (isFirstOpenApp) {
            saveStorageFilesOnFirstOpenApp(onComplete)
        } else {
            checkModifiedFilesOnNotFirstOpenApp(onComplete)
        }
    }

    private fun checkModifiedFilesOnNotFirstOpenApp(onComplete: (List<File>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val databaseJob = async(Dispatchers.IO) {
                selectAllFilesUseCase.execute()
            }

            val externalDir = Environment.getExternalStorageDirectory()
            val fileList = mutableListOf<FileChecksum>()
            val preferences = requireActivity().getSharedPreferences(
                MainActivity.APP_PREFERENCES,
                Context.MODE_PRIVATE
            )

            val closedTime = preferences.getLong(
                MainActivity.CLOSE_APP_TIME_PREFERENCE,
                System.currentTimeMillis()
            )

            val databaseFiles = databaseJob.await()
            val modifiedFilesList: MutableList<File> = mutableListOf()
            readStorageForFindModifiedFiles(
                directory = externalDir,
                fileList = fileList,
                closedTime = closedTime,
                onFileFound = {
                    val last = fileList.last()
                    for (file in databaseFiles) {
                        if (file.path == last.path) {
                            if (file.checksum != last.checksum) {
                                modifiedFilesList.add(FileMapper.toFile(last))
                            }
                        }
                    }
                }
            )
            fileManagerViewModel.insertAll(fileList)
            withContext(Dispatchers.Main) {
                makeSnackbar(binding.buttonPanelMotionLayout, getString(R.string.reading_is_over))
                binding.emptyFolderView.emptyFolderTextView.text = getString(R.string.no_modified_files)
                onComplete(modifiedFilesList)
            }
        }
    }

    private fun apacheReadDirectory(directory: File, fileList: MutableList<File>) {
        val files = FileUtils.listFiles(directory, null, true)
        fileList.addAll(files)
    }

    private fun readStorageForFindModifiedFiles(
        directory: File,
        fileList: MutableList<FileChecksum>,
        onFileFound: () -> Unit,
        closedTime: Long
    ) {
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                if (file.lastModified() > closedTime) {
                    readStorageForFindModifiedFiles(file, fileList, onFileFound, closedTime)
                }
            } else {
                if (file.lastModified() > closedTime) {
                    fileList.add(FileChecksum(
                        path = file.path,
                        checksum = Md5Provider.getMd5Checksum(file.absolutePath)
                    ))
                    onFileFound()
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveStorageFilesOnFirstOpenApp(onComplete: (List<File>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val externalDir = Environment.getExternalStorageDirectory()
            val fileList = mutableListOf<File>()

            val readJob = launch(Dispatchers.IO) {
                apacheReadDirectory(externalDir, fileList)
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
                if (storageFile != null && storageFile.checksum == file.checksum) {
                    modifiedFilesList.add(FileMapper.toFile(file))
                }
            }
            fileManagerViewModel.insertAll(storageFilesList)
            withContext(Dispatchers.Main) {
                makeSnackbar(binding.buttonPanelMotionLayout, getString(R.string.reading_is_over))
                onComplete(modifiedFilesList)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        fileManagerViewModel.rootFiles.observe(viewLifecycleOwner) {
            fileAdapter.differ.submitList(it)
        }
    }

    private fun PartSortRadioGroupBinding.configSorting() {

        sortByDateAscButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.DATE_OF_CREATION_ASC)
        }
        sortByDateDescButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.DATE_OF_CREATION_DESC)
        }
        sortByExtensionAscButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.EXTENSION_ASC)
        }
        sortByExtensionDescButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.EXTENSION_DESC)
        }
        sortBySizeAscButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.SIZE_ASC)
        }
        sortBySizeDescButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.SIZE_DESC)
        }

    }

    //TODO: mb hide view
    private fun FragmentFileManagerBinding.configBackButton(parentPath: String) {
        backButton.setOnClickListener {
            if (parentPath != path) {
                val substringPath =
                    parentPath.substring(0, parentPath.lastIndexOf("/"))

                rootFolder = File(substringPath)
                filesAndFolders = rootFolder.listFiles()

                (requireActivity() as MainActivity).supportActionBar?.title =
                    substringPath

                fileManagerViewModel.setRootFiles(filesAndFolders?.toList())
                fileManagerViewModel.setParentPath(substringPath)
            } else {
                makeSnackbar(binding.buttonPanelMotionLayout, getString(R.string.top_level_remind))
            }
        }
    }

    private fun FragmentFileManagerBinding.configEmptyFolderViewVisibility(it: List<File>) {
        if (it == null || it.isEmpty()) {
            emptyFolderView.root.visibility = View.VISIBLE
            emptyFolderView.emptyFolderTextView.text = getString(R.string.empty_folder)
        } else {
            emptyFolderView.root.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "TAG"
    }
}