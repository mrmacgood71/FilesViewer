package it.macgood.vkfilemanager.presentation.filemanager

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.macgood.vkfilemanager.R
import it.macgood.vkfilemanager.databinding.ItemFileBinding
import it.macgood.vkfilemanager.databinding.ItemLegendBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileManagerAdapter(
    // TODO: cringe
    private val fileManagerViewModel: FileManagerViewModel
): RecyclerView.Adapter<FileManagerAdapter.FileViewHolder>() {

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

        with(holder) {
            binding.fileNameTextView.text = file?.name
            if (file!!.isDirectory) {
                binding.fileIconImageView.setImageResource(R.drawable.ic_baseline_folder_24)
            } else {
                if (file.extension == "jpg" || file.extension == "png") {
                    binding.fileIconImageView.setImageURI(Uri.fromFile(file))
                } else {
                    binding.fileIconImageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24)
                }
            }

            binding.fileSizeTextView.text = (file.length() / 1024).toString() + " kb"
            val d = SimpleDateFormat("dd.MM.yyyy HH:mm").format(
                Date(file.lastModified())
            );
            binding.fileDateOfCreationTextView.text = d.toString()
            
            itemView.setOnClickListener {
                if (file.isDirectory) {
                    val root = File(file.absolutePath)
                    val filesAndFolders = root.listFiles()
                    fileManagerViewModel.setParentPath(file.path)

                    fileManagerViewModel.setRootFiles(filesAndFolders?.toList())

                } else {
                    try {
                        openFile(file, it)
                    } catch (e: Exception) {
                        Toast.makeText(it.context, "Cannot open the file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            itemView.setOnLongClickListener {
                binding.itemLayout.setBackgroundColor(Color.GRAY)
                true
            }
        }
    }


    override fun getItemCount(): Int = differ.currentList.size

    private fun openFile(file: File, it: View) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val mime = MimeTypeMap.getSingleton()
        val mimeType = mime.getMimeTypeFromExtension(file.extension)
        intent.setDataAndType(Uri.parse(file.path), mimeType)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        Intent.createChooser(intent, "Open with: ")
        it.context.startActivity(intent)
    }



    class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)
    class LegendViewHolder(val binding: ItemLegendBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TAG = "TAG"
    }
}