package com.hyperisk.youtubegecko.ui.gecko_1

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.hyperisk.youtubegecko.R
import kotlinx.android.synthetic.main.fragment_gecko_2.view.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class WebView1Fragment : Fragment() {
    private val TAG = "WebView1Fragment"
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
    private var bitmapCopy: Bitmap? = null
    private var copySrcRect: Rect? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        WebView.enableSlowWholeDocumentDraw()

        val view = inflater.inflate(R.layout.fragment_gecko_1_3, container, false)

        //val webView: WebView = view.findViewById(R.id.youtube_webview)
        //webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        view.show_hide_button.setOnClickListener {
            val webView: WebView = view.findViewById(R.id.youtube_webview)
            webView.visibility = if (webView.visibility == View.VISIBLE)
                View.INVISIBLE else View.VISIBLE
        }
        return view
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)
        loadHtml(view1)

        mainThreadHandler.postDelayed({
            val webView: WebView = view?.findViewById(R.id.youtube_webview) ?: return@postDelayed
            screenshot1(webView)
        }, 4000)
    }


    private fun loadHtml(view: View) {
        Log.i(TAG, "loadHtml")
        val webView: WebView = view.findViewById(R.id.youtube_webview)
        webView.settings.javaScriptEnabled=true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        //        webView.loadUrl("https://mobile.twitter.com")
        //        webView.loadUrl("https://www.youtube.com/watch?v=S0Q4gqBUs7c");

        val htmlPage = readHTMLFromUTF8File(resources.openRawResource(R.raw.ayp_youtube_player_gecko1))
        webView.loadDataWithBaseURL("https://www.youtube.com", htmlPage, "text/html", "utf-8", null)
    }

    // captures only HTML elements, and video is black
    // https://stackoverflow.com/questions/20900196/how-to-capture-a-webview-to-bitmap-in-android
    private fun screenshot1(webView: WebView) {
        //Log.d(TAG, "screenshot start ${webView.width}, ${webView.contentHeight}")
        val bitmapCopyNotNull: Bitmap = bitmapCopy ?: run {
            val bitmapNew = Bitmap.createBitmap(
                webView.width,
                webView.height,
                Bitmap.Config.ARGB_8888
            )
            bitmapCopy = bitmapNew
            bitmapNew
        }
        val canvas = Canvas(bitmapCopyNotNull)
        webView.draw(canvas)

        val viewNotNull = view ?: return
        viewNotNull.image_copy_pixel.setImageBitmap(bitmapCopyNotNull)
        //Log.d(TAG, "screenshot done ${bitmapCopyNotNull.width}, ${bitmapCopyNotNull.height}")

        mainThreadHandler.postDelayed({
            screenshot1(webView)
        }, 100)
    }

    // does not capture any if the view is hidden
    fun screenshot2(webView: WebView) {
        val w = activity?.window ?: return
        val bitmapCopyNotNull: Bitmap = bitmapCopy ?: run {
            val bitmapNew = Bitmap.createBitmap(
                webView.width,
                webView.height,
                Bitmap.Config.ARGB_8888
            )
            bitmapCopy = bitmapNew
            bitmapNew
        }
        val rectNotNull: Rect = copySrcRect ?: run {
            val rectNew = Rect(0, 0, webView.width, webView.height)
            copySrcRect = rectNew
            rectNew
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            PixelCopy.request(w, rectNotNull, bitmapCopyNotNull, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    val viewNotNull = view ?: return@request
                    viewNotNull.image_copy_pixel.setImageBitmap(bitmapCopyNotNull)

                    mainThreadHandler.postDelayed({
                        screenshot2(webView)
                    }, 100)
                }
            }, mainThreadHandler)
        }
    }

    // https://stackoverflow.com/questions/29767866/android-capture-youtube-movie-on-webview
    fun screenshot3() {

    }

    private fun readHTMLFromUTF8File(inputStream: InputStream): String {
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