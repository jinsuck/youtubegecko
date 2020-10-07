package com.hyperisk.youtubegecko.ui.pierfran.pierfranui.utils

import android.animation.Animator
import android.view.View
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.PlayerConstants
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.YouTubePlayerInterface
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.listeners.YouTubePlayerListener

class FadeViewHelper(val targetView: View): YouTubePlayerListener {
    companion object {
        const val  DEFAULT_ANIMATION_DURATION = 300L
        const val  DEFAULT_FADE_OUT_DELAY = 3000L
    }

    private var isPlaying = false

    private var canFade = false
    private var isVisible = true

    private var fadeOut: Runnable = Runnable{ fade(0f) }

    var isDisabled = false

    /**
     * Duration of the fade animation in milliseconds.
     */
    var animationDuration = DEFAULT_ANIMATION_DURATION

    /**
     * Delay after which the view automatically fades out.
     */
    var fadeOutDelay = DEFAULT_FADE_OUT_DELAY

    fun toggleVisibility() {
        fade(if (isVisible) 0f else 1f)
    }

    private fun fade(finalAlpha: Float) {
        if (!canFade || isDisabled)
            return

        isVisible = finalAlpha != 0f

        // if the controls are shown and the player is playing they should automatically fade after a while.
        // otherwise don't do anything automatically
        if (finalAlpha == 1f && isPlaying)
            targetView.handler?.postDelayed(fadeOut, fadeOutDelay)
        else
            targetView.handler?.removeCallbacks(fadeOut)

        targetView.animate()
                .alpha(finalAlpha)
                .setDuration(animationDuration)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {
                        if (finalAlpha == 1f) targetView.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animator: Animator) {
                        if (finalAlpha == 0f) targetView.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                }).start()
    }

    private fun updateState(state: PlayerConstants.PlayerState) {
        when (state) {
            PlayerConstants.PlayerState.ENDED -> isPlaying = false
            PlayerConstants.PlayerState.PAUSED -> isPlaying = false
            PlayerConstants.PlayerState.PLAYING -> isPlaying = true
            PlayerConstants.PlayerState.UNSTARTED -> { }
            else -> { }
        }
    }

    override fun onStateChange(youTubePlayer: YouTubePlayerInterface, state: PlayerConstants.PlayerState) {
        updateState(state)

        when(state) {
            PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.PAUSED, PlayerConstants.PlayerState.VIDEO_CUED -> {
                canFade = true
                if (state == PlayerConstants.PlayerState.PLAYING)
                    targetView.handler?.postDelayed(fadeOut, fadeOutDelay)
                else
                    targetView.handler?.removeCallbacks(fadeOut)
            }
            PlayerConstants.PlayerState.BUFFERING, PlayerConstants.PlayerState.UNSTARTED -> {
                fade(1f)
                canFade = false
            }
            PlayerConstants.PlayerState.UNKNOWN -> fade(1f)
            PlayerConstants.PlayerState.ENDED -> fade(1f)
        }
    }

    override fun onReady(youTubePlayer: YouTubePlayerInterface) { }
    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayerInterface, playbackQuality: PlayerConstants.PlaybackQuality) { }
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayerInterface, playbackRate: PlayerConstants.PlaybackRate) { }
    override fun onError(youTubePlayer: YouTubePlayerInterface, error: PlayerConstants.PlayerError) { }
    override fun onApiChange(youTubePlayer: YouTubePlayerInterface) { }
    override fun onCurrentSecond(youTubePlayer: YouTubePlayerInterface, second: Float) { }
    override fun onVideoDuration(youTubePlayer: YouTubePlayerInterface, duration: Float) { }
    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayerInterface, loadedFraction: Float) { }
    override fun onVideoId(youTubePlayer: YouTubePlayerInterface, videoId: String) { }
}