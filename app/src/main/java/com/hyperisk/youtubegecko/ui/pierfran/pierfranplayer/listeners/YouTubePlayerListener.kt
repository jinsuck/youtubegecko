package com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.listeners

import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.PlayerConstants
import com.hyperisk.youtubegecko.ui.pierfran.pierfranplayer.YouTubePlayerInterface


interface YouTubePlayerListener {
    /**
     * Called when the player is ready to play videos. You should start using with the player only after this method is called.
     * @param youTubePlayer The [YouTubePlayerInterface] object.
     */
    fun onReady(youTubePlayer: YouTubePlayerInterface)

    /**
     * Called every time the state of the player changes. Check [PlayerConstants.PlayerState] to see all the possible states.
     * @param state a state from [PlayerConstants.PlayerState]
     */
    fun onStateChange(youTubePlayer: YouTubePlayerInterface, state: PlayerConstants.PlayerState)

    /**
     * Called every time the quality of the playback changes. Check [PlayerConstants.PlaybackQuality] to see all the possible values.
     * @param playbackQuality a state from [PlayerConstants.PlaybackQuality]
     */
    fun onPlaybackQualityChange(youTubePlayer: YouTubePlayerInterface, playbackQuality: PlayerConstants.PlaybackQuality)

    /**
     * Called every time the speed of the playback changes. Check [PlayerConstants.PlaybackRate] to see all the possible values.
     * @param playbackRate a state from [PlayerConstants.PlaybackRate]
     */
    fun onPlaybackRateChange(youTubePlayer: YouTubePlayerInterface, playbackRate: PlayerConstants.PlaybackRate)

    /**
     * Called when an error occurs in the player. Check [PlayerConstants.PlayerError] to see all the possible values.
     * @param error a state from [PlayerConstants.PlayerError]
     */
    fun onError(youTubePlayer: YouTubePlayerInterface, error: PlayerConstants.PlayerError)

    /**
     * Called periodically by the player, the argument is the number of seconds that have been played.
     * @param second current second of the playback
     */
    fun onCurrentSecond(youTubePlayer: YouTubePlayerInterface, second: Float)

    /**
     * Called when the total duration of the video is loaded. <br></br><br></br>
     * Note that getDuration() will return 0 until the video's metadata is loaded, which normally happens just after the video starts playing.
     * @param duration total duration of the video
     */
    fun onVideoDuration(youTubePlayer: YouTubePlayerInterface, duration: Float)

    /**
     * Called periodically by the player, the argument is the percentage of the video that has been buffered.
     * @param loadedFraction a number between 0 and 1 that represents the percentage of the video that has been buffered.
     */
    fun onVideoLoadedFraction(youTubePlayer: YouTubePlayerInterface, loadedFraction: Float)

    /**
     * Called when the id of the current video is loaded
     * @param videoId the id of the video being played
     */
    fun onVideoId(youTubePlayer: YouTubePlayerInterface, videoId: String)

    fun onApiChange(youTubePlayer: YouTubePlayerInterface)
}
