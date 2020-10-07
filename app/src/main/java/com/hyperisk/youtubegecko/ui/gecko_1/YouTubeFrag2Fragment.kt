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
private const val TAG = "YouTubeFrag2Fragment"

class YouTubeFrag2Fragment : Fragment(), YouTubePlayer.OnInitializedListener {
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
        val view = inflater.inflate(R.layout.fragment_youtubeplayer_2, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")

        val ytpFragX = YouTubePlayerSupportFragmentX.newInstance()
        childFragmentManager.beginTransaction().add(R.id.youtube_fragment_x, ytpFragX).commitNowAllowingStateLoss()
        ytpFragX.initialize(DEVELOPER_KEY, this)

        view.postDelayed({
            ytpView = ytpFragX.requireView()
//            val ytpFragXParent = ytpView!!.parent as ViewGroup
//            ytpFragXParent.removeView(ytpView)
            //ytpView?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            screenshot2(0)
        }, 1000)
    }

    fun screenshot2(toolbarHeight: Int) {
        //if (!window.isActive) return
        val bitmapCopyNotNull: Bitmap = bitmapCopy ?: run {
            val bitmapNew = Bitmap.createBitmap(
                ytpView!!.measuredWidth,
                ytpView!!.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmapCopy = bitmapNew
            bitmapNew
        }

        //Log.d(TAG, "window size ${window.decorView.measuredWidth} x ${window.decorView.measuredHeight}")
        val rectNotNull = Rect(0, toolbarHeight, ytpView!!.measuredWidth, toolbarHeight + ytpView!!.measuredHeight)
        val beforePixelCopy = System.currentTimeMillis()
        if (screenShotStartTime == 0L) screenShotStartTime = beforePixelCopy
        Log.d(TAG, "screenshot2 rectNotNull $rectNotNull, elapsed from first screenshot: ${beforePixelCopy - screenShotStartTime}ms")

        if (surfaceView == null) {
            val chv0 = (((ytpView!! as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0) as? ViewGroup)
            chv0?.let {
                Log.d(TAG, "child view 0: $it, size: ${it.width} x ${it.height}")
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
        } ?: run {
            Log.d(TAG, "surfaceView is null")
        }
        mainThreadHandler.postDelayed({
            screenshot2(toolbarHeight)
        }, 33)
    }

    private fun inspectView(view: ViewGroup, indent: Int) {
        for (i in 0 until view.childCount) {
            val chv1 = view.getChildAt(i)
            var spaces = "  "
            for (i in 0 until indent) spaces += "  "
            Log.d(TAG, "$spaces child view $i: $chv1, size: ${chv1.width} x ${chv1.height}")
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


/*
child view 0: apom{4ce6b88 VFE...... ......I. 0,0-600,330}, size: 600 x 330
    child view 0: qit{3d76921 V.E...... ......I. 0,0-600,330}, size: 600 x 330
        child view 0: android.view.SurfaceView{5460146 V.E...... ........ 0,0-600,330}, size: 600 x 330
  ---> SurfaceView
        child view 1: android.view.View{5560f07 V.ED..... ........ 0,0-600,330}, size: 600 x 330
    child view 1: android.widget.ImageView{792f50e G.ED..... ......ID 0,0-600,330}, size: 600 x 330
    child view 2: aojg{4a2b834 G.E...... ......I. 0,0-600,330}, size: 600 x 330
    child view 3: aohc{9a0765d I.E...... ......I. 0,0-600,330}, size: 600 x 330
    child view 4: qrx{650ebd2 G.E...... ......ID 0,0-600,330}, size: 600 x 330
        child view 0: android.widget.RelativeLayout{eba94a3 V.E...... ......ID 0,0-600,330 #7f0b03e0 app:id/controls_layout}, size: 600 x 330
            child view 0: androidx.appcompat.widget.AppCompatTextView{7cd23a0 G.ED..... ......I. 0,0-0,0 #7f0b0a7a app:id/player_error_view}, size: 0 x 0
            child view 1: com.google.android.libraries.youtube.common.ui.TouchImageView{f31b59 IFED..C.. ......I. 180,45-420,285 #7f0b0a75 app:id/player_control_play_pause_replay_button}, size: 240 x 240
            child view 2: com.google.android.libraries.youtube.common.ui.TouchImageView{9adf31e GF.D..C.. ......ID 0,0-0,0 #7f0b0a76 app:id/player_control_previous_button}, size: 0 x 0
            child view 3: com.google.android.libraries.youtube.common.ui.TouchImageView{657d7ff GF.D..C.. ......ID 0,0-0,0 #7f0b0a74 app:id/player_control_next_button}, size: 0 x 0
            child view 4: android.widget.ProgressBar{570d9cc V.ED..... ......ID 228,93-372,237 #7f0b0a7e app:id/player_loading_view}, size: 144 x 144
            child view 5: com.google.android.apps.youtube.embeddedplayer.service.ui.controlsbar.remoteloaded.MinimalTimeBar{3311415 V.ED..... ......ID 0,318-600,330 #7f0b0a80 app:id/player_minimal_time_bar}, size: 600 x 12
    child view 5: qrs{b2ef0ba V.E...... ........ 0,0-600,330}, size: 600 x 330
        child view 0: android.widget.RelativeLayout{d42232a V.E...... ........ 0,0-600,330 #7f0b03e0 app:id/controls_layout}, size: 600 x 330
            child view 0: androidx.appcompat.widget.AppCompatTextView{85bf51b G.ED..... ......I. 0,0-0,0 #7f0b0a7a app:id/player_error_view}, size: 0 x 0
            child view 1: android.view.View{1b7c6b8 G.ED..... ......I. 0,0-0,0 #7f0b01bc app:id/bottom_bar_background}, size: 0 x 0
            child view 2: android.view.View{41bdc91 G.ED..... ......I. 0,0-0,0 #7f0b0fe1 app:id/top_bar_background}, size: 0 x 0
            child view 3: androidx.appcompat.widget.AppCompatTextView{1ae61b0 G.ED..C.. ......I. 0,0-0,0 #7f0b0cd1 app:id/related_videos_screen_button}, size: 0 x 0
            child view 4: android.widget.RelativeLayout{63747f6 V.E...... ........ 0,0-600,330 #7f0b01bd app:id/bottom_bar_container}, size: 600 x 330
                child view 0: android.widget.LinearLayout{bbac7f7 G.E...... ......I. 0,0-0,0 #7f0b01c5 app:id/bottom_end_container}, size: 0 x 0
                    child view 0: com.google.android.libraries.youtube.common.ui.TouchImageView{77e9664 GFED..C.. ......I. 0,0-0,0 #7f0b06b5 app:id/hide_controls_button}, size: 0 x 0
                    child view 1: com.google.android.libraries.youtube.common.ui.TouchImageView{c8607ae G.ED..C.. ......I. 0,0-0,0 #7f0b00e7 app:id/api_watch_in_youtube_button}, size: 0 x 0
                    child view 2: com.google.android.libraries.youtube.common.ui.TouchImageView{810b0cd VFED..C.. ......ID 0,0-0,0 #7f0b062f app:id/fullscreen_button}, size: 0 x 0
                child view 1: android.widget.LinearLayout{87ed82 V.E...... ......ID 0,330-600,330 #7f0b0f9b app:id/time_bar_container}, size: 600 x 0
                    child view 0: androidx.appcompat.widget.AppCompatTextView{8c9ec93 G.ED..C.. ......ID 0,0-0,0 #7f0b0802 app:id/live_label}, size: 0 x 0
                    child view 1: com.google.android.libraries.youtube.player.features.overlay.timebar.TimeBar{c0b4d0 G..D..... ......I. 0,0-0,0 #7f0b0f98 app:id/time_bar}, size: 0 x 0
            child view 5: com.google.android.libraries.youtube.common.ui.TouchImageView{2048cc9 GFED..C.. ......I. 0,0-0,0 #7f0b0a82 app:id/player_overflow_button}, size: 0 x 0
            child view 6: com.google.android.libraries.youtube.common.ui.TouchImageView{e1dd762 GFED..C.. ......I. 0,0-0,0 #7f0b0a86 app:id/player_share_button}, size: 0 x 0
            child view 7: androidx.appcompat.widget.AppCompatTextView{b4b5fce GFED..C.. ......I. 0,0-0,0 #7f0b0a7d app:id/player_learn_more_button}, size: 0 x 0
            child view 8: com.google.android.libraries.youtube.common.ui.TouchImageView{d9cbeef GFED..C.. ......I. 0,0-0,0 #7f0b0a75 app:id/player_control_play_pause_replay_button}, size: 0 x 0
            child view 9: com.google.android.libraries.youtube.common.ui.TouchImageView{9c44dfc GFED..C.. ......ID 0,0-0,0 #7f0b0a78 app:id/player_control_seekback_button}, size: 0 x 0
            child view 10: com.google.android.libraries.youtube.common.ui.TouchImageView{5402c85 GFED..C.. ......ID 0,0-0,0 #7f0b0a79 app:id/player_control_seekforward_button}, size: 0 x 0
            child view 11: com.google.android.libraries.youtube.common.ui.TouchImageView{dc1aada GF.D..C.. ......ID 0,0-0,0 #7f0b0a76 app:id/player_control_previous_button}, size: 0 x 0
            child view 12: com.google.android.libraries.youtube.common.ui.TouchImageView{c205b0b GF.D..C.. ......ID 0,0-0,0 #7f0b0a74 app:id/player_control_next_button}, size: 0 x 0
            child view 13: androidx.appcompat.widget.AppCompatTextView{f8fe8dc G.ED..C.. ......I. 0,0-0,0 #7f0b0a89 app:id/player_video_title_view}, size: 0 x 0
            child view 14: android.widget.ProgressBar{b064de8 G.ED..... ......ID 0,0-0,0 #7f0b0a7e app:id/player_loading_view}, size: 0 x 0
            child view 15: android.widget.FrameLayout{6dc0c01 V.E...... ........ 0,0-600,330 #7f0b051e app:id/embed_quickseek_container}, size: 600 x 330
                child view 0: com.google.android.libraries.youtube.player.features.quickseek.overlay.CircularClipTapBloomView{19853d6 G.ED..... ......I. 0,0-0,0 #7f0b0f32 app:id/tap_bloom_view}, size: 0 x 0
                child view 1: androidx.appcompat.widget.AppCompatImageView{4c6962d G.ED..... ......I. 0,0-0,0 #7f0b0452 app:id/dark_background}, size: 0 x 0
                child view 2: android.widget.LinearLayout{11f9aa6 V.E...... ........ 238,141-361,189 #7f0b05d7 app:id/fast_forward_rewind_triangles}, size: 123 x 48
                    child view 0: androidx.appcompat.widget.AppCompatImageView{df79ce7 I.ED..... ......I. 0,0-33,48 #7f0b0f16 app:id/swipe_triangle_left}, size: 33 x 48
                    child view 1: androidx.appcompat.widget.AppCompatImageView{5e66094 I.ED..... ......I. 45,0-78,48 #7f0b0f17 app:id/swipe_triangle_mid}, size: 33 x 48
                    child view 2: androidx.appcompat.widget.AppCompatImageView{afc673d I.ED..... ......I. 90,0-123,48 #7f0b0f18 app:id/swipe_triangle_right}, size: 33 x 48
                child view 3: com.google.android.libraries.youtube.common.ui.YouTubeTextView{7313544 G.ED..... ......I. 0,0-0,0 #7f0b05d6 app:id/fast_forward_rewind_hint_text}, size: 0 x 0
                child view 4: com.google.android.libraries.youtube.common.ui.YouTubeTextView{4eb1657 G.ED..... ......I. 0,0-0,0 #7f0b105d app:id/user_education_text_view}, size: 0 x 0
    child view 6: android.widget.FrameLayout{e884347 G.E...C.. ......ID 0,0-600,330}, size: 600 x 330
        child view 0: android.widget.RelativeLayout{b1abb32 V.E...... ......ID 0,0-600,330}, size: 600 x 330
            child view 0: com.google.android.libraries.youtube.common.ui.FixedAspectRatioRelativeLayout{a172083 V.E...... ......ID 0,40-600,290 #7f0b0cd0 app:id/related_video_fixed_aspect_ratio_layout}, size: 600 x 250
                child view 0: android.support.v7.widget.RecyclerView{12f200 VFED..... ......ID 0,72-600,242}, size: 600 x 170
            child view 1: androidx.appcompat.widget.AppCompatTextView{9cd3a39 V.ED..... ......ID 0,0-366,105 #7f0b0ccc app:id/related_overlay_title}, size: 366 x 105
            child view 2: com.google.android.libraries.youtube.common.ui.TouchImageView{535587e V.ED..C.. ......ID 456,0-600,144 #7f0b057b app:id/exit_related_page_button}, size: 144 x 144
    child view 7: android.widget.FrameLayout{989ad99 G.E...... ......ID 0,0-600,330}, size: 600 x 330
        child view 0: android.widget.RelativeLayout{82141df V.E...... ......ID 0,0-600,330}, size: 600 x 330
            child view 0: android.widget.LinearLayout{9b52e2c V.E...... ......ID 60,9-60,129 #7f0b00ac app:id/ad_title_layout}, size: 0 x 120
                child view 0: com.google.android.libraries.youtube.common.ui.CircularImageView{83e40f5 V.ED..C.. ......ID 120,12-216,108 #7f0b0f73 app:id/thumbnail}, size: 96 x 96
                child view 1: androidx.appcompat.widget.AppCompatTextView{cca7e8a G.ED..C.. ......ID 270,19-270,100 #7f0b0fb5 app:id/title}, size: 0 x 81
            child view 1: android.widget.FrameLayout{d821cfb V.E...C.. ......ID 0,186-309,330 #7f0b00aa app:id/ad_text_and_ad_choices_button}, size: 309 x 144
                child view 0: androidx.appcompat.widget.AppCompatTextView{a5d0118 V.ED..... ......ID 0,6-309,144 #7f0b00a9 app:id/ad_text}, size: 309 x 138
            child view 2: android.widget.FrameLayout{7ef771 V.E...C.. ......ID 180,162-600,306 #7f0b0e19 app:id/skip_ad_button}, size: 420 x 144
                child view 0: android.widget.LinearLayout{7d9f956 V.E...... ......ID 0,8-420,136}, size: 420 x 128
                    child view 0: androidx.appcompat.widget.AppCompatTextView{4b8dd7 V.ED..... ......ID 15,15-315,113 #7f0b0e1e app:id/skip_ad_text}, size: 300 x 98
                    child view 1: androidx.appcompat.widget.AppCompatImageView{ad16c4 V.ED..... ......ID 333,40-381,88 #7f0b0e1d app:id/skip_ad_icon}, size: 48 x 48
    child view 8: aazw{63a99ad G.E...... ......ID 0,0-600,330}, size: 600 x 330
        child view 0: android.widget.FrameLayout{a9454e2 V.E...... ......ID 0,0-600,330}, size: 600 x 330
            child view 0: androidx.appcompat.widget.AppCompatTextView{bd13073 G.ED..... ......I. 0,0-0,0 #7f0b08e0 app:id/minimize_survey}, size: 0 x 0
            child view 1: android.widget.LinearLayout{c6db30 V.E...... ......ID 36,0-564,330 #7f0b0991 app:id/normal_survey}, size: 528 x 330
                child view 0: androidx.appcompat.widget.AppCompatTextView{29423a9 V.ED..... ......ID 0,0-528,26 #7f0b0f11 app:id/survey_question}, size: 528 x 26
                child view 1: android.widget.LinearLayout{9a6dd2e V.E...... ......ID 0,26-528,83 #7f0b0f0a app:id/survey_answers_row_1}, size: 528 x 57
                    child view 0: androidx.appcompat.widget.AppCompatTextView{30460cf V.ED..C.. ......ID 0,0-258,57 #7f0b0f05 app:id/survey_answer_1}, size: 258 x 57
                    child view 1: androidx.appcompat.widget.AppCompatTextView{d767a5c V.ED..C.. ......ID 270,0-528,57 #7f0b0f06 app:id/survey_answer_2}, size: 258 x 57
                child view 2: android.widget.LinearLayout{3e25165 V.E...... ......ID 0,95-528,152 #7f0b0f0b app:id/survey_answers_row_2}, size: 528 x 57
                    child view 0: androidx.appcompat.widget.AppCompatTextView{5479e3a V.ED..C.. ......ID 0,0-258,57 #7f0b0f07 app:id/survey_answer_3}, size: 258 x 57
                    child view 1: androidx.appcompat.widget.AppCompatTextView{c903aeb V.ED..C.. ......ID 270,0-528,57 #7f0b0f08 app:id/survey_answer_4}, size: 258 x 57
                child view 3: android.widget.LinearLayout{81ee048 V.E...... ......ID 0,164-528,221 #7f0b0f0c app:id/survey_answers_row_3}, size: 528 x 57
                    child view 0: androidx.appcompat.widget.AppCompatTextView{d2b9ee1 V.ED..C.. ......ID 0,0-258,57 #7f0b0f09 app:id/survey_answer_5}, size: 258 x 57
                    child view 1: android.widget.FrameLayout{9016406 V.E...... ......ID 270,0-528,57}, size: 258 x 57
                        child view 0: androidx.appcompat.widget.AppCompatTextView{4b59ac7 G.ED..C.. ......I. 0,0-0,0 #7f0b0e20 app:id/skip_button}, size: 0 x 0
                        child view 1: androidx.appcompat.widget.AppCompatTextView{365b8f4 G.ED..C.. ......I. 0,0-0,0 #7f0b0ee2 app:id/submit_button}, size: 0 x 0
                child view 4: androidx.appcompat.widget.AppCompatTextView{162481d V..D..... ......ID 24,257-24,306 #7f0b0f0d app:id/survey_attribution}, size: 0 x 49
            child view 2: android.view.ViewStub{c3fba92 G.E...... ......I. 0,0-0,0 #7f0b0f0f app:id/survey_interstitial_stub}, size: 0 x 0
    child view 9: aoel{e71c63 G.E...... ......ID 0,0-600,330}, size: 600 x 330
        child view 0: androidx.appcompat.widget.AppCompatImageView{49f7060 V.ED..... ......ID 0,0-600,330 #7f0b080b app:id/livestream_channel_image}, size: 600 x 330
        child view 1: android.widget.RelativeLayout{6604919 V.E...... ......ID 0,162-600,330 #7f0b0812 app:id/livestream_offline_slate}, size: 600 x 168
            child view 0: androidx.appcompat.widget.AppCompatImageView{f9aedde V.ED..... ......ID 48,48-120,120 #7f0b080c app:id/livestream_icon}, size: 72 x 72
            child view 1: android.view.View{1251bbf V.ED..... ......ID 120,84-120,84 #7f0b0817 app:id/livestream_text_anchor}, size: 0 x 0
            child view 2: androidx.appcompat.widget.AppCompatTextView{ffb328c V.ED..... ......ID 168,27-168,84 #7f0b080d app:id/livestream_line1}, size: 0 x 57
            child view 3: androidx.appcompat.widget.AppCompatTextView{ba35dd5 V.ED..... ......ID 168,84-168,133 #7f0b080e app:id/livestream_line2}, size: 0 x 49
            child view 4: android.widget.LinearLayout{2e409ea V.E...C.. ......ID 277,35-552,132 #7f0b080f app:id/livestream_notification_button}, size: 275 x 97
                child view 0: androidx.appcompat.widget.AppCompatImageView{b19b4db V.ED..... ......ID 24,48-24,48 #7f0b0810 app:id/livestream_notification_icon}, size: 0 x 0
                child view 1: androidx.appcompat.widget.AppCompatTextView{b6eeb78 V.ED..... ......ID 36,0-251,97 #7f0b0811 app:id/livestream_notification_text}, size: 215 x 97
            child view 5: android.widget.LinearLayout{c90251 V.E...C.. ......ID 289,35-552,132 #7f0b0815 app:id/livestream_replay_button}, size: 263 x 97
                child view 0: androidx.appcompat.widget.AppCompatTextView{ff0dab6 V.ED..... ......ID 24,0-239,97 #7f0b0816 app:id/livestream_replay_text}, size: 215 x 97
        child view 2: android.widget.RelativeLayout{af4c3b7 G.E...... ......I. 0,0-0,0 #7f0b0813 app:id/livestream_offline_slate_collapsed}, size: 0 x 0
            child view 0: androidx.appcompat.widget.AppCompatTextView{4634724 V.ED..... ......I. 0,0-0,0 #7f0b0818 app:id/livestream_trailer_text}, size: 0 x 0
            child view 1: androidx.appcompat.widget.AppCompatImageView{7ca728d VFED..C.. ......I. 0,0-0,0 #7f0b0814 app:id/livestream_offline_slate_expand_button}, size: 0 x 0

 */
