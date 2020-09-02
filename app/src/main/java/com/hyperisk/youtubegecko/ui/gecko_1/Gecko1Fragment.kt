package com.hyperisk.youtubegecko.ui.gecko_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.hyperisk.youtubegecko.R

class Gecko1Fragment : Fragment() {

    private lateinit var viewModel: Gecko1ViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel =
                ViewModelProviders.of(this).get(Gecko1ViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gecko_1, container, false)
        return root
    }
}