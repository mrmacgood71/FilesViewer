package it.macgood.vkfilemanager.presentation.filemanager

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import it.macgood.core.fragment.BaseFragment
import it.macgood.vkfilemanager.databinding.FragmentFileManagerBinding
import it.macgood.vkfilemanager.domain.usecase.SelectAllFilesUseCase
import it.macgood.vkfilemanager.presentation.MainActivity
import it.macgood.vkfilemanager.presentation.filemanager.adapter.FileManagerAdapter
import it.macgood.vkfilemanager.presentation.filemanager.mapper.FileMapper
import it.macgood.vkfilemanager.presentation.filemanager.model.SortBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                    binding.providePermissionButton.visibility = View.GONE
                    filesAndFolders = rootFolder.listFiles()
                    with(binding) {
                        readExternalStorage { list ->
                            binding.showModifiedFilesButton?.text = "Modified"
                            binding.showModifiedFilesButton?.setOnClickListener {
                                fileAdapter.differ.submitList(list)
                            }
                            binding.showStorageFilesButton?.setOnClickListener {
                                fileManagerViewModel.sortFilesBy(SortBy.FILENAME_ASC)
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

                            binding.fileRecyclerView.adapter = fileAdapter
                        }
                    }
                }
                //заново спросить насчёт permission
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
            readDirectory(externalDir, fileList)

            val databaseFilesList = selectAllFilesUseCase.execute()
            databaseFilesList.forEach { Log.d(TAG, "readExternalStorage: ${it}") }
            fileList.forEach { Log.d(TAG, "readExternalStorage: ${it}") }
            val storageFilesList = FileMapper.toFileChecksum(fileList)
            val modifiedFilesList: MutableList<File> = mutableListOf()

            databaseFilesList.forEach { file ->
                val find = storageFilesList.find { file.path == it.path }
                if (find != null) {
                    if (find.checksum == file.checksum) {
                        withContext(Dispatchers.Main) {
                            modifiedFilesList.add(FileMapper.toFile(file))
//                            fileAdapter.differ.submitList(modifiedFilesList.toList())
                        }
                        Log.d(TAG, "RES: not mod ${find} + ${file}")
                    } else {
                        Log.d(TAG, "RES: moded ${find} + ${file}")
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

    //TODO: Filechecksum
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
        } else {
            emptyFolderView.root.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "TAG"
    }
}