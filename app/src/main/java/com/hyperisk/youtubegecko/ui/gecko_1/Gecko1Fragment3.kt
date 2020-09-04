package com.hyperisk.youtubegecko.ui.gecko_1

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.hyperisk.youtubegecko.R
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.Utils
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


class Gecko1Fragment3 : Fragment() {
    private val TAG = "Gecko1Fragment2"
    private lateinit var viewModel: Gecko1ViewModel
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gecko_1_3, container, false)

        mainThreadHandler.postDelayed({
            loaad()
        }, 1000)

        return view
    }

    private fun loaad() {
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