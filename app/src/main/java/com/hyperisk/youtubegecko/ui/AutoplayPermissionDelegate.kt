package com.hyperisk.youtubegecko.ui

import android.util.Log
import org.mozilla.geckoview.GeckoSession

class AutoplayPermissionDelegate : GeckoSession.PermissionDelegate {
    private val TAG = "AutoplayPermissionDelegate"
    override fun onAndroidPermissionsRequest(
        session: GeckoSession,
        permissions: Array<String>?,
        callback: GeckoSession.PermissionDelegate.Callback
    ) {
        Log.w(TAG, "unhandled onAndroidPermissionsRequest $permissions")
    }

    override fun onContentPermissionRequest(
        session: GeckoSession,
        uri: String?,
        type: Int, callback: GeckoSession.PermissionDelegate.Callback
    ) {
        /*
        int PERMISSION_GEOLOCATION = 0;
        int PERMISSION_DESKTOP_NOTIFICATION = 1;
        int PERMISSION_PERSISTENT_STORAGE = 2;
        int PERMISSION_XR = 3;
        int PERMISSION_AUTOPLAY_INAUDIBLE = 4;
        int PERMISSION_AUTOPLAY_AUDIBLE = 5;
        int PERMISSION_MEDIA_KEY_SYSTEM_ACCESS = 6;
         */
        Log.i(TAG, "grant onContentPermissionRequest (type $type) $uri")
        if (type == GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE || type == GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE) {
            callback.grant()
        } else {
            callback.reject()
        }
    }

    override fun onMediaPermissionRequest(
        session: GeckoSession,
        uri: String,
        video: Array<GeckoSession.PermissionDelegate.MediaSource>?,
        audio: Array<GeckoSession.PermissionDelegate.MediaSource>?,
        callback: GeckoSession.PermissionDelegate.MediaCallback
    ) {
        Log.w(TAG, "unhandled onMediaPermissionRequest $uri")
    }
}