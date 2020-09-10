package com.hyperisk.youtubegecko.ui.gecko_1

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView

class ExtendedWebView(context: Context, attr: AttributeSet) : WebView(context, attr) {
    var lastCaptureTime = 0L
    val captureIntervalMsec = 777L

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //Log.d(TAG, "onDraw")
//        canvas?.let { canvasNotNull ->
//            val curTime = System.currentTimeMillis()
//            if (lastCaptureTime == 0L || (curTime - lastCaptureTime > captureIntervalMsec)) {
//                lastCaptureTime = curTime
//            }
//        }
    }
}

private const val TAG = "ExtendedWebView"