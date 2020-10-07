package com.hyperisk.youtubegecko.ui.gecko_1

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX
import com.google.android.youtube.player.YouTubePlayerView
import com.hyperisk.youtubegecko.R
import kotlinx.android.synthetic.main.fragment_gecko_2.view.*
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.ref.WeakReference

// https://gist.github.com/medyo/f226b967213c3b8ec6f6bebb5338a492

private const val DEVELOPER_KEY = "AIzaSyCeiyNXn9BJyEfHISBRoRaG_VbYA8gZRzE"
private const val TAG = "YouTubeFrag1Fragment"

class YouTubeFrag1Fragment : Fragment(), YouTubePlayer.OnInitializedListener {
    private var testDialog: TestDialog? = null
    private var bitmapCopy: Bitmap? = null
    private var ytpView: View? = null
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
    private var screenShotStartTime = 0L
    private var numScreenshots = 0
    private var surfaceView: SurfaceView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youtubeplayer, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")

        val ytpFragX = YouTubePlayerSupportFragmentX.newInstance()
        childFragmentManager.beginTransaction().add(R.id.youtube_fragment_x, ytpFragX).commitNowAllowingStateLoss()
        ytpFragX.initialize(DEVELOPER_KEY, this)

        view.postDelayed({
            showAlertDialog(view)
        }, 100)

        view.postDelayed({
            ytpView = ytpFragX.requireView()
            val ytpFragXParent = ytpView!!.parent as ViewGroup
            ytpFragXParent.removeView(ytpView)
            //ytpView?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            testDialog?.findViewById<FrameLayout>(R.id.youtubeplayer_fragment_in_dialog)!!.addView(ytpView)
        }, 3000)
    }

    private fun showAlertDialog(view: View) {
            if (testDialog == null) {
                val newDialog = TestDialog(view.context)
                newDialog.setTitle("This is my custom newDialog box")
                newDialog.setCancelable(true)
                newDialog.show()
                newDialog.onViewCreated(this)
                testDialog = newDialog
            }
    }

    fun screenshot2(window: Window, toolbarHeight: Int) {
        //if (!window.isActive) return
        if (testDialog == null || !isAdded) return

        //Log.d(TAG, "window size ${window.decorView.measuredWidth} x ${window.decorView.measuredHeight}")
        val rectNotNull = Rect(0, toolbarHeight, window.decorView.measuredWidth, toolbarHeight + window.decorView.measuredHeight)
        val beforePixelCopy = System.currentTimeMillis()
        if (screenShotStartTime == 0L) screenShotStartTime = beforePixelCopy
        Log.d(TAG, "screenshot2 rectNotNull $rectNotNull, elapsed from first screenshot: ${beforePixelCopy - screenShotStartTime}ms")

        if (surfaceView == null && ytpView != null) {
            val fralay = ((((window.decorView as FrameLayout).getChildAt(0) as FrameLayout).getChildAt(0) as FrameLayout).getChildAt(0) as ViewGroup).getChildAt(1)
            val fralay2 = ((fralay as FrameLayout).getChildAt(0) as YouTubePlayerView).getChildAt(0) as FrameLayout
            val chv0 = fralay2.getChildAt(0) as? ViewGroup
            chv0?.let {
                Log.d(TAG, "chv0: $it, size: ${it.width} x ${it.height}")
                inspectView(it, 0)
            } ?:run {
                Log.d(TAG, "chv0 is not ViewGroup, and instead ${chv0?.javaClass?.canonicalName}")
            }
        }

        surfaceView?.let { surfaceViewNotNull ->
            if (surfaceViewNotNull.width == 0) {
                Log.d(TAG, "surfaceViewNotNull.width == 0")
                return
            }

            // does not work -- with LAYER_TYPE_SOFTWARE, it's black anyway
            //surfaceViewNotNull.isDrawingCacheEnabled = true
            //surfaceViewNotNull.getDrawingCache()?.let {
            //    val bitmapCache = Bitmap.createBitmap(it)
            //    Log.d(TAG, "copied from bitmap! ${it.width} x ${it.height}")
            //    requireView().image_copy_pixel.setImageBitmap(bitmapCache)
            //} ?: run {
            //    Log.w(TAG, "getDrawingCache null")
            //}

            //var bitmapFromCanvas =
            //    Bitmap.createBitmap(surfaceViewNotNull.width, surfaceViewNotNull.height, Bitmap.Config.ARGB_8888)
            //val canvas = Canvas(bitmapFromCanvas)
            //canvas.drawColor(Color.YELLOW)
            //surfaceViewNotNull.draw(canvas)
            //requireView().image_copy_pixel.setImageBitmap(bitmapFromCanvas)
            //Log.d(TAG, "copied from bitmap canvas! ${bitmapFromCanvas.width} x ${bitmapFromCanvas.height}")

            var bitmapCopy =
                Bitmap.createBitmap(surfaceViewNotNull.width, surfaceViewNotNull.height, Bitmap.Config.ARGB_8888)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                try {
                    PixelCopy.request(surfaceViewNotNull, bitmapCopy, PixelCopy.OnPixelCopyFinishedListener { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            requireView().image_copy_pixel.setImageBitmap(bitmapCopy)
                        }
                    }, mainThreadHandler)
                } catch (e: Exception) {
                    Log.d(TAG, "PixelCopy.request: " + e.toString())
                }
            }
        }
        mainThreadHandler.postDelayed({
            screenshot2(window, toolbarHeight)
        }, 33)
    }

    private fun inspectView(view: ViewGroup, indent: Int) {
        for (i in 0 until view.childCount) {
            val chv1 = view.getChildAt(i)
            var spaces = "  "
            for (i in 0 until indent) spaces += "  "
            Log.d(TAG, "$spaces chv $i: $chv1, size: ${chv1.width} x ${chv1.height}")
            if (chv1 is TextureView) {
                Log.d(TAG, " ---> TextureView")
            } else if (chv1 is SurfaceView) {
                Log.d(TAG, " ---> SurfaceView")
                surfaceView = chv1 as SurfaceView
            } else if (chv1 is ViewGroup) {
                inspectView(chv1, indent + 2)
            }
        }

    }

    private class TestDialog(context: Context) : Dialog(context) {
        private var fragmentRef: WeakReference<YouTubeFrag1Fragment>? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.test_alert_dialog_youtubeplayer)

            // TYPE_APPLICATION_MEDIA: for showing media (such as video). These windows are displayed behind their attached window.
            // https://developer.android.com/reference/android/view/WindowManager.LayoutParams
            window!!.attributes.type = WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA
        }

        fun onViewCreated(fragment: YouTubeFrag1Fragment) {
            fragmentRef = WeakReference(fragment)
            fragment.mainThreadHandler.postDelayed({
                fragment.screenshot2(window!!, 0)
            }, 1000)
        }

    }

    override fun onInitializationSuccess(
        p0: YouTubePlayer.Provider?,
        player: YouTubePlayer?,
        wasRestored: Boolean
    ) {
        Log.i(TAG, "YouTubePlayer onInitializationSuccess")
        if (!wasRestored) {
            player?.cueVideo("VCrkjGIfZzY")
            view?.postDelayed({
                Log.i(TAG, "YouTubePlayer play now")
                player?.play()
            }, 1000)
        }

    }

    override fun onInitializationFailure(
        p0: YouTubePlayer.Provider?,
        p1: YouTubeInitializationResult?
    ) {
        Log.e(TAG, "YouTubePlayer onInitializationFailure")
    }
}

