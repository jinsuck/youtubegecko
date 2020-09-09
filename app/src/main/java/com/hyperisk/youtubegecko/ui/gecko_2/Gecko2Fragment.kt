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
import com.hyperisk.youtubegecko.ui.AutoplayPermissionDelegate
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.geckoview.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class Gecko2Fragment : Fragment() {

    private lateinit var viewModel: Gecko2ViewModel
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(Gecko2ViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_gecko_2, container, false)
        val settings =
            if (BuildConfig.DEBUG) {
                GeckoRuntimeSettings.Builder()
                    .remoteDebuggingEnabled(true)
                    .consoleOutput(true)
                    .build()
            } else {
                GeckoRuntimeSettings.Builder()
                    .consoleOutput(true)
                    .build()
            }
        if (s_runtime == null) {
            s_runtime = GeckoRuntime.create(requireContext(), settings)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val geckoSession = GeckoSession()
        regExtension(geckoSession)
        geckoSession.permissionDelegate = AutoplayPermissionDelegate()
        geckoSession.open(s_runtime!!)
        val geckoView = view.findViewById<GeckoView>(R.id.geckoview)
        geckoView.setSession(geckoSession)

        loadHhtml(geckoSession)

        mainThreadHandler.postDelayed({
            sendPortMessage("playVideo")
            // TODO, wait for player ready event
        }, 2000)
    }

    val portDelegate: WebExtension.PortDelegate = object : WebExtension.PortDelegate {
        private val TAG = "PortDelegate"

        override fun onPortMessage(message: Any, port: WebExtension.Port) {
            Log.d(TAG, "onPortMessage >>> $message")
            //val message = toJSMesage(obj)
            //if (message != null) {
            //    s_webView?.onMessageReceived(message.name, message.data)
            //}
        }

        override fun onDisconnect(port: WebExtension.Port) {
            if (port === s_port) {
                s_port = null
            }
        }
    }

    private fun regExtension(session: GeckoSession) {
        val messageDelegate: WebExtension.MessageDelegate = object : WebExtension.MessageDelegate {
            override fun onConnect(port: WebExtension.Port) {
                port.setDelegate(portDelegate)
                s_port = port
                Log.i(TAG, "MessageDelegate: connected $port")
            }

            override fun onMessage(nativeApp: String, message: Any, messageSender: WebExtension.MessageSender): GeckoResult<Any>? {
                Log.i(TAG, "MessageDelegate message (from $nativeApp) $message")
                return null
            }
        }

        val runtimeNotNull = s_runtime ?: run {
            Log.e(TAG, "regExtension, s_runtime is null")
            return
        }
        runtimeNotNull.webExtensionController.list().then { extensionList ->
            var result: GeckoResult<WebExtension>? = null
            for (extension: WebExtension in extensionList!!) {
                Log.i(TAG, "extension in list: ${extension.id} ${extension.metaData?.version}")
                if (extension.id == "'messaging@imvu.com"
                        && extension.metaData?.version?.equals("1.0") == true) {
                    Log.i(TAG, "Extension already installed...")
                    if (BuildConfig.DEBUG) {
                        s_runtime?.webExtensionController?.uninstall(extension) ?: run {
                            Log.e(TAG, "uninstall skipped (webExtensionController null?)")
                        }
                    } else {
                        result = GeckoResult.fromValue(extension)
                    }
                }
            }
            result ?: run {
                s_runtime!!.webExtensionController
                    .installBuiltIn("resource://android/res/raw/")
            }
        }.accept( // Register message delegate for background script
            { extension ->
                val extensionNotNull = extension ?: run {
                    Log.e(TAG, "webExtensionController accept, extension is null")
                }
                Log.i("MessageDelegate", " installBuiltIn accept, setMessageDelegate")
                extension?.setMessageDelegate(messageDelegate, "browser")
                //session.webExtensionController.setMessageDelegate(extension!!, messageDelegate, "browser")
            }
        ) { e -> Log.e("MessageDelegate", "Error registering WebExtension", e) }
    }

    private fun loadHhtml(geckoSession: GeckoSession) {
        val htmlData = Gecko2ViewModel.readHTMLFromUTF8File(resources.openRawResource(R.raw.ayp_youtube_player_gecko2))
        val outputDir: File = requireContext().cacheDir // context being the Activity pointer
        val outputFile: File = File.createTempFile("iframe_", ".html", outputDir)
        val writer = BufferedWriter(FileWriter(outputFile))
        writer.write(htmlData)
        writer.flush()
        val fileUri: String = Uri.fromFile(outputFile).toString()
        geckoSession.loadUri(fileUri)
        outputFile.deleteOnExit()
    }

    private fun sendPortMessage(message: String) {
        val portNotNull = s_port ?: run {
            Log.e(TAG, "testSendPortMessage, s_port is null")
            return
        }
        val jsonObj = JSONObject()
        try {
            jsonObj.put("code", message)
            jsonObj.put("event", 1234)
        } catch (ex: JSONException) {
            throw RuntimeException(ex)
        }
        Log.i(TAG, "postMessage from Java to port $message")
        portNotNull.postMessage(jsonObj)
    }

    companion object {
        private const val TAG = "Gecko2Fragment"
        private var s_port: WebExtension.Port? = null
        private var s_runtime: GeckoRuntime? = null
    }

    class JSMessage(val name: String, val data: String) {
        override fun toString(): String {
            return String.format("{%s, %s}", name, data)
        }

        companion object {
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
        }
    }
}