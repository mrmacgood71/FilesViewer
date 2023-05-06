package it.macgood.vkfilemanager.presentation

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import it.macgood.vkfilemanager.databinding.FragmentFileManagerBinding
import java.io.File

class FileManagerFragment : Fragment() {

    private lateinit var binding: FragmentFileManagerBinding

    private val fileManagerViewModel by viewModels<FileManagerViewModel>()

    private val path = Environment.getExternalStorageDirectory().path
    private var root: File = File(path)
    private var filesAndFolders: Array<File>? = null

    val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when {
            granted -> {
                filesAndFolders = root.listFiles()
                val fileAdapter = FileManagerAdapter()
                fileAdapter.differ.submitList(filesAndFolders?.toList())
                binding.fileRecyclerView.adapter = fileAdapter
            }
            !shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                Toast.makeText(requireContext(), "Reading is needed", Toast.LENGTH_SHORT).show()
            }
            else -> {

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFileManagerBinding.inflate(inflater, container, false)


        permission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        fileManagerViewModel.parentPath.postValue(path)

        Log.d(TAG, "onCreateView: ${fileManagerViewModel.parentPath.value}")


        val fileAdapter = FileManagerAdapter()
        fileAdapter.differ.submitList(filesAndFolders?.toList())
        Log.d(TAG, "onCreateView: ${fileAdapter.itemCount}")
        binding.fileRecyclerView.adapter = fileAdapter
        fileManagerViewModel.parentPath.observe(viewLifecycleOwner) { parentPath ->
            binding.pathTextView.text = parentPath
            binding.backButton.setOnClickListener {
                Log.d(TAG, "onCreateView: ${fileAdapter.itemCount}")
                root = File(parentPath)
                filesAndFolders = root.listFiles()
                fileAdapter.differ.submitList(root.listFiles()?.toList())
            }
        }


        return binding.root
    }

    companion object {
        const val TAG = "TAG"
    }
}