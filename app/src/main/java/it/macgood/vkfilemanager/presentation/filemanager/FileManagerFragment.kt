package it.macgood.vkfilemanager.presentation.filemanager

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.common.io.Files
import com.google.common.io.Files.isFile
import dagger.hilt.android.AndroidEntryPoint
import it.macgood.core.fragment.BaseFragment
import it.macgood.vkfilemanager.databinding.FragmentFileManagerBinding
import it.macgood.vkfilemanager.domain.usecase.SelectAllFilesUseCase
import it.macgood.vkfilemanager.presentation.MainActivity
import it.macgood.vkfilemanager.presentation.filemanager.adapter.FileManagerAdapter
import it.macgood.vkfilemanager.presentation.filemanager.mapper.FileMapper
import it.macgood.vkfilemanager.presentation.filemanager.model.SortBy
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class FileManagerFragment : BaseFragment() {

    private lateinit var binding: FragmentFileManagerBinding

    val fileManagerViewModel by viewModels<FileManagerViewModel>()

    @Inject
    lateinit var selectAllFilesUseCase: SelectAllFilesUseCase

    private val path = Environment.getExternalStorageDirectory().path
    private var rootFolder: File = File(path)
    private lateinit var fileAdapter: FileManagerAdapter

    private var filesAndFolders: Array<File>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFileManagerBinding.inflate(inflater, container, false)

        fileAdapter = FileManagerAdapter(this)

        permission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

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
                        filesAndFolders = rootFolder.listFiles()

                        readExternalStorage { list ->
                            showModifiedFilesButton?.text = "Modified"
                            showModifiedFilesButton?.setOnClickListener {
                                fileAdapter.differ.submitList(list)
                                configEmptyFolderViewVisibility(list)
                                binding.emptyFolderView.emptyFolderTextView.text = "No modified files"
                                binding.backButton.visibility = View.GONE
                            }
                            showStorageFilesButton?.setOnClickListener {
                                fileManagerViewModel.sortFilesBy(SortBy.FILENAME_ASC)
                                binding.backButton.visibility = View.VISIBLE
                            }
                        }
                        fileManagerViewModel.parentPath.observe(viewLifecycleOwner) { parentPath ->
                            (requireActivity() as MainActivity).supportActionBar?.title = parentPath
                            configBackButton(parentPath)
                            configSorting()
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
                    makeToast("Reading is needed")
                }
                else -> {
                    binding.configEmptyFolderViewVisibility(listOf())
                    binding.providePermissionButton.visibility = View.VISIBLE
                }
            }
        }

    //minimal
    fun readExternalStorage(onComplete: (List<File>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val externalDir = Environment.getExternalStorageDirectory()
            val fileList = mutableListOf<File>()

            val readJob = launch(Dispatchers.IO) {
                val start = System.currentTimeMillis()
                apacheReadDirectory(externalDir, fileList)
                val end = System.currentTimeMillis()
                Log.d(TAG, "readExternalStorage: ${end - start}")
            }

            val databaseJob = async(Dispatchers.IO) {
                selectAllFilesUseCase.execute()
            }
            readJob.join()
            val storageFilesList = FileMapper.toFileChecksum(fileList)
            val modifiedFilesList: MutableList<File> = mutableListOf()
            val databaseFilesList = databaseJob.await()
            for (file in databaseFilesList.toList()) {
                val find = storageFilesList.find { it.path == file.path }
                if (find != null) {
                    if (find.checksum == file.checksum) {

                    } else {
                        modifiedFilesList.add(FileMapper.toFile(file))
                    }
                }
            }
            fileManagerViewModel.insertAll(storageFilesList)
            withContext(Dispatchers.Main) {
                makeToast("rdy")
                onComplete(modifiedFilesList)
            }
        }
    }

    fun apacheReadDirectory(directory: File, fileList: MutableList<File>) {
        val files = FileUtils.listFiles(directory, null, true)
        fileList.addAll(files)
    }

//    fun guavaReadDirectory(directory: File, fileList: MutableList<File>) {
//        val files = Files.fileTreeTraverser().preOrderTraversal(directory).filter { it.isFile }
//        fileList.addAll(files)
//    }

    fun readDirectory(directory: File, fileList: MutableList<File>) {
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                readDirectory(file, fileList)
            } else {
                fileList.add(file)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fileManagerViewModel.rootFiles.observe(viewLifecycleOwner) {
            fileAdapter.differ.submitList(it)
        }
    }

    private fun FragmentFileManagerBinding.configSorting() {
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
                makeToast("You are at the top level")
            }
        }
    }

    private fun FragmentFileManagerBinding.configEmptyFolderViewVisibility(it: List<File>) {
        if (it == null || it.isEmpty()) {
            emptyFolderView.root.visibility = View.VISIBLE
            emptyFolderView.emptyFolderTextView.text = "This folder is empty :("
        } else {
            emptyFolderView.root.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "TAG"
    }
}