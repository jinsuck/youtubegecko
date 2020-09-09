/*

package org.godotengine.godot.modules.webview.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import android.util.Log;
import android.graphics.Bitmap;
import android.widget.FrameLayout;

import org.godotengine.godot.Godot;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.WebExtension;

public class GeckoWebViewProxy
{
    private final static String TAG = "GeckoWebViewProxy";

    private ExtendedGeckoView m_geckoView;

    private GeckoSession m_geckoSession;

    private int m_uniqueId;

    private Godot m_activity;

    private native void pictureCaptureSucceededNative(Bitmap bitmap, int id);

    private native void pictureCaptureFailedNative(int id);

    private native void onMessageReceivedNative(int id, String name, Object argument);

    public native static void registerClass();

    private static WebExtension.Port s_port = null;

    private static GeckoRuntime s_runtime = null;

    private static Set<GeckoWebViewProxy> s_webViews = new HashSet<>();

    private class FrameCaptureListener implements ExtendedGeckoView.PictureCaptureListener
    {
        @Override
        public void pictureCaptureSucceeded(Bitmap bitmap)
        {
            pictureCaptureSucceededNative(bitmap, m_uniqueId);
        }

        @Override
        public void pictureCaptureFailed()
        {
            pictureCaptureFailedNative(m_uniqueId);
        }
    }

    private FrameCaptureListener m_captureListener = new FrameCaptureListener();

    static void initializeRuntimeAndExtensions(Context context)
    {
        GeckoRuntimeSettings.Builder geckoRuntimeSettings = new GeckoRuntimeSettings.Builder();
        geckoRuntimeSettings.javaScriptEnabled(true);
        geckoRuntimeSettings.autoplayDefault(GeckoRuntimeSettings.AUTOPLAY_DEFAULT_ALLOWED);
        s_runtime = GeckoRuntime.create(context, geckoRuntimeSettings.build());

        initializeExtensions();
    }

    private static void initializeExtensions()
    {
        final WebExtension.PortDelegate portDelegate = new WebExtension.PortDelegate()
        {
            private final static String TAG = "PortDelegate";

            // public WebExtension.Port port = null;

            class JSMessage
            {
                private String m_name;
                private String m_data;

                JSMessage(String name, String data)
                {
                    m_name = name;
                    m_data = data;
                }

                String getName()
                {
                    return m_name;
                }

                String getData()
                {
                    return m_data;
                }

                @NonNull
                public String toString()
                {
                    return String.format("{%s, %s}", m_name, m_data);
                }
            }

            private JSMessage toJSMesage(final Object obj)
            {
                try
                {
                    JSONObject jsonObj;
                    if (obj instanceof String)
                    {
                        jsonObj = new JSONObject((String)obj);
                    }
                    else if (obj instanceof JSONObject)
                    {
                        jsonObj = (JSONObject) obj;
                    }
                    else
                    {
                        Log.e(TAG, "Not supported message type - " + obj.getClass().toString());
                        return null;
                    }

                    String name = jsonObj.getString("event");

                    // could be null
                    String data = jsonObj.optString("info");

                    return new JSMessage(name, data);
                }
                catch (org.json.JSONException exc)
                {
                    Log.e(TAG, "Failed to parse json str - " + exc.getMessage());

                    return null;
                }
            }

            public void onPortMessage(final @NonNull Object obj, final @NonNull WebExtension.Port port)
            {
                JSMessage message = toJSMesage(obj);
                if (message != null)
                {
                    // Log.d(TAG, "Message received - " + message.toString());
                    for (GeckoWebViewProxy webView : s_webViews)
                    {
                        webView.onMessageReceived(message.getName(), message.getData());
                    }
                }
            }

            public void onDisconnect(final @NonNull WebExtension.Port port)
            {
                if (port == s_port)
                {
                    s_port = null;
                }
            }
        };

        WebExtension.MessageDelegate messageDelegate = new WebExtension.MessageDelegate()
        {
            public void onConnect(final @NonNull WebExtension.Port port)
            {
                s_port = port;
                s_port.setDelegate(portDelegate);
                Log.i(TAG, "WebExtension port is connected");
            }
        };

        WebExtension extension = new WebExtension(
                "resource://android/assets/messaging/",
                "messaging@imvu.com",
                WebExtension.Flags.ALLOW_CONTENT_MESSAGING);

        extension.setMessageDelegate(messageDelegate, "browser");

        // Run the WebExtension
        s_runtime.registerWebExtension(extension);

        Log.i(TAG, "WebExtension successfully initialized");
    }

    @Keep
    public GeckoWebViewProxy(Context context, int uniqueId, int width, int height)
    {
        Log.i(TAG, "GeckoWebViewProxy constructor called");

        m_activity = (Godot)context;

        m_uniqueId = uniqueId;

        m_geckoView = new ExtendedGeckoView(m_activity);
        m_geckoView.setLayoutParams(new FrameLayout.LayoutParams(width, height));

        // add to the bottom of the scene
        m_activity.layout.addView(m_geckoView);

        m_geckoSession = new GeckoSession();
        m_geckoSession.open(s_runtime);
        m_geckoView.setSession(m_geckoSession);

        s_webViews.add(this);
    }

    @Keep
    @SuppressWarnings("unused")
    public void release()
    {
        s_webViews.remove(this);
        m_geckoSession.close();
        m_activity.layout.removeView(m_geckoView);
    }

    private void onMessageReceived(String messageName, Object argument)
    {
        onMessageReceivedNative(m_uniqueId, messageName, argument);
    }

    @SuppressWarnings("unused")
    public void setVisibility(int visibility)
    {
        m_geckoView.setVisibility(visibility);
    }

    @Keep
    @SuppressWarnings("unused")
    public void evaluateJavascript(final String code)
    {
        try
        {
            JSONObject message = new JSONObject();
            message.put("code", code);

            Log.i(TAG, "Message is sent - " + message.toString());

            s_port.postMessage(message);
        }
        catch(org.json.JSONException exc)
        {
            Log.e(TAG, "Executing js code failed - " + exc.getMessage());
        }
    }

    @Keep
    @SuppressWarnings("unused")
    public void capturePicture()
    {
        try
        {
            if (!m_geckoView.isCapturingInProgress())
            {
                m_geckoView.capturePicture(m_captureListener);
            }
        }
        catch(Exception exc)
        {
            Log.e(TAG, "Capture failed - " + exc.getMessage());
        }
    }

    @Keep
    @SuppressWarnings("unused")
    public void loadUrl(final String uri)
    {
        m_geckoSession.loadUri(uri);
    }

    @Keep
    @SuppressWarnings("unused")
    public void loadHtmlData(final String htmlData)
    {
        try
        {
            File outputDir = m_activity.getCacheDir(); // context being the Activity pointer
            File outputFile = File.createTempFile("iframe_", ".html", outputDir);
            String path = outputFile.getAbsolutePath();

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(htmlData);
            writer.flush();

            String fileUri = Uri.fromFile(outputFile).toString();
            m_geckoSession.loadUri(fileUri);

            outputFile.deleteOnExit();
        }
        catch(IOException exc)
        {
            Log.e(TAG, "loadHtmlData failed - " + exc.getMessage());
        }
    }
}
 */