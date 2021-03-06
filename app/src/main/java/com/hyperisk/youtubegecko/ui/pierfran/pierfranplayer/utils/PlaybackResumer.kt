package com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.utils

import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.PlayerConstants
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.YouTubePlayerInterface
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.listeners.AbstractYouTubePlayerListener


/**
 * Class responsible for resuming the playback state in case of network problems.
 * eg: player is playing -> network goes out -> player stops -> network comes back -> player resumes playback automatically.
 */
internal class PlaybackResumer : AbstractYouTubePlayerListener() {

    private var canLoad = false
    private var isPlaying = false
    private var error: PlayerConstants.PlayerError? = null

    private var currentVideoId: String? = null
    private var currentSecond: Float = 0f

    fun resume(youTubePlayer: YouTubePlayerInterface) {
        currentVideoId?.let { videoId ->
            if (isPlaying && error == PlayerConstants.PlayerError.HTML_5_PLAYER)
                youTubePlayer.loadOrCueVideo(canLoad, videoId, currentSecond)
            else if (!isPlaying && error == PlayerConstants.PlayerError.HTML_5_PLAYER)
                youTubePlayer.cueVideo(videoId, currentSecond)
        }

        error = null
    }

    override fun onStateChange(youTubePlayer: YouTubePlayerInterface, state: PlayerConstants.PlayerState) {
        when (state) {
            PlayerConstants.PlayerState.ENDED -> {
                isPlaying = false
                return
            }
            PlayerConstants.PlayerState.PAUSED -> {
                isPlaying = false
                return
            }
            PlayerConstants.PlayerState.PLAYING -> {
                isPlaying = true
                return
            }
            else -> { }
        }
    }

    override fun onError(youTubePlayer: YouTubePlayerInterface, error: PlayerConstants.PlayerError) {
        if (error == PlayerConstants.PlayerError.HTML_5_PLAYER)
            this.error = error
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayerInterface, second: Float) {
        currentSecond = second
    }

    override fun onVideoId(youTubePlayer: YouTubePlayerInterface, videoId: String) {
        currentVideoId = videoId
    }

    fun onLifecycleResume() {
        canLoad = true
    }

    fun onLifecycleStop() {
        canLoad = false
    }
}
