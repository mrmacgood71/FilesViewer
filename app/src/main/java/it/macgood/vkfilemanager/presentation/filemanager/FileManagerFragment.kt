package it.macgood.vkfilemanager.presentation.filemanager

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import it.macgood.core.fragment.BaseFragment
import it.macgood.vkfilemanager.databinding.FragmentFileManagerBinding
import it.macgood.vkfilemanager.presentation.MainActivity
import it.macgood.vkfilemanager.presentation.model.SortBy
import java.io.File

class FileManagerFragment : BaseFragment() {

    private lateinit var binding: FragmentFileManagerBinding

    private val fileManagerViewModel by viewModels<FileManagerViewModel>()

    private val path = Environment.getExternalStorageDirectory().path
    private var root: File = File(path)
    private lateinit var fileAdapter: FileManagerAdapter
    //TODO: view stub if no files
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
    val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when {
            granted -> {
                filesAndFolders = root.listFiles()


                fileManagerViewModel.parentPath.observe(viewLifecycleOwner) { parentPath ->

                    (requireActivity() as MainActivity).supportActionBar?.title = parentPath

                    binding.backButton.setOnClickListener {

                        if (parentPath != path) {
                            val substringPath = parentPath.substring(0, parentPath.lastIndexOf("/"))

                            root = File(substringPath)
                            filesAndFolders = root.listFiles()

                            (requireActivity() as MainActivity).supportActionBar?.title = substringPath

                            fileManagerViewModel.setRootFiles(filesAndFolders?.toList())
                            fileManagerViewModel.setParentPath(substringPath)
                        } else {
                            makeToast("You are at the top level")
                        }
                    }
                }

                configSorting()

                fileManagerViewModel.rootFiles.observe(viewLifecycleOwner) {

                    if (it == null || it.isEmpty()) {
                        binding.emptyFolderView.root.visibility = View.VISIBLE
                    } else {
                        binding.emptyFolderView.root.visibility = View.GONE
                    }
                    fileAdapter.differ.submitList(it)
                }

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

    override fun onResume() {
        super.onResume()
        fileManagerViewModel.rootFiles.observe(viewLifecycleOwner) {
            fileAdapter.differ.submitList(it)
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


    companion object {
        const val TAG = "TAG"
    }
}