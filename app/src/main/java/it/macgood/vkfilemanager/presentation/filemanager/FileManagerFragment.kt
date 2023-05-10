package it.macgood.vkfilemanager.presentation.filemanager

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import it.macgood.core.fragment.BaseFragment
import it.macgood.vkfilemanager.R
import it.macgood.vkfilemanager.databinding.FragmentFileManagerBinding
import it.macgood.vkfilemanager.databinding.PartSortRadioGroupBinding
import it.macgood.vkfilemanager.presentation.MainActivity
import it.macgood.vkfilemanager.presentation.filemanager.adapter.FileManagerAdapter
import it.macgood.domain.model.SortBy
import java.io.File
import kotlin.properties.Delegates

@AndroidEntryPoint
class FileManagerFragment : BaseFragment() {

    val fileManagerViewModel by viewModels<FileManagerViewModel>()

    private val path = Environment.getExternalStorageDirectory().path
    private var rootFolder: File = File(path)
    private var filesAndFolders: Array<File>? = null

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

    private val permission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    with(binding) {
                        var titlePath: String = path
                        providePermissionButton.visibility = View.GONE
                        backButton.visibility = View.VISIBLE

                        configEnableSortButton()

                        filesAndFolders = rootFolder.listFiles()

                        configModifiedFilesButton()

                        readExternalStorage { list ->
                            makeSnackbar(
                                binding.buttonPanelMotionLayout,
                                getString(R.string.reading_is_over)
                            )
                        }
                        fileManagerViewModel.parentPath.observe(viewLifecycleOwner) { parentPath ->
                            (requireActivity() as MainActivity).supportActionBar?.title = parentPath
                            titlePath = parentPath
                            configBackButton(parentPath)
                            sortByLinearLayout.configSorting()
                            configShowingStorage(titlePath)

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
                    makeSnackbar(
                        binding.buttonPanelMotionLayout,
                        getString(R.string.reading_needed)
                    )
                }
                else -> {
                    binding.configShowingNotGrantedPermissionView()
                }
            }
        }

    override fun onResume() {
        super.onResume()
        fileManagerViewModel.parentPath.observe(viewLifecycleOwner) {
            binding.configShowingStorage(it)
        }
    }

    private fun FragmentFileManagerBinding.configModifiedFilesButton() {

        showModifiedFilesButton.setOnClickListener {
            configEmptyFolderViewVisibility(listOf())
            fileAdapter.differ.submitList(listOf())
            emptyFolderView.emptyFolderTextView.text =
                getString(R.string.reading_is_not_over)
            enableSortButton.alpha = 0.5f
            enableSortButton.setOnClickListener { null }
        }

        fileManagerViewModel.modifiedList.observe(viewLifecycleOwner) { list ->
            if (isFirstOpenApp) {

                showModifiedFilesButton.setOnClickListener { null }
                showModifiedFilesButton.setOnClickListener {
                    configEmptyFolderViewVisibility(listOf())
                    fileAdapter.differ.submitList(listOf())
                    emptyFolderView.emptyFolderTextView.text =
                        getString(R.string.first_write_to_database)
                    enableSortButton.alpha = 0.5f
                    enableSortButton.setOnClickListener { null }
                }
            } else {
                showModifiedFilesButton.setOnClickListener {
                    (requireActivity() as MainActivity).supportActionBar?.title =
                        getString(R.string.modified_files)
                    fileAdapter.differ.submitList(list)
                    configEmptyFolderViewVisibility(list)
                    emptyFolderView.emptyFolderTextView.text =
                        getString(R.string.no_modified_files)
                    enableSortButton.alpha = 0.5f
                    enableSortButton.setOnClickListener { null }
                    backButton.visibility = View.GONE
                }
            }
        }
    }

    private fun FragmentFileManagerBinding.configShowingNotGrantedPermissionView() {
        configEmptyFolderViewVisibility(listOf())
        emptyFolderView.emptyFolderTextView.text =
            getString(R.string.please_allow_permission)
        providePermissionButton.visibility = View.VISIBLE
        backButton.visibility = View.GONE
        fileManagerViewModel.setIsReadingIsWorking(false)
    }

    private fun FragmentFileManagerBinding.configShowingStorage(
        titlePath: String
    ) {
        showStorageFilesButton.setOnClickListener {
            (requireActivity() as MainActivity).supportActionBar?.title =
                titlePath
            fileManagerViewModel.sortFilesBy(SortBy.FILENAME_ASC)
            backButton.visibility = View.VISIBLE
            enableSortButton.alpha = 1f
            configEnableSortButton()
        }
    }

    private fun FragmentFileManagerBinding.configEnableSortButton() {
        enableSortButton.setOnClickListener {
            if (buttonPanelMotionLayout.currentState == R.id.start) {
                buttonPanelMotionLayout.transitionToEnd()
            } else {
                buttonPanelMotionLayout.transitionToStart()
            }
        }
    }

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

    private fun readExternalStorage(onComplete: (List<File>) -> Unit) {
        fileManagerViewModel.isReadingIsWorking.observe(viewLifecycleOwner) {
            if (!it) {
                if (isFirstOpenApp) {
                    fileManagerViewModel.saveStorageFilesOnFirstOpenApp(onComplete, rootFolder)
                } else {
                    val preferences = requireActivity().getSharedPreferences(
                        MainActivity.APP_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                    val closedTime = preferences.getLong(
                        MainActivity.CLOSE_APP_TIME_PREFERENCE,
                    0L
                    )
                    fileManagerViewModel.checkModifiedFilesOnNotFirstOpenApp(
                        onComplete,
                        rootFolder,
                        closedTime
                    )
                }
                fileManagerViewModel.setIsReadingIsWorking(true)
            }
        }
    }

}