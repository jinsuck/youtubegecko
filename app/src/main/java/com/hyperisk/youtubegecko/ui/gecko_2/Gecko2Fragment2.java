package com.hyperisk.youtubegecko.ui.gecko_2;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hyperisk.youtubegecko.R;
import com.hyperisk.youtubegecko.ui.gecko_1.pierfranplayer.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Gecko2Fragment2 extends Fragment {
    private static final String TAG = "Gecko2Fragment2";

    private static GeckoRuntime sRuntime;
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private WebExtension.Port mPort;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_gecko_2, container, false);

        GeckoView geckoView = view.findViewById(R.id.geckoview);
        GeckoSession session = new GeckoSession();

        if (sRuntime == null) {
            GeckoRuntimeSettings settings = new GeckoRuntimeSettings.Builder()
                    .remoteDebuggingEnabled(true)
                    .consoleOutput(true)
                    .build();
            sRuntime = GeckoRuntime.create(getContext(), settings);
        }

        WebExtension.PortDelegate portDelegate = new WebExtension.PortDelegate() {
            @Override
            public void onPortMessage(final @NonNull Object message,
                                      final @NonNull WebExtension.Port port) {
                Log.d(TAG, "onPortMessage >>> " + message);
            }

            @Override
            public void onDisconnect(final @NonNull WebExtension.Port port) {
                // This port is not usable anymore.
                if (port == mPort) {
                    mPort = null;
                }
            }
        };

        WebExtension.MessageDelegate messageDelegate = new WebExtension.MessageDelegate() {
            @Override
            @Nullable
            public void onConnect(final @NonNull WebExtension.Port port) {
                Log.i(TAG, "onConnect");
                mPort = port;
                mPort.setDelegate(portDelegate);
            }

            @Nullable
            @Override
            public GeckoResult<Object> onMessage(@NonNull String s, @NonNull Object o, @NonNull WebExtension.MessageSender messageSender) {
                Log.i(TAG, ">>> onMessage " + s + " : " + o);
                return null;
            }
        };


        sRuntime.getWebExtensionController()
                .installBuiltIn("resource://android/res/raw/")
                .accept(
                        // Register message delegate for background script
                        extension -> extension.setMessageDelegate(messageDelegate, "browser"),
                        e -> Log.e("MessageDelegate", "Error registering WebExtension", e)
                );

        session.open(sRuntime);
        geckoView.setSession(session);

//        session.loadUri("https://mobile.twitter.com");
//        session.loadUri("https://www.youtube.com/watch?v=S0Q4gqBUs7c");

        String htmlData = Utils.INSTANCE.readHTMLFromUTF8File(getResources().openRawResource(R.raw.ayp_youtube_player));
        File outputDir = requireContext().getCacheDir(); // context being the Activity pointer
        File outputFile = null;
        try {
            outputFile = File.createTempFile("iframe_", ".html", outputDir);
            String path = outputFile.getAbsolutePath();
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(htmlData);
            writer.flush();
            String fileUri = Uri.fromFile(outputFile).toString();
            session.loadUri(fileUri);
            outputFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mainThreadHandler.postDelayed(() -> {
            if (mPort == null) {
                Log.e(TAG, ">>> mPort is NULL");
                return;
            }

            JSONObject message = new JSONObject();
            try {
                message.put("code", "alert('test alert!');");
                message.put("event", 1234);
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }

            Log.i(TAG, "postMessage from Java to port " + message.toString());
            mPort.postMessage(message);

        }, 3300);


        return view;
    }

}
