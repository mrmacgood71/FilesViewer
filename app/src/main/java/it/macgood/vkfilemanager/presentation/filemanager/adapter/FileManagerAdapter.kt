package it.macgood.vkfilemanager.presentation.filemanager.adapter

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.macgood.vkfilemanager.R
import it.macgood.vkfilemanager.databinding.ItemFileBinding
import it.macgood.vkfilemanager.databinding.ItemLegendBinding
import it.macgood.vkfilemanager.presentation.filemanager.FileManagerFragment
import it.macgood.vkfilemanager.presentation.utils.FileUtils
import it.macgood.vkfilemanager.presentation.utils.FileUtils.convertTime
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

class FileManagerAdapter(
    // TODO: 1/2 cringe
    private val parentFragment: FileManagerFragment
) : RecyclerView.Adapter<FileManagerAdapter.FileViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<File>() {

        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return FileViewHolder(ItemFileBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = differ.currentList[position]

        with(holder.binding) {
            fileNameTextView.text = file?.name
            configFilesIcons(file)
            openFileInfo(file)
        }
        holder.itemView.setOnClickListener {
            configItemClick(file, it)
        }
        holder.itemView.setOnLongClickListener {
            sendFile(file, it)
            true
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    private fun ItemFileBinding.configFilesIcons(file: File) {
        if (file.isDirectory) {
            fileIconImageView.setImageResource(R.drawable.ic_baseline_folder_24)
        } else {
            if (file.extension == "jpg" || file.extension == "png") {
                fileIconImageView.setImageURI(Uri.fromFile(file))
            } else {
                fileIconImageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24)
            }
        }
    }

    private fun ItemFileBinding.openFileInfo(
        file: File
    ) {
        fileInfoButton.setOnClickListener {
            it.findNavController()
                .navigate(
                    R.id.action_fileManagerFragment_to_fileInfoDialogFragment,
                    bundleOf(
                        "name" to file.name,
                        "size" to FileUtils.countFileSize(file),
                        "dateOfCreation" to file.lastModified().convertTime()
                    )
                )
        }
    }

    private fun configItemClick(file: File, it: View) {
        if (file.isDirectory) {
            val root = File(file.absolutePath)
            val filesAndFolders = root.listFiles()

            parentFragment.fileManagerViewModel.setParentPath(file.path)
            parentFragment.fileManagerViewModel.setRootFiles(filesAndFolders?.toList())

        } else {
            try {
                openFile(file, it)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(it.context, "Cannot open the file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFile(file: File, it: View) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

        val fileUri = FileProvider.getUriForFile(
            it.context,
            it.context.applicationContext.packageName + ".provider",
            file
        )
        intent.setDataAndType(fileUri, mimeType)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        Intent.createChooser(intent, it.context.getString(R.string.open_with))
        it.context.startActivity(intent)
    }

    private fun sendFile(file: File, it: View) {
        val fileUri = FileProvider.getUriForFile(
            it.context,
            it.context.applicationContext.packageName + ".provider",
            file
        )
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
        val intent = Intent()

        intent.action = Intent.ACTION_SEND
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        intent.setDataAndType(fileUri, mimeType)
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        Intent.createChooser(intent, it.context.getString(R.string.open_with))

        it.context.startActivity(intent)
    }

    class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)
    class LegendViewHolder(val binding: ItemLegendBinding) : RecyclerView.ViewHolder(binding.root)

}