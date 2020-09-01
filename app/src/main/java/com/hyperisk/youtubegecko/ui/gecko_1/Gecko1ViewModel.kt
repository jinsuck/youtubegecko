package com.hyperisk.youtubegecko.ui.gecko_1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Gecko1ViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gecko 1 Fragment"
    }
    val text: LiveData<String> = _text
}