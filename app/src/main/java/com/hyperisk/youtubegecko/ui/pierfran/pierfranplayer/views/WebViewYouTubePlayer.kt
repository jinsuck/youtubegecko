package com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.hyperisk.youtubegecko.R
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.YouTubePlayerInterface
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.YouTubePlayerBridge
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.listeners.YouTubePlayerListener
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.options.IFramePlayerOptions
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.utils.Utils
import java.util.*

/**
 * WebView implementation of [YouTubePlayerInterface]. The player runs inside the WebView, using the IFrame Player API.
 */

private const val TAG = "home/WebViewYouTubePlayer"

internal class WebViewYouTubePlayer constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : WebView(context, attrs, defStyleAttr), YouTubePlayerInterface, YouTubePlayerBridge.YouTubePlayerBridgeCallbacks {

    private lateinit var youTubePlayerInitListener: (YouTubePlayerInterface) -> Unit

    private val youTubePlayerListeners = HashSet<YouTubePlayerListener>()
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    internal var isBackgroundPlaybackEnabled = false

    internal fun initialize(initListener: (YouTubePlayerInterface) -> Unit, playerOptions: IFramePlayerOptions?) {
        youTubePlayerInitListener = initListener
        initWebView(playerOptions ?: IFramePlayerOptions.default)
    }

    override fun onYouTubeIFrameAPIReady() = youTubePlayerInitListener(this)

    override fun getInstance(): YouTubePlayerInterface = this

    override fun loadVideo(videoId: String, startSeconds: Float) {
        Log.i(TAG, "loadVideo $startSeconds")
        mainThreadHandler.post { loadUrl("javascript:loadVideo('$videoId', $startSeconds)") }
    }

    override fun cueVideo(videoId: String, startSeconds: Float) {
        Log.i(TAG, "cueVideo")
        mainThreadHandler.post { loadUrl("javascript:cueVideo('$videoId', $startSeconds)") }
    }

    override fun play() {
        Log.i(TAG, "play")
        mainThreadHandler.post { loadUrl("javascript:playVideo()") }
    }

    override fun pause() {
        mainThreadHandler.post { loadUrl("javascript:pauseVideo()") }
    }

    override fun mute() {
        mainThreadHandler.post { loadUrl("javascript:mute()") }
    }

    override fun unMute() {
        mainThreadHandler.post { loadUrl("javascript:unMute()") }
    }

    override fun setVolume(volumePercent: Int) {
        require(!(volumePercent < 0 || volumePercent > 100)) { "Volume must be between 0 and 100" }

        mainThreadHandler.post { loadUrl("javascript:setVolume($volumePercent)") }
    }

    override fun seekTo(time: Float) {
        Log.i(TAG, "seekTo")
        mainThreadHandler.post { loadUrl("javascript:seekTo($time)") }
    }

    override fun destroy() {
        youTubePlayerListeners.clear()
        mainThreadHandler.removeCallbacksAndMessages(null)
        super.destroy()
    }

    override fun getListeners(): Collection<YouTubePlayerListener> {
        return Collections.unmodifiableCollection(HashSet(youTubePlayerListeners))
    }

    override fun addListener(listener: YouTubePlayerListener): Boolean {
        return youTubePlayerListeners.add(listener)
    }

    override fun removeListener(listener: YouTubePlayerListener): Boolean {
        return youTubePlayerListeners.remove(listener)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(playerOptions: IFramePlayerOptions) {
        settings.javaScriptEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        addJavascriptInterface(YouTubePlayerBridge(this), "YouTubePlayerBridge")

        Log.i(TAG, "injectedPlayerVars $playerOptions")
        val htmlPage = Utils
                .readHTMLFromUTF8File(resources.openRawResource(R.raw.ayp_youtube_player_home))
                .replace("<<injectedPlayerVars>>", playerOptions.toString())

        loadDataWithBaseURL(playerOptions.getOrigin(), htmlPage, "text/html", "utf-8", null)

        // if the video's thumbnail is not in memory, show a black screen
        webChromeClient = object : WebChromeClient() {
            override fun getDefaultVideoPoster(): Bitmap? {
                val result = super.getDefaultVideoPoster()

                return result ?: Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
            }
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        if (isBackgroundPlaybackEnabled && (visibility == View.GONE || visibility == View.INVISIBLE))
            return

        super.onWindowVisibilityChanged(visibility)
    }
}
