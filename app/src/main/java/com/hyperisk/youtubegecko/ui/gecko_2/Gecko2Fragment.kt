package com.hyperisk.youtubegecko.ui.gecko_2

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.hyperisk.youtubegecko.R
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.Utils
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.views.GeckoYouTubePlayer
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.geckoview.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException

class Gecko2Fragment : Fragment() {

    private lateinit var viewModel: Gecko2ViewModel
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
                ViewModelProviders.of(this).get(Gecko2ViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gecko_2, container, false)
        val geckoView = root.findViewById<GeckoView>(R.id.geckoview)
        val geckoSession = GeckoSession()
        s_runtime = GeckoRuntime.create(requireContext())

        regExt(geckoSession)

        geckoSession.open(s_runtime!!)
        geckoView.setSession(geckoSession)

//        mainThreadHandler.postDelayed({
            Log.i(TAG, " >>>> load HTML")
//        val htmlPage = Utils
//            .readHTMLFromUTF8File(resources.openRawResource(R.raw.ayp_simple))
////            .replace("<<injectedPlayerVars>>", playerOptions.toString())
////            .replace("<<injectedPlayerVars>>", "{\"autoplay\":0,\"controls\":1,\"enablejsapi\":1,\"fs\":0,\"origin\":\"https:\\/\\/www.youtube.com\",\"rel\":0,\"showinfo\":0,\"iv_load_policy\":3,\"modestbranding\":1,\"cc_load_policy\":0}")
//            .replace("<<injectedPlayerVars>>", "{}")
//        Log.i(TAG, "loadString")
//        geckoSession.loadString(htmlPage, "text/html")


            val htmlData = Utils.readHTMLFromUTF8File(resources.openRawResource(R.raw.ayp_youtube_player))
            val outputDir: File = requireContext().cacheDir // context being the Activity pointer
            val outputFile: File = File.createTempFile("iframe_", ".html", outputDir)
            val path: String = outputFile.getAbsolutePath()
            val writer = BufferedWriter(FileWriter(outputFile))
            writer.write(htmlData)
            writer.flush()
            val fileUri: String = Uri.fromFile(outputFile).toString()
            geckoSession.loadUri(fileUri)
            outputFile.deleteOnExit()

//        geckoSession.loadUri("https://www.youtube.com/watch?v=S0Q4gqBUs7c")

//        geckoSession.loadUri("https://mobile.twitter.com")

//        }, 2000)


        return root
    }

    val portDelegate: WebExtension.PortDelegate = object : WebExtension.PortDelegate {
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

    private fun regExt(session: GeckoSession) {
        val messageDelegate: WebExtension.MessageDelegate = object : WebExtension.MessageDelegate {
            override fun onConnect(port: WebExtension.Port) {
                s_port = port
                s_port!!.setDelegate(portDelegate)
                Log.i(TAG, "WebExtension port is connected")
                throw RuntimeException("here onConnect!")
            }

            override fun onMessage(
                nativeApp: String,
                message: Any,
                p2: WebExtension.MessageSender
            ): GeckoResult<Any>? {
                Log.i(TAG, "WebExtension onMessage from $nativeApp: $message")
                return null
            }
        }

        s_runtime!!.webExtensionController.list().then { extensionList ->
            var result: GeckoResult<WebExtension>? = null
            for (extension: WebExtension in extensionList!!) {
                Log.i(TAG, "extension in list: ${extension.id} ${extension.metaData?.version}")
//                if (extension.id == "'messaging@imvu.com" && extension.metaData?.version?.equals(1f)!!) {
//                    Log.i(TAG, "Extension already installed, no need to install it again")
//                    result =  GeckoResult.fromValue(extension)
//                }

                if (extension.id != null) {
                    Log.i(TAG, "UNINSTALL the extension!!!")
                    s_runtime!!.webExtensionController.uninstall(extension)
                }

            }
            result ?: run {
                Log.i(TAG, " >>>> call installBuiltIn now")
                s_runtime!!.webExtensionController
                    .installBuiltIn("resource://android/res/raw/")
            }
        }
            .accept( // Register message delegate for background script
                { extension ->
                    Log.i("MessageDelegate", " >>>> installBuiltIn accept, setMessageDelegate")
                    session.webExtensionController.setMessageDelegate(
                        extension!!,
                        messageDelegate,
                        "browser"
                    )
                }
            ) { e -> Log.e("MessageDelegate", "Error registering WebExtension", e) }

    }

    companion object {
        private const val TAG = "Gecko2Fragment"
        private var s_port: WebExtension.Port? = null
        private var s_runtime: GeckoRuntime? = null
        private var s_webView: GeckoYouTubePlayer? = null
    }
}