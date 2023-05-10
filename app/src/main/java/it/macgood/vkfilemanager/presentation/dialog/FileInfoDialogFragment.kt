package it.macgood.vkfilemanager.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import it.macgood.vkfilemanager.databinding.FragmentFileInfoDialogBinding
import it.macgood.vkfilemanager.presentation.utils.FileUtils.determineFileSize

class FileInfoDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentFileInfoDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val size = arguments?.getLong("size")
        val name = arguments?.getString("name")
        val dateOfCreation = arguments?.getString("dateOfCreation")

        binding = FragmentFileInfoDialogBinding.inflate(inflater, container, false)
        binding.fileNameTextView.text = name
        binding.fileSizeTextView.text = size?.determineFileSize()
        binding.fileDateOfCreationTextView.text = dateOfCreation.toString()

        return binding.root
    }
}