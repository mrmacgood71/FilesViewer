package it.macgood.vkfilemanager.presentation.filemanager

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import it.macgood.core.fragment.BaseFragment
import it.macgood.vkfilemanager.data.model.FileChecksum
import it.macgood.vkfilemanager.databinding.FragmentFileManagerBinding
import it.macgood.vkfilemanager.presentation.MainActivity
import it.macgood.vkfilemanager.presentation.filemanager.adapter.FileManagerAdapter
import it.macgood.vkfilemanager.presentation.model.SortBy
import it.macgood.vkfilemanager.utils.Md5Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

@AndroidEntryPoint
class FileManagerFragment : BaseFragment() {

    private lateinit var binding: FragmentFileManagerBinding

    private val fileManagerViewModel by viewModels<FileManagerViewModel>()

    private val path = Environment.getExternalStorageDirectory().path
    private var root: File = File(path)
    private lateinit var fileAdapter: FileManagerAdapter

    private var filesAndFolders: Array<File>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFileManagerBinding.inflate(inflater, container, false)

        fileAdapter = FileManagerAdapter(fileManagerViewModel)

        permission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        fileManagerViewModel.setParentPath(path)

        return binding.root
    }

    //TODO: mb do smth else
    val permission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    filesAndFolders = root.listFiles()

                    readStorageFiles()

                    fileManagerViewModel.parentPath.observe(viewLifecycleOwner) { parentPath ->
                        (requireActivity() as MainActivity).supportActionBar?.title = parentPath
                        configBackButton(parentPath)
                    }

                    configSorting()

                    fileManagerViewModel.rootFiles.observe(viewLifecycleOwner) {
                        configEmptyFolderViewVisibility(it)
                        fileAdapter.differ.submitList(it)
                    }

                    fileManagerViewModel.selectAllFiles()

                    binding.fileRecyclerView.adapter = fileAdapter
                }
                //заново спросить насчёт permission
                !shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    Toast.makeText(requireContext(), "Reading is needed", Toast.LENGTH_SHORT).show()
                }
                else -> {

                }
            }
        }

    private fun readStorageFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("TAG", "storageFileList: ")
            readFilesInStorage(path)
            for (i in 0..5000) {
                storageFileList.forEach { file ->
                    writeToFile(file.path + ", " + file.checksum, requireContext())
                    Log.d(TAG, "storageFileList: ${file.path}, ${file.checksum}")
                }
            }
        }
    }

    private fun writeToFile(data: String, context: Context) {
        try {
            val outputStreamWriter =
                OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        fileManagerViewModel.rootFiles.observe(viewLifecycleOwner) {
            fileAdapter.differ.submitList(it)
        }
    }

    val storageFileList: MutableList<FileChecksum> = mutableListOf()
    private fun readFilesInStorage(root: String) {
        val file = File(root)
        val filesAndFolders = file.listFiles()
        if (filesAndFolders != null) {
            if (file.isDirectory) {
                filesAndFolders.forEach {
                    readFilesInStorage(it.absolutePath)
                }

            } else {
                storageFileList.add(
                    FileChecksum(
                        path = file.absolutePath,
                        checksum = Md5Provider.getMd5Checksum(file.absolutePath)
                    )
                )
            }
        } else {
            storageFileList.add(
                FileChecksum(
                    path = file.absolutePath,
                    checksum = Md5Provider.getMd5Checksum(file.absolutePath)
                )
            )
        }
    }

    private fun configSorting() {
        binding.sortByNameAscButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.FILENAME_ASC)
        }
        binding.sortByNameDescButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.FILENAME_DESC)
        }
        binding.sortByDateAscButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.DATE_OF_CREATION_ASC)
        }
        binding.sortByDateDescButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.DATE_OF_CREATION_DESC)
        }
        binding.sortByExtensionAscButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.EXTENSION_ASC)
        }
        binding.sortByExtensionDescButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.EXTENSION_DESC)
        }
        binding.sortBySizeAscButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.SIZE_ASC)
        }
        binding.sortBySizeDescButton.setOnClickListener {
            fileManagerViewModel.sortFilesBy(SortBy.SIZE_DESC)
        }

    }

    private fun configBackButton(parentPath: String) {
        binding.backButton.setOnClickListener {
            if (parentPath != path) {
                val substringPath =
                    parentPath.substring(0, parentPath.lastIndexOf("/"))

                root = File(substringPath)
                filesAndFolders = root.listFiles()

                (requireActivity() as MainActivity).supportActionBar?.title =
                    substringPath

                fileManagerViewModel.setRootFiles(filesAndFolders?.toList())
                fileManagerViewModel.setParentPath(substringPath)
            } else {
                makeToast("You are at the top level")
            }
        }
    }

    private fun configEmptyFolderViewVisibility(it: List<File>) {
        if (it == null || it.isEmpty()) {
            binding.emptyFolderView.root.visibility = View.VISIBLE
        } else {
            binding.emptyFolderView.root.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "TAG"
    }
}