package com.hyperisk.youtubegecko.ui.gecko_1

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.hyperisk.youtubegecko.R
import com.hyperisk.youtubegecko.ui.AutoplayPermissionDelegate
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.Utils
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.Utils.readHTMLFromUTF8File
import kotlinx.android.synthetic.main.fragment_gecko_1_2.view.*
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.PermissionDelegate
import org.mozilla.geckoview.GeckoSession.PermissionDelegate.*
import org.mozilla.geckoview.GeckoView
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.RuntimeException


class Gecko1Fragment : Fragment() {
    private val TAG = "Gecko1Fragment"
    private lateinit var viewModel: Gecko1ViewModel
    private lateinit var session: GeckoSession
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gecko_1_2, container, false)

        view.text_using.text = "GeckoView, autoPlay, no messaging"
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        loadHtml()
    }

    private fun initView() {
        Log.i(TAG, "initView")
        val viewNotNull = view ?: return
        val geckoView: GeckoView = viewNotNull.findViewById(R.id.geckoview)
        session = GeckoSession()
        session.permissionDelegate = AutoplayPermissionDelegate()

        val settings = GeckoRuntimeSettings.Builder()
            .javaScriptEnabled(true)
            .consoleOutput(true)
            .debugLogging(true)
            .build()
        var sRuntime = GeckoRuntime.create(requireContext(), settings)
        session.open(sRuntime)
        geckoView.setSession(session)

//        session.loadUri("https://mobile.twitter.com");
//        session.loadUri("https://www.youtube.com/watch?v=S0Q4gqBUs7c");
    }

    private fun loadHtml() {
        Log.i(TAG, "initHtml")
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


    private fun loaadTestWebView() {
        val viewNotNull = view ?: return
        val webView: WebView = viewNotNull.findViewById(R.id.youtube_webview)
        webView.settings.javaScriptEnabled=true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
//        webView.loadUrl("https://mobile.twitter.com")
//        webView.loadUrl("https://www.youtube.com/watch?v=S0Q4gqBUs7c");

        val htmlPage = Utils
            .readHTMLFromUTF8File(resources.openRawResource(R.raw.ayp_youtube_player_gecko1))
//        webView.loadData(htmlPage, "text/html", "utf-8")
        webView.loadDataWithBaseURL("https://www.youtube.com", htmlPage, "text/html", "utf-8", null)
    }
}