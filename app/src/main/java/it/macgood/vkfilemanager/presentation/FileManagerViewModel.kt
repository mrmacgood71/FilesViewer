package it.macgood.vkfilemanager.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FileManagerViewModel : ViewModel() {

    val parentPath: MutableLiveData<String> = MutableLiveData()

}