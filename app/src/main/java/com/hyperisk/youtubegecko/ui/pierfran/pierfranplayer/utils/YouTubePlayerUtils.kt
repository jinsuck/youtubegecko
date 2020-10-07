@file:JvmName("YouTubePlayerUtils")
package com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.utils

import androidx.lifecycle.Lifecycle
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.YouTubePlayerInterface

/**
 * Calls [YouTubePlayerInterface.cueVideo] or [YouTubePlayerInterface.loadVideo] depending on which one is more appropriate.
 * If it can't decide, calls [YouTubePlayerInterface.cueVideo] by default.
 *
 * In most cases you want to avoid calling [YouTubePlayerInterface.loadVideo] if the Activity/Fragment is not in the foreground.
 * This function automates these checks for you.
 * @param lifecycle the lifecycle of the Activity or Fragment containing the YouTubePlayerView.
 * @param videoId id of the video.
 * @param startSeconds the time from which the video should start playing.
 */
fun YouTubePlayerInterface.loadOrCueVideo(lifecycle: Lifecycle, videoId: String, startSeconds: Float) {
    loadOrCueVideo(lifecycle.currentState == Lifecycle.State.RESUMED, videoId, startSeconds)
}


@JvmSynthetic internal fun YouTubePlayerInterface.loadOrCueVideo(canLoad: Boolean, videoId: String, startSeconds: Float) {
    if (canLoad)
        loadVideo(videoId, startSeconds)
    else
        cueVideo(videoId, startSeconds)
}