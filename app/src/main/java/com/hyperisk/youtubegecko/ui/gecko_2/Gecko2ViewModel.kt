package com.hyperisk.youtubegecko.ui.gecko_2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class Gecko2ViewModel : ViewModel() {

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is gecko 2 Fragment"
//    }
//    val text: LiveData<String> = _text

    companion object {
        fun readHTMLFromUTF8File(inputStream: InputStream): String {
            try {
                val bufferedReader = BufferedReader(InputStreamReader(inputStream, "utf-8"))

                var currentLine: String? = bufferedReader.readLine()
                val sb = StringBuilder()

                while (currentLine != null) {
                    sb.append(currentLine).append("\n")
                    currentLine = bufferedReader.readLine()
                }

                return sb.toString()
            } catch (e: Exception) {
                throw RuntimeException("Can't parse HTML file.")
            } finally {
                inputStream.close()
            }
        }
    }
}