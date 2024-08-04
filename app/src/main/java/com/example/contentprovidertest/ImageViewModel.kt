package com.example.contentprovidertest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    var images = MutableLiveData<MutableList<Image>>()
        private set

    fun updateImages(newImages: MutableList<Image>) {
        images.value = newImages
    }
}
