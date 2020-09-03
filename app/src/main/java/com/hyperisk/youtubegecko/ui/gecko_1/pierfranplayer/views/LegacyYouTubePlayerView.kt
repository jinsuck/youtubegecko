package com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.views

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.PlayerConstants
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.YouTubePlayer
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.listeners.AbstractYouTubePlayerListener
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.listeners.YouTubePlayerCallback
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.listeners.YouTubePlayerListener
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.options.IFramePlayerOptions
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.NetworkListener
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.PlaybackResumer
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranui.DefaultPlayerUiController
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranui.PlayerUiController
import org.mozilla.geckoview.GeckoView

internal class LegacyYouTubePlayerView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
        SixteenByNineFrameLayout(context, attrs, defStyleAttr), LifecycleObserver {

    constructor(context: Context): this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet? = null): this(context, attrs, 0)

//    internal val youTubePlayer: YouTubePlayer = WebViewYouTubePlayer(context)
    internal val youTubePlayer: YouTubePlayer = GeckoYouTubePlayer(context, attrs)
    private val defaultPlayerUiController: DefaultPlayerUiController

    private val networkListener = NetworkListener()
    private val playbackResumer = PlaybackResumer()
//    private val fullScreenHelper = FullScreenHelper(this)

    internal var isYouTubePlayerReady = false
    private var initialize = { }
    private val youTubePlayerCallbacks = HashSet<YouTubePlayerCallback>()

    internal var canPlay = true
        private set

    var isUsingCustomUi = false
        private set

    init {
        addView(youTubePlayer as View, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        defaultPlayerUiController = DefaultPlayerUiController(this, youTubePlayer)

//        fullScreenHelper.addFullScreenListener(defaultPlayerUiController)

        youTubePlayer.addListener(defaultPlayerUiController)
        youTubePlayer.addListener(playbackResumer)

        // stop playing if the user loads a video but then leaves the app before the video starts playing.
        youTubePlayer.addListener(object : AbstractYouTubePlayerListener() {
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                if(state == PlayerConstants.PlayerState.PLAYING && !isEligibleForPlayback())
                    youTubePlayer.pause()
            }
        })

        youTubePlayer.addListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                isYouTubePlayerReady = true

                youTubePlayerCallbacks.forEach { it.onYouTubePlayer(youTubePlayer) }
                youTubePlayerCallbacks.clear()

                youTubePlayer.removeListener(this)
            }
        })

        networkListener.onNetworkAvailable = {
            if (!isYouTubePlayerReady)
                initialize()
            else
                playbackResumer.resume(youTubePlayer)
        }
    }

    /**
     * Initialize the player. You must call this method before using the player.
     * @param youTubePlayerListener listener for player events
     * @param handleNetworkEvents if set to true a broadcast receiver will be registered and network events will be handled automatically.
     * If set to false, you should handle network events with your own broadcast receiver.
     * @param playerOptions customizable options for the embedded video player, can be null.
     */
    fun initialize(youTubePlayerListener: YouTubePlayerListener, handleNetworkEvents: Boolean, playerOptions: IFramePlayerOptions?) {
        if(isYouTubePlayerReady)
            throw IllegalStateException("This YouTubePlayerView has already been initialized.")

        if (handleNetworkEvents)
            context.registerReceiver(networkListener, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        initialize = {
            youTubePlayer.initialize({it.addListener(youTubePlayerListener)}, playerOptions)
        }

        if(!handleNetworkEvents)
            initialize()
    }

    /**
     * Initialize the player.
     * @param handleNetworkEvents if set to true a broadcast receiver will be registered and network events will be handled automatically.
     * If set to false, you should handle network events with your own broadcast receiver.
     *
     * @see LegacyYouTubePlayerView.initialize
     */
    fun initialize(youTubePlayerListener: YouTubePlayerListener, handleNetworkEvents: Boolean) =
            initialize(youTubePlayerListener, handleNetworkEvents, null)

    /**
     * Initialize the player. Network events are automatically handled by the player.
     * @param youTubePlayerListener listener for player events
     *
     * @see LegacyYouTubePlayerView.initialize
     */
    fun initialize(youTubePlayerListener: YouTubePlayerListener) =
            initialize(youTubePlayerListener, true)

    /**
     * Initialize a player using the web-base Ui instead pf the native Ui.
     * The default PlayerUiController will be removed and [LegacyYouTubePlayerView.getPlayerUiController] will throw exception.
     *
     * @see LegacyYouTubePlayerView.initialize
     */
//    fun initializeWithWebUi(youTubePlayerListener: YouTubePlayerListener, handleNetworkEvents: Boolean) {
//        val iFramePlayerOptions = IFramePlayerOptions.Builder().controls(1).build()
//        inflateCustomPlayerUi(R.layout.ayp_empty_layout)
//        initialize(youTubePlayerListener, handleNetworkEvents, iFramePlayerOptions)
//    }

    /**
     * @param youTubePlayerCallback A callback that will be called when the YouTubePlayer is ready.
     * If the player is ready when the function is called, the callback is called immediately.
     * This function is called only once.
     */
    fun getYouTubePlayerWhenReady(youTubePlayerCallback: YouTubePlayerCallback) {
        if(isYouTubePlayerReady)
            youTubePlayerCallback.onYouTubePlayer(youTubePlayer)
        else
            youTubePlayerCallbacks.add(youTubePlayerCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (youTubePlayer is GeckoYouTubePlayer) {
            removeView(youTubePlayer)
            youTubePlayer.removeAllViews()
            youTubePlayer.destroy()
        }
    }

    /**
     * Call this method before destroying the host Fragment/Activity, or register this View as an observer of its host lifecycle
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        if (youTubePlayer is WebView) {
            removeView(youTubePlayer)
            youTubePlayer.removeAllViews()
            youTubePlayer.destroy()
        } else if (youTubePlayer is GeckoYouTubePlayer) {
            removeView(youTubePlayer)
            youTubePlayer.removeAllViews()
            youTubePlayer.destroy()
        }
        try {
            context.unregisterReceiver(networkListener)
        } catch (ignore: Exception) {
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    internal fun onResume() {
        playbackResumer.onLifecycleResume()
        canPlay = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun onStop() {
        youTubePlayer.pause()
        playbackResumer.onLifecycleStop()
        canPlay = false
    }

    /**
     * Checks whether the player is in an eligible state for playback in
     * respect of the {@link WebViewYouTubePlayer#isBackgroundPlaybackEnabled}
     * property.
     */
    internal fun isEligibleForPlayback(): Boolean {
        return canPlay // || youTubePlayer.isBackgroundPlaybackEnabled
    }

    /**
     * Don't use this method if you want to publish your app on the PlayStore. Background playback is against YouTube terms of service.
     */
//    fun enableBackgroundPlayback(enable: Boolean) {
//        youTubePlayer.isBackgroundPlaybackEnabled = enable
//    }

    fun getPlayerUiController(): PlayerUiController {
        if (isUsingCustomUi)
            throw RuntimeException("You have inflated a custom player Ui. You must manage it with your own controller.")

        return defaultPlayerUiController
    }

//    fun enterFullScreen() = fullScreenHelper.enterFullScreen()
//
//    fun exitFullScreen() = fullScreenHelper.exitFullScreen()
//
//    fun toggleFullScreen() = fullScreenHelper.toggleFullScreen()
//
//    fun isFullScreen(): Boolean = fullScreenHelper.isFullScreen
//
//    fun addFullScreenListener(fullScreenListener: YouTubePlayerFullScreenListener): Boolean =
//            fullScreenHelper.addFullScreenListener(fullScreenListener)
//
//    fun removeFullScreenListener(fullScreenListener: YouTubePlayerFullScreenListener): Boolean =
//            fullScreenHelper.removeFullScreenListener(fullScreenListener)
}
