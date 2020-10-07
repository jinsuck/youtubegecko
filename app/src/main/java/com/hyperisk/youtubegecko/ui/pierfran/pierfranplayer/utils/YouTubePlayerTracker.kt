package com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.utils

import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.PlayerConstants
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.YouTubePlayerInterface
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.listeners.AbstractYouTubePlayerListener


/**
 * Utility class responsible for tracking the state of YouTubePlayer.
 * This is a YouTubePlayerListener, therefore to work it has to be added as listener to a YouTubePlayer.
 */
class YouTubePlayerTracker : AbstractYouTubePlayerListener() {
    /**
     * @return the player state. A value from [PlayerConstants.PlayerState]
     */
    var state: PlayerConstants.PlayerState = PlayerConstants.PlayerState.UNKNOWN
        private set
    var currentSecond: Float = 0f
        private set
    var videoDuration: Float = 0f
        private set
    var videoId: String? = null
        private set

    override fun onStateChange(youTubePlayer: YouTubePlayerInterface, state: PlayerConstants.PlayerState) {
        this.state = state
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayerInterface, second: Float) {
        currentSecond = second
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayerInterface, duration: Float) {
        videoDuration = duration
    }

    override fun onVideoId(youTubePlayer: YouTubePlayerInterface, videoId: String) {
        this.videoId = videoId
    }
}
