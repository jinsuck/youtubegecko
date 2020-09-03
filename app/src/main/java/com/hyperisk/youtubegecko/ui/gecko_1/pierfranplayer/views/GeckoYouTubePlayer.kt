package com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import com.hyperisk.youtubegecko.R
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.YouTubePlayer
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.YouTubePlayerBridge
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.listeners.YouTubePlayerListener
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.options.IFramePlayerOptions
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.geckoview.*
import org.mozilla.geckoview.WebExtension.MessageDelegate
import org.mozilla.geckoview.WebExtension.PortDelegate
import java.util.*


internal class GeckoYouTubePlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
)
    : GeckoView(context, attrs), YouTubePlayer, YouTubePlayerBridge.YouTubePlayerBridgeCallbacks {

    private lateinit var youTubePlayerInitListener: (YouTubePlayer) -> Unit
    private val youTubePlayerListeners = HashSet<YouTubePlayerListener>()
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
    var geckoRuntime: GeckoRuntime? = null

    override fun initialize(
        initListener: (YouTubePlayer) -> Unit,
        playerOptions: IFramePlayerOptions?
    ) {
        youTubePlayerInitListener = initListener

        mainThreadHandler.postDelayed({
            val geckoRuntimeSettings = GeckoRuntimeSettings.Builder()
            geckoRuntimeSettings.javaScriptEnabled(true);

            // TODO
//            addJavascriptInterface(YouTubePlayerBridge(this), "YouTubePlayerBridge")

            val geckoSession = GeckoSession()
            geckoRuntime = GeckoRuntime.create(context, geckoRuntimeSettings.build())

//            regExt()

            geckoSession.open(geckoRuntime!!)
            setSession(geckoSession)

            val htmlPage = Utils
                .readHTMLFromUTF8File(resources.openRawResource(R.raw.ayp_youtube_player))
                .replace("<<injectedPlayerVars>>", playerOptions.toString())
            Log.i(TAG, "loadString")
//            session?.loadString(htmlPage, "text/html")
//            session?.loadUri("https://www.youtube.com/watch?v=S0Q4gqBUs7c")
            session?.loadUri("https://mobile.twitter.com")
        }, 500)

        mainThreadHandler.postDelayed({
//            loadVideo("S0Q4gqBUs7c", 0f)
//            play()

        }, 2000)
    }

    fun destroy() {
        session?.close()
        geckoRuntime?.shutdown()
    }

    private fun regExt() {
        val messageDelegate: MessageDelegate = object : MessageDelegate {
            override fun onConnect(port: WebExtension.Port) {
                s_port = port
                s_port!!.setDelegate(portDelegate)
                Log.i(TAG, "WebExtension port is connected")
            }
        }


        geckoRuntime!!.webExtensionController.list().then { extensionList ->
            var result:GeckoResult<WebExtension>? = null
            for (extension: WebExtension in extensionList!!) {
                Log.i(TAG, "extension in list: ${extension.id} ${extension.metaData?.version}")
                if (extension.id == "'messaging@imvu.com" && extension.metaData?.version?.equals(1f)!!) {
                    Log.i(TAG, "Extension already installed, no need to install it again")
                    result =  GeckoResult.fromValue(extension)
                }
            }
            result ?: run {
                Log.i(TAG, "call installBuiltIn now")
                geckoRuntime!!.webExtensionController
                    .installBuiltIn("resource://android/res/raw/")
            }
        }
            .accept( // Register message delegate for background script
            { extension ->
                Log.i("MessageDelegate", "installBuiltIn accept, setMessageDelegate")
                session!!.webExtensionController.setMessageDelegate(extension!!, messageDelegate, "browser")
            }
        ) { e -> Log.e("MessageDelegate", "Error registering WebExtension", e) }

    }

    fun evaluateJavascript(code: String?) {
        try {
            val message = JSONObject()
            message.put("code", code)
            Log.i(TAG, "Message is sent - $message")
            s_port!!.postMessage(message)
        } catch (exc: JSONException) {
            Log.e(TAG, "Executing js code failed - " + exc.message)
        }
    }

    fun onMessageReceived(name: String, data: String) {
        Log.i(TAG, "onMessageReceived $name $data")
    }

    override fun loadVideo(videoId: String, startSeconds: Float) {
        Log.i(TAG, "loadVideo")
        mainThreadHandler.post {
//            session?.loadUri("javascript:loadVideo('$videoId', $startSeconds)")
            evaluateJavascript("showAlert111();")
        }
    }

    override fun cueVideo(videoId: String, startSeconds: Float) {
        TODO("Not yet implemented")
    }

    override fun play() {
        Log.i(TAG, "play")
//        mainThreadHandler.post { session?.loadUri("javascript:playVideo()") }
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun mute() {
        TODO("Not yet implemented")
    }

    override fun unMute() {
        TODO("Not yet implemented")
    }

    override fun setVolume(volumePercent: Int) {
        TODO("Not yet implemented")
    }

    override fun seekTo(time: Float) {
        TODO("Not yet implemented")
    }

    override fun addListener(listener: YouTubePlayerListener): Boolean {
        return youTubePlayerListeners.add(listener)
    }

    override fun removeListener(listener: YouTubePlayerListener): Boolean {
        TODO("Not yet implemented")
    }

    override fun getInstance(): YouTubePlayer {
        TODO("Not yet implemented")
    }

    override fun getListeners(): Collection<YouTubePlayerListener> {
        TODO("Not yet implemented")
    }

    override fun onYouTubeIFrameAPIReady() {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "GeckoYouTubePlayer"
        private var s_port: WebExtension.Port? = null
        private var s_runtime: GeckoRuntime? = null
        private var s_webView: GeckoYouTubePlayer? = null
    }


    val portDelegate: PortDelegate = object : PortDelegate {
        private val TAG = "PortDelegate"

        // public WebExtension.Port port = null;
        inner class JSMessage(val name: String, val data: String) {

            override fun toString(): String {
                return String.format("{%s, %s}", name, data)
            }
        }

        private fun toJSMesage(obj: Any): JSMessage? {
            return try {
                val jsonObj: JSONObject = when (obj) {
                    is String -> {
                        JSONObject(obj)
                    }
                    is JSONObject -> {
                        obj
                    }
                    else -> {
                        Log.e(TAG, "Not supported message type - " + obj.javaClass.toString())
                        return null
                    }
                }
                val name = jsonObj.getString("event")

                // could be null
                val data = jsonObj.optString("info")
                JSMessage(name, data)
            } catch (exc: JSONException) {
                Log.e(TAG, "Failed to parse json str - " + exc.message)
                null
            }
        }

        override fun onPortMessage(obj: Any, port: WebExtension.Port) {
            val message = toJSMesage(obj)
            if (message != null) {
                s_webView?.onMessageReceived(message.name, message.data)
            }
        }

        override fun onDisconnect(port: WebExtension.Port) {
            if (port === s_port) {
                s_port = null
            }
        }
    }
}