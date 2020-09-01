package com.hyperisk.youtubegecko.ui.gecko_2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Gecko2ViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gecko 2 Fragment"
    }
    val text: LiveData<String> = _text
}