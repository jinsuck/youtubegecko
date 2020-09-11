package com.hyperisk.youtubegecko.ui.gecko_1

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import com.hyperisk.youtubegecko.R
import kotlinx.android.synthetic.main.fragment_gecko_1_3.view.*
import kotlinx.android.synthetic.main.fragment_gecko_2.view.image_copy_pixel
import kotlinx.android.synthetic.main.fragment_gecko_2.view.show_hide_button
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference

private val TAG = "WebView1Fragment"

class WebView1Fragment : Fragment() {
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
    private var bitmapCopy: Bitmap? = null
    private var testDialog: TestDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gecko_1_3, container, false)
        view.show_hide_button.setOnClickListener {
            val webView: WebView = view.findViewById(R.id.youtube_webview)
            webView.visibility = if (webView.visibility == View.VISIBLE)
                View.INVISIBLE else View.VISIBLE
        }
        view.show_dialog_button.setOnClickListener {
            view.show_dialog_button.isEnabled = false
            showAlertDialog(view)
        }
        return view
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)
        //initShot3(view1)

        // does not work in OS 8 or below (not our problem)
        // https://stackoverflow.com/questions/40145667/android-console-displays-w-art-attempt-to-remove-non-jni-local-reference

        //loadHtml(view1.findViewById(R.id.youtube_webview), resources.openRawResource(R.raw.ayp_youtube_player_gecko1))
        //mainThreadHandler.postDelayed({
        //    Log.d(TAG, "onViewCreated postDelayed screenshot2")
        //    val webView: WebView = view?.findViewById(R.id.youtube_webview) ?: return@postDelayed
        //    screenshot2(webView, activity?.window!!, 300)
        //}, 4000)
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
        //clearShot3()
        testDialog?.dismiss()
        testDialog = null
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
        //if (mCustomView != null) {
        //    val view1Child = (mCustomView as FrameLayout).children.asIterable().first()
        //    if (view1Child is Surface) Log.i(TAG, "view1Child is Surface")
        //    else if (view1Child is SurfaceView) Log.i(TAG, "view1Child is SurfaceView")
        //    else if (view1Child is VideoView) Log.i(TAG, "view1Child is VideoView")
        //    else if (view1Child is VideoDecoderGLSurfaceView) Log.i(
        //        TAG,
        //        "view1Child is VideoDecoderGLSurfaceView"
        //    )
        //    else if (view1Child is FrameLayout) {
        //        val view1ChildFocused = view1Child.focusedChild
        //        // does not work http://www.java2s.com/Open-Source/Android_Free_Code/Video/webview/name_cprVideoEnabledWebChromeClient_java.htm
        //        // ??? https://source.chromium.org/chromium/chromium/src/+/master:android_webview/java/src/org/chromium/android_webview/FullScreenView.java
        //        Log.i(TAG, "view1Child is FrameLayout, view1ChildFocused: $view1ChildFocused")
        //    }
        //    else Log.i(TAG, "view1Child is ... ${view1Child.accessibilityClassName}")
        //
        //    val bm = view1Child.drawToBitmap()
        //    val viewNotNull = view ?: return
        //    viewNotNull.image_copy_pixel.setImageBitmap(bm)
        //} else {
            webView.draw(canvas)
            val viewNotNull = view ?: return
            viewNotNull.image_copy_pixel.setImageBitmap(bitmapCopyNotNull)
            //Log.d(TAG, "screenshot done ${bitmapCopyNotNull.width}, ${bitmapCopyNotNull.height}")
        //}

        mainThreadHandler.postDelayed({
            screenshot1(webView)
        }, 1000)
    }

    private var screenShotStartTime = 0L
    private var numScreenshots = 0

    fun screenshot2(webView: WebView, window: Window, toolbarHeight: Int) {
        //if (!window.isActive) return
        if (testDialog == null || !isAdded) return
        val bitmapCopyNotNull: Bitmap = bitmapCopy ?: run {
            val bitmapNew = Bitmap.createBitmap(
                webView.measuredWidth,
                webView.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmapCopy = bitmapNew
            bitmapNew
        }
//        val rectNotNull: Rect = copySrcRect ?: run {
//            val rectNew = Rect(0, toolbarHeight, webView.measuredWidth, webView.measuredHeight + toolbarHeight)
//            copySrcRect = rectNew
//            rectNew
//        }

        //Log.d(TAG, "window size ${window.decorView.measuredWidth} x ${window.decorView.measuredHeight}")
        val rectNotNull = Rect(0, toolbarHeight, window.decorView.measuredWidth, toolbarHeight + window.decorView.measuredHeight)
        val beforePixelCopy = System.currentTimeMillis()
        if (screenShotStartTime == 0L) screenShotStartTime = beforePixelCopy
        //Log.d(TAG, "screenshot2 rectNotNull $rectNotNull, elapsed from first screenshot: ${beforePixelCopy - screenShotStartTime}ms")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            PixelCopy.request(window, rectNotNull, bitmapCopyNotNull, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    val viewNotNull = view ?: return@request
                    viewNotNull.image_copy_pixel.setImageBitmap(bitmapCopyNotNull)

                    val afterPixelCopy = System.currentTimeMillis()
                    numScreenshots++
                    val screenShotElapsed = (afterPixelCopy - screenShotStartTime).toFloat() / 1000f
                    Log.d(TAG, "PixelCopy elapsed: ${afterPixelCopy - beforePixelCopy}ms, ${numScreenshots / screenShotElapsed} fps average")
                    mainThreadHandler.postDelayed({
                        screenshot2(webView, window, toolbarHeight)
                    }, 33)
                }
            }, mainThreadHandler)
        }
    }

    // https://stackoverflow.com/questions/29767866/android-capture-youtube-movie-on-webview
    fun screenshot3() {

    }

    //private var customViewContainer: FrameLayout? = null
    //private var customViewCallback: CustomViewCallback? = null
    //private var mCustomView: View? = null
    //private var mWebChromeClient: MyWebChromeClient? = null
    //private var mWebViewClient: myWebViewClient? = null

    //private fun initShot3(view: View) {
    //    val webView: ExtendedWebView? = view.findViewById(R.id.youtube_webview)
    //    webView!!.settings.setBuiltInZoomControls(true)
    //    mWebViewClient = myWebViewClient()
    //    webView.setWebViewClient(mWebViewClient)
    //
    //    mWebChromeClient = MyWebChromeClient()
    //    webView.setWebChromeClient(mWebChromeClient)
    //
    //    customViewContainer = view?.findViewById(R.id.custom_view)
    //}
    //
    //private fun clearShot3() {
    //    mWebChromeClient?.onHideCustomView()
    //}

    //inner class MyWebChromeClient : WebChromeClient() {
    //    private var mVideoProgressView: View? = null
    //    override fun onShowCustomView(
    //        view: View,
    //        requestedOrientation: Int,
    //        callback: CustomViewCallback
    //    ) {
    //        onShowCustomView(
    //            view,
    //            callback
    //        ) //To change body of overridden methods use File | Settings | File Templates.
    //    }
    //
    //    override fun onShowCustomView(view1: View, callback: CustomViewCallback) {
    //        Log.i(TAG, "onShowCustomView")
    //
    //        // if a view already exists then immediately terminate the new one
    //        if (mCustomView != null) {
    //            callback.onCustomViewHidden()
    //            return
    //        }
    //        val webView: WebView? = view?.findViewById(R.id.youtube_webview)
    //        mCustomView = view1
    //
    //        //webView!!.visibility = View.GONE
    //        customViewContainer?.visibility = View.VISIBLE
    //        customViewContainer?.addView(view1)
    //        customViewCallback = callback
    //    }
    //
    //    //override fun getVideoLoadingProgressView(): View? {
    //    //    if (mVideoProgressView == null) {
    //    //        val inflater = LayoutInflater.from(context)
    //    //        mVideoProgressView = inflater.inflate(R.layout.video_progress, null)
    //    //    }
    //    //    return mVideoProgressView
    //    //}
    //
    //    override fun onHideCustomView() {
    //        super.onHideCustomView() //To change body of overridden methods use File | Settings | File Templates.
    //        if (mCustomView == null) return
    //        val webView: WebView = view?.findViewById(R.id.youtube_webview) ?: return
    //        webView.setVisibility(View.VISIBLE)
    //        customViewContainer?.setVisibility(View.GONE)
    //
    //        // Hide the custom view.
    //        mCustomView?.setVisibility(View.GONE)
    //
    //        // Remove the custom view from its container.
    //        customViewContainer?.removeView(mCustomView)
    //        customViewCallback?.onCustomViewHidden()
    //        mCustomView = null
    //    }
    //}

    private fun showAlertDialog(view: View) {
        if (testDialog == null) {
            val newDialog = TestDialog(view.context)
            newDialog.setTitle("This is my custom newDialog box")
            newDialog.setCancelable(true)
            newDialog.show()
            newDialog.onViewCreated(this, "GBexfwe-9j0")
            testDialog = newDialog
        }
    }


    private class TestDialog(context: Context) : Dialog(context) {
        private var fragmentRef: WeakReference<WebView1Fragment>? = null
        private var videoId: String? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.test_alert_dialog)

            // TYPE_APPLICATION_MEDIA: for showing media (such as video). These windows are displayed behind their attached window.
            // https://developer.android.com/reference/android/view/WindowManager.LayoutParams
            //window!!.attributes.type = WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA

            val webView: WebView = findViewById(R.id.webview_in_dialog) ?: return
            webView.addJavascriptInterface(YouTubePlayerInterface(this), "YouTubePlayerInterface")
            loadHtml(webView, context.resources.openRawResource(R.raw.ayp_youtube_player_gecko1))
        }

        fun onViewCreated(fragment: WebView1Fragment, videoId: String) {
            this.videoId = videoId
            fragment.mainThreadHandler.postDelayed({
                val webView: WebView = findViewById(R.id.webview_in_dialog) ?: return@postDelayed
                Log.d(TAG, "TestDialog postDelayed screenshot2")
                fragment.screenshot2(webView, window!!, 0)
            }, 1000)
            fragmentRef = WeakReference(fragment)
        }

        fun onYoutubePlayerReady() {
            videoId?.let {
                fragmentRef?.get()?.mainThreadHandler?.postDelayed({
                    playVideo(it)
                }, 30)
            } ?: run {
                Log.e(TAG, "videoId is not set on PlayerReady")
            }
        }

        fun playVideo(videoId: String) {
            val webView: WebView = findViewById(R.id.webview_in_dialog) ?: return
            webView.loadUrl("javascript:player.loadVideoById('$videoId', 0);")
        }

        override fun onStop() {
            Log.d(TAG, "onStop")
            super.onStop()
            fragmentRef?.get()?.testDialog = null
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    private class YouTubePlayerInterface(val testDialog: TestDialog) {
        @JavascriptInterface
        fun sendReady() {
            Log.i(TAG, "YouTubePlayerInterface, sendReady")
            testDialog.onYoutubePlayerReady();
        }
    }

    companion object {
        private fun loadHtml(webView: WebView, htmlRes: InputStream) {
            webView.settings.javaScriptEnabled=true
            webView.settings.mediaPlaybackRequiresUserGesture = false
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            val htmlPage = readHTMLFromUTF8File(htmlRes)
            webView.loadDataWithBaseURL("https://www.youtube.com", htmlPage, "text/html", "utf-8", null)
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
}