package com.hyperisk.youtubegecko.ui.gecko_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.hyperisk.youtubegecko.R

class Gecko2Fragment : Fragment() {

    private lateinit var viewModel: Gecko2ViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel =
                ViewModelProviders.of(this).get(Gecko2ViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gecko_2, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
//        viewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }
}