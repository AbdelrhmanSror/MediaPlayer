package com.example.mediaplayer.audioPlayer


import android.app.Service
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import javax.inject.Inject


class AudioPlayer @Inject constructor(private val service: Service,
                                      private val mediaSessionCompat: MediaSessionCompat,
                                      var player: SimpleExoPlayer?,
                                      private val noisy: Noisy) :
        PlayerControlDelegate(service, player), AudioPlayerObservable {

    private var audioSessionId: Int = -1

    private val iPlayerState = ArrayList<IpLayerState?>()

    //to give flexibility if i want to do extra work while releasing the player like stopping service
    private var extraRelease: (() -> Unit)? = null

    //to see if the ui is visible or not
    private var isUiVisible = true

    var isPlaying = true

    var currentAudioIndex = -1
        private set

    private var durationSet: Boolean = false

    /**
     * to indicate if the player is released or not so when the ui is not visible we release the player
     * this is to avoid reinitializing the player again when user release the player and ui
     * is still visible so if he resume the player we do not have to initialize it again
     */
    private var isReleased = false

    private lateinit var runnable: Runnable

    private var handler: Handler? = null

    private val mediaSessionConnector: MediaSessionConnector by lazy {
        MediaSessionConnector(mediaSessionCompat)
    }



    companion object {
        fun create(application: Service, player: SimpleExoPlayer?, mediaSessionCompat: MediaSessionCompat, noisy: Noisy): AudioPlayer {
            return AudioPlayer(application, mediaSessionCompat, player, noisy)
        }
    }


    init {
        setOnPlayerStateChanged()
    }

    /**
     * to trigger call backs instantly once the the observer is attached
     *
     *[instantTrigger] set this if u enter ur activity or fragment from notification so u want to trigger callback to refresh ui.
     * if u set [instantTrigger] and player was not already playing it won't have any effect,it is only active if the player was playing
     *
     *this function will trigger these callbacks if possible
     * onPlayerStateChanged.onAudioChanged)
     * onPlayerStateChanged.onShuffleModeChanged
     * onPlayerStateChanged.onRepeatModeChanged
     * onPlayerStateChanged.onDurationChange
     *
     * warning :do not use [instantTrigger] other than that because it may trigger callback twice at same time which will lead to unwanted behaviour
     */
    private fun instantTrigger(ipLayerState: IpLayerState) {
        /**
         *audio player at initial state will return -1 because there is no audio is being played
         * also we just want to trigger these manually to refresh ui cause
         * if the audio is playing these events won't trigger until the events that trigger them happen
         * so to refresh ui when activity or fragment rebind with the service we have to do it manually
         * also we do it only if audio already playing otherwise we continue the normal flow
         */
        if (currentAudioIndex != -1) {
            Log.v("playerstate", "${player!!.currentWindowIndex}")
            notifyObserver(ipLayerState)

        }
    }


    /**
     * to register observer we need to give it the class that implement the interface of observer
     */
    override fun registerObserver(ipLayerState: IpLayerState) {
        instantTrigger(ipLayerState)
        setOnProgressChanged(ipLayerState)
        this.iPlayerState.add(ipLayerState)
    }

    /**
     * WARNING :DO NOT CALL [removeObserver] inside  [release]  OTHERWISE THERE WILL BE CRASHING
     *
     * call this if u just want to un register your observer,this will not release the player
     * if u want to release every thing use [release]
     * if you have registered to listen to progress this [removeObserver] will stop the progress
     */
    override fun removeObserver(ipLayerState: IpLayerState) {
        stopProgress()
        this.iPlayerState.remove(ipLayerState)
    }

    override fun notifyObserver(ipLayerState: IpLayerState) {
        ipLayerState.onAudioChanged(player!!.currentWindowIndex, player!!.playWhenReady)
        ipLayerState.onShuffleModeChanged(player!!.shuffleModeEnabled)
        ipLayerState.onRepeatModeChanged(player!!.repeatMode)
        ipLayerState.onDurationChange(player!!.duration)
        ipLayerState.onAudioSessionId(audioSessionId)


    }


    //handle the player when actions happen in notification
    private fun setOnPlayerStateChanged() {
        player!!.run {
            addListener(object : Player.EventListener {
                override fun onPositionDiscontinuity(reason: Int) {
                    if (player!!.isTrackChanging(reason)) {
                        Log.v("playbackstakestate", " tracking  stak${playbackState}  player  ${player!!.playbackError}")
                        currentAudioIndex = currentWindowIndex
                        durationSet = false
                        iPlayerState.forEach {
                            it?.onAudioChanged(currentWindowIndex, playWhenReady)
                        }
                        //triggerDurationCallback()
                    }
                }
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when {
                        playbackState == ExoPlayer.STATE_READY && !durationSet -> {
                            iPlayerState.forEach {
                                it?.onDurationChange(player!!.duration)
                            }
                            durationSet = true
                        }
                        player!!.isPlayerPlaying() -> {
                            Log.v("playbackstakestate", " playing  stak${playbackState}  player  ${player!!.playbackError}")
                            // Active playback.
                            //when player start again we start listening to  events of headphone
                            isReleased = false
                            isPlaying = true
                            if (isUiVisible)
                                resumeProgress()
                            noisy.register()
                            iPlayerState.forEach {
                                it?.onPlay()
                            }
                        }
                        playWhenReady -> {
                            // Not playing because playback ended, the player is buffering, stopped or
                            // failed. Check playbackState and player.getPlaybackError for details.
                            Log.v("playbackstakestate", " plaback stak${playbackState}  player  ${player!!.playbackError}")
                        }
                        player!!.isTracksEnded() -> {
                            iPlayerState.forEach {
                                it?.onAudioListCompleted()
                            }
                        }
                        player!!.isPlayerPausing() -> {
                            Log.v("playbackstakestate", " stopping stak${playbackState}  player  ${player!!.playbackError}")
                            if (playWhenReady != isPlaying) {
                                // Paused by app.
                                isReleased = false
                                isPlaying = false
                                pauseProgress()
                                //when player stop we stop listening to plug off events of headphone because player is already stopped
                                noisy.unregister()
                                iPlayerState.forEach {
                                    it?.onPause()
                                }
                            }
                        }
                    }
                }


                override fun onRepeatModeChanged(repeatMode: Int) {
                    iPlayerState.forEach {
                        it?.onRepeatModeChanged(repeatMode)
                    }
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    iPlayerState.forEach {
                        it?.onShuffleModeChanged(shuffleModeEnabled)
                    }
                }
            })
        }
        setAudioSessionChangeListener()
    }

    private fun Player.isPlayerPausing(): Boolean {
        return !playWhenReady && playbackState == Player.STATE_READY && playWhenReady != isPlaying
    }

    private fun Player.isPlayerPlaying(): Boolean {
        return playWhenReady && playbackState == Player.STATE_READY && playWhenReady != isPlaying

    }

    private fun Player.isTracksEnded(): Boolean {
        return playbackState == ExoPlayer.STATE_ENDED
    }

    private fun Player.isTrackChanging(reason: Int): Boolean {
        return reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason == Player.DISCONTINUITY_REASON_SEEK && currentWindowIndex != currentAudioIndex
    }

    private fun setAudioSessionChangeListener() {
        if (audioSessionId == -1) {
            player?.addAnalyticsListener(object : AnalyticsListener {

                /**
                 * Called when the audio session id is set.
                 *
                 * @param eventTime      The event time.
                 * @param audioSessionId The audio session id.
                 */
                override fun onAudioSessionId(eventTime: AnalyticsListener.EventTime?, audioSessionId: Int) {
                    this@AudioPlayer.audioSessionId = audioSessionId
                    iPlayerState.forEach {
                        it?.onAudioSessionId(audioSessionId)
                    }

                }
            })
        }

    }


    /**
     * getting the duration of current playing audio
     * to get duration it require player to be in ready state so we handle
     * this in runnable so to keep trying until getting the duration
     */
    private fun triggerDurationCallback() {
        val handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null
        runnable = Runnable {
            if (player?.playbackState == ExoPlayer.STATE_READY) {
                iPlayerState.forEach {
                    it?.onDurationChange(player!!.duration)
                }
            } else
                handler.postDelayed(runnable!!, 0)
        }
        handler.post(runnable)
        handler.postDelayed(runnable, 0)

    }


    private fun setOnProgressChanged(ipLayerState: IpLayerState) {
        ipLayerState.onProgressChangedLiveData(ProgressLiveData())
    }

    inner class ProgressLiveData : MutableLiveData<Long>() {
        init {
            handler = Handler()
            runnable = Runnable {
                player?.let {
                    // onPlayerStateChanged.onProgressChanged(player!!.currentPosition)
                    postValue(player!!.currentPosition)
                    //update the text position under seek bar to reflect the current position of seek bar
                    handler?.postDelayed(runnable, 50)
                }
            }
            handler?.postDelayed(runnable, 50)
        }

        /**
         *we do not update isUiVisisble here cause this variable
         * is actually represent the actual state of ui if its already visible or not visible
         * which means that ui is completely destroyed
         * so that is why we do not update this variable here because if the screen goes black
         * and user dismiss the notification the player would be released and we don not want this
         */
        override fun onInactive() {
            pauseProgress()

        }

        override fun onActive() {
            isUiVisible = true
            resumeProgress()
        }

    }

    private fun pauseProgress() {
        if (::runnable.isInitialized) {
            handler?.removeCallbacks(runnable)
        }
    }

    private fun resumeProgress() {
        if (::runnable.isInitialized) {
            handler?.post(runnable)
        }
    }


    private fun releasePlayerPermanently() {
        Log.v("playerrealsed", "done")
        extraRelease?.invoke()
        player!!.release()
        player = null
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null)
    }


    /**
     * if u want to release every thing use [release]
     *
     * this has no effect if the observer of progress is still attached
     *
     * this will delay the releasing of player until the observer of progress is not attached unless user resume the player again nothing wil happen
     *
     * use [release] if u have no observer for progress its effect will be executed immediately
     *
     * if there was observer of progress this will have no effect until the observer of progress get unsubscribed using [removeObserver]
     *
     *if u have progress observer and  u want to immediately release the player call [removeObserver] and [release] together
     *
     * WARNING :DO NOT CALL [removeObserver] inside  [release]  OTHERWISE THERE WILL BE CRASHING(STACK OVER FLOW)
     */
    fun release(extra: (() -> Unit)?) {
        extraRelease = extra
        player.let {
            if (!isUiVisible) {
                releasePlayerPermanently()
            } else {
                isReleased = true
            }
        }
    }

    /**
     * this will only be called if the ui is completely destroyed
     */
    private fun stopProgress() {
        isUiVisible = false
        pauseProgress()
        if (isReleased) {
            releasePlayerPermanently()
        }
        handler = null
    }

    /**
     * to control the player through headset or google assistant
     */
    fun enableCommandControl(mediaDescriptionCompat: (Int) -> MediaDescriptionCompat) {
        mediaSessionConnector.setPlayer(player)
        mediaSessionCompat.isActive = true
        val queueNavigator: TimelineQueueNavigator = object : TimelineQueueNavigator(mediaSessionCompat) {
            override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat {
                return mediaDescriptionCompat(windowIndex)
            }

        }
        mediaSessionConnector.setQueueNavigator(queueNavigator)
    }

}