package com.hyperisk.youtubegecko.ui.gecko_2

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.PixelCopy
import android.view.SurfaceView
import androidx.core.view.children
import org.mozilla.geckoview.GeckoView


class ExtendedGeckoView(context: Context, attr: AttributeSet) : GeckoView(context, attr) {

    private var m_bitmap: Bitmap? = null
    private var m_capturingInProgress = false
    private var m_sizeChanged = true
    private val childSurfaceView: SurfaceView

    init {
        Log.d(TAG, "init")

        addOnLayoutChangeListener { v, left, top, right, bottom,
                                    oldLeft, oldTop, oldRight, oldBottom ->
            Log.i(TAG, "OnLayoutChange from $oldLeft $oldTop ~ $oldRight $oldBottom to $left $top ~ $right $bottom")
            m_sizeChanged = true
        }

        if (childCount != 1) {
            throw java.lang.RuntimeException("childCount is expected to be 1")
        }
        val firstChildView = children.asIterable().first()
        childSurfaceView = firstChildView as? SurfaceView ?: run {
            throw java.lang.RuntimeException("firstChildView is expected to be SurfaceView")
        }
    }

    fun capturePicture(listener: PictureCaptureListener?) {
        if (m_capturingInProgress) {
            throw RuntimeException("Capturing picture is already in progress")
        }
        if (listener == null) {
            throw IllegalArgumentException("Invalid parameter - listener")
        }
        if (width == 0 || height == 0) {
            throw RuntimeException("Invalid size of the view")
        }
        if (m_bitmap == null || m_sizeChanged) {
            Log.d(TAG, String.format("Creating bitmap with size - (%d, %d)", width, height))
            m_bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            m_sizeChanged = false
        }
        m_capturingInProgress = true

        PixelCopy.request(childSurfaceView, m_bitmap!!, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                listener.pictureCaptureSucceeded(m_bitmap)
            } else {
                Log.e(TAG, "PixelCopy failed, reason - " + getErrorDescription(copyResult))
                listener.pictureCaptureFailed()
            }
            m_capturingInProgress = false
        }, handler)
    }

    interface PictureCaptureListener {
        fun pictureCaptureSucceeded(bitmap: Bitmap?)
        fun pictureCaptureFailed()
    }

    companion object {
        private fun getErrorDescription(pixelCopyError: Int): String? {
            return when (pixelCopyError) {
                PixelCopy.ERROR_DESTINATION_INVALID -> "Invalid destination"
                PixelCopy.ERROR_SOURCE_INVALID -> "Invalid source"
                PixelCopy.ERROR_TIMEOUT -> "Timeout expired"
                PixelCopy.ERROR_SOURCE_NO_DATA -> "No data in source"
                PixelCopy.ERROR_UNKNOWN -> "Unknown error"
                PixelCopy.SUCCESS -> "Success"
                else -> String.format("Unknown error code - %d", pixelCopyError)
            }
        }
    }
}

private const val TAG = "ExtendedGeckoView"