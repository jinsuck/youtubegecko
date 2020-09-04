package com.hyperisk.youtubegecko.ui.gecko_1

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hyperisk.youtubegecko.R
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.Utils.readHTMLFromUTF8File
import kotlinx.android.synthetic.main.fragment_gecko_1_2.view.*
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException


class Gecko1Fragment2 : Fragment() {
    private val TAG = "Gecko1Fragment2"
    private lateinit var viewModel: Gecko1ViewModel
    private lateinit var session: GeckoSession
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gecko_1_2, container, false)

        view.text_using.text = TAG

        mainThreadHandler.postDelayed({
            initView()

            mainThreadHandler.postDelayed({
                loadHtml()
            }, 500)

        }, 500)


        return view
    }

    private fun initView() {
        val viewNotNull = view ?: return
        val geckoView: GeckoView = viewNotNull.findViewById(R.id.geckoview)
        session = GeckoSession()
        val settings = GeckoRuntimeSettings.Builder()
            .javaScriptEnabled(true)
            .consoleOutput(true)
            .debugLogging(true)
            .autoP // https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/permissions.html
            .build()
        var sRuntime = GeckoRuntime.create(requireContext(), settings)
        session.open(sRuntime)
        geckoView.setSession(session)

//        session.loadUri("https://mobile.twitter.com");
//        session.loadUri("https://www.youtube.com/watch?v=S0Q4gqBUs7c");
    }

    private fun loadHtml() {
        val htmlData = readHTMLFromUTF8File(
            resources.openRawResource(R.raw.ayp_youtube_player_gecko1)
        )
        val outputDir = requireContext().cacheDir // context being the Activity pointer
        var outputFile: File? = null
        try {
            outputFile = File.createTempFile("iframe_", ".html", outputDir)
            val path = outputFile.absolutePath
            val writer = BufferedWriter(FileWriter(outputFile))
            writer.write(htmlData)
            writer.flush()
            val fileUri = Uri.fromFile(outputFile).toString()
            session.loadUri(fileUri)
            outputFile.deleteOnExit()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}