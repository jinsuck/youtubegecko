package com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.listeners

import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.PlayerConstants
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.YouTubePlayerInterface


/**
 * Extend this class if you want to implement only some of the methods of [YouTubePlayerListener]
 */
abstract class AbstractYouTubePlayerListener : YouTubePlayerListener {
    override fun onReady(youTubePlayer: YouTubePlayerInterface) {}
    override fun onStateChange(youTubePlayer: YouTubePlayerInterface, state: PlayerConstants.PlayerState) {}
    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayerInterface, playbackQuality: PlayerConstants.PlaybackQuality) {}
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayerInterface, playbackRate: PlayerConstants.PlaybackRate) {}
    override fun onError(youTubePlayer: YouTubePlayerInterface, error: PlayerConstants.PlayerError) {}
    override fun onApiChange(youTubePlayer: YouTubePlayerInterface) {}
    override fun onCurrentSecond(youTubePlayer: YouTubePlayerInterface, second: Float) {}
    override fun onVideoDuration(youTubePlayer: YouTubePlayerInterface, duration: Float) {}
    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayerInterface, loadedFraction: Float) {}
    override fun onVideoId(youTubePlayer: YouTubePlayerInterface, videoId: String) {}
}
