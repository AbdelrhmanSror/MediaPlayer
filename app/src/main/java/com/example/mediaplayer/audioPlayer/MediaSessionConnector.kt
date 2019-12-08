/*
package com.example.mediaplayer.audioPlayer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ShuffleMode
import android.util.Log
import android.util.Pair
import androidx.annotation.LongDef
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.DiscontinuityReason
import com.google.android.exoplayer2.Player.TimelineChangeReason
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.ErrorMessageProvider
import com.google.android.exoplayer2.util.RepeatModeUtil.RepeatToggleModes
import com.google.android.exoplayer2.util.Util
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

class MediaSessionConnector(
        */
/** The wrapped [MediaSessionCompat].  *//*

        val mediaSession: MediaSessionCompat,val audioForegroundService: AudioForegroundService) {
    companion object {
        @MediaSessionConnector.PlaybackActions
        val ALL_PLAYBACK_ACTIONS = (PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_SEEK_TO
                or PlaybackStateCompat.ACTION_FAST_FORWARD
                or PlaybackStateCompat.ACTION_REWIND
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
        */
/** The default playback actions.  *//*

        @MediaSessionConnector.PlaybackActions
        val DEFAULT_PLAYBACK_ACTIONS = ALL_PLAYBACK_ACTIONS
        */
/** The default fast forward increment, in milliseconds.  *//*

        const val DEFAULT_FAST_FORWARD_MS = 15000
        */
/** The default rewind increment, in milliseconds.  *//*

        const val DEFAULT_REWIND_MS = 5000
        const val EXTRAS_PITCH = "EXO_PITCH"
        private const val BASE_PLAYBACK_ACTIONS = (PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                or PlaybackStateCompat.ACTION_SET_REPEAT_MODE)
        private const val BASE_MEDIA_SESSION_FLAGS = (MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        private const val EDITOR_MEDIA_SESSION_FLAGS = BASE_MEDIA_SESSION_FLAGS or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
        private val METADATA_EMPTY = MediaMetadataCompat.Builder().build()

        init {
            ExoPlayerLibraryInfo.registerModule("goog.exo.mediasession")
        }
    }

    */
/** Playback actions supported by the connector.  *//*

    @LongDef(flag = true, value = [PlaybackStateCompat.ACTION_PLAY_PAUSE, PlaybackStateCompat.ACTION_PLAY, PlaybackStateCompat.ACTION_PAUSE, PlaybackStateCompat.ACTION_SEEK_TO, PlaybackStateCompat.ACTION_FAST_FORWARD, PlaybackStateCompat.ACTION_REWIND, PlaybackStateCompat.ACTION_STOP, PlaybackStateCompat.ACTION_SET_REPEAT_MODE, PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE])
    @Retention(RetentionPolicy.SOURCE)
    annotation class PlaybackActions

    */
/** Receiver of media commands sent by a media controller.  *//*

    interface CommandReceiver {
        */
/**
 * See [MediaSessionCompat.Callback.onCommand]. The
 * receiver may handle the command, but is not required to do so. Changes to the player should
 * be made via the [ControlDispatcher].
 *
 * @param player The player connected to the media session.
 * @param controlDispatcher A [ControlDispatcher] that should be used for dispatching
 * changes to the player.
 * @param command The command name.
 * @param extras Optional parameters for the command, may be null.
 * @param cb A result receiver to which a result may be sent by the command, may be null.
 * @return Whether the receiver handled the command.
 *//*

        fun onCommand(
                player: Player?,
                controlDispatcher: ControlDispatcher?,
                command: String?,
                extras: Bundle?,
                cb: ResultReceiver?): Boolean
    }

    */
/** Interface to which playback preparation and play actions are delegated.  *//*

    interface PlaybackPreparer : CommandReceiver {
        */
/**
 * Returns the actions which are supported by the preparer. The supported actions must be a
 * bitmask combined out of [PlaybackStateCompat.ACTION_PREPARE], [ ][PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID], [ ][PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH], [ ][PlaybackStateCompat.ACTION_PREPARE_FROM_URI], [ ][PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID], [ ][PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH] and [ ][PlaybackStateCompat.ACTION_PLAY_FROM_URI].
 *
 * @return The bitmask of the supported media actions.
 *//*

        val supportedPrepareActions: Long

        */
/**
 * See [MediaSessionCompat.Callback.onPrepare].
 *
 * @param playWhenReady Whether playback should be started after preparation.
 *//*

        fun onPrepare(playWhenReady: Boolean)

        */
/**
 * See [MediaSessionCompat.Callback.onPrepareFromMediaId].
 *
 * @param mediaId The media id of the media item to be prepared.
 * @param playWhenReady Whether playback should be started after preparation.
 * @param extras A [Bundle] of extras passed by the media controller.
 *//*

        fun onPrepareFromMediaId(mediaId: String?, playWhenReady: Boolean, extras: Bundle?)

        */
/**
 * See [MediaSessionCompat.Callback.onPrepareFromSearch].
 *
 * @param query The search query.
 * @param playWhenReady Whether playback should be started after preparation.
 * @param extras A [Bundle] of extras passed by the media controller.
 *//*

        fun onPrepareFromSearch(query: String?, playWhenReady: Boolean, extras: Bundle?)

        */
/**
 * See [MediaSessionCompat.Callback.onPrepareFromUri].
 *
 * @param uri The [Uri] of the media item to be prepared.
 * @param playWhenReady Whether playback should be started after preparation.
 * @param extras A [Bundle] of extras passed by the media controller.
 *//*

        fun onPrepareFromUri(uri: Uri?, playWhenReady: Boolean, extras: Bundle?)

        companion object {
            const val ACTIONS = (PlaybackStateCompat.ACTION_PREPARE
                    or PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID
                    or PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH
                    or PlaybackStateCompat.ACTION_PREPARE_FROM_URI
                    or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                    or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                    or PlaybackStateCompat.ACTION_PLAY_FROM_URI)
        }
    }

    */
/**
 * Handles queue navigation actions, and updates the media session queue by calling `MediaSessionCompat.setQueue()`.
 *//*

    interface QueueNavigator : CommandReceiver {
        */
/**
 * Returns the actions which are supported by the navigator. The supported actions must be a
 * bitmask combined out of [PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM], [ ][PlaybackStateCompat.ACTION_SKIP_TO_NEXT], [ ][PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS].
 *
 * @param player The player connected to the media session.
 * @return The bitmask of the supported media actions.
 *//*

        fun getSupportedQueueNavigatorActions(player: Player?): Long

        */
/**
 * Called when the timeline of the player has changed.
 *
 * @param player The player connected to the media session.
 *//*

        fun onTimelineChanged(player: Player?)

        */
/**
 * Called when the current window index changed.
 *
 * @param player The player connected to the media session.
 *//*

        fun onCurrentWindowIndexChanged(player: Player?)

        */
/**
 * Gets the id of the currently active queue item, or [ ][MediaSessionCompat.QueueItem.UNKNOWN_ID] if the active item is unknown.
 *
 *
 * To let the connector publish metadata for the active queue item, the queue item with the
 * returned id must be available in the list of items returned by [ ][MediaControllerCompat.getQueue].
 *
 * @param player The player connected to the media session.
 * @return The id of the active queue item.
 *//*

        fun getActiveQueueItemId(player: Player?): Long

        */
/**
 * See [MediaSessionCompat.Callback.onSkipToPrevious].
 *
 * @param player The player connected to the media session.
 * @param controlDispatcher A [ControlDispatcher] that should be used for dispatching
 * changes to the player.
 *//*

        fun onSkipToPrevious(player: Player?, controlDispatcher: ControlDispatcher?)

        */
/**
 * See [MediaSessionCompat.Callback.onSkipToQueueItem].
 *
 * @param player The player connected to the media session.
 * @param controlDispatcher A [ControlDispatcher] that should be used for dispatching
 * changes to the player.
 *//*

        fun onSkipToQueueItem(player: Player?, controlDispatcher: ControlDispatcher?, id: Long)

        */
/**
 * See [MediaSessionCompat.Callback.onSkipToNext].
 *
 * @param player The player connected to the media session.
 * @param controlDispatcher A [ControlDispatcher] that should be used for dispatching
 * changes to the player.
 *//*

        fun onSkipToNext(player: Player?, controlDispatcher: ControlDispatcher?)

        companion object {
            const val ACTIONS = (PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        }
    }

    */
/** Handles media session queue edits.  *//*

    interface QueueEditor : CommandReceiver {
        */
/**
 * See [MediaSessionCompat.Callback.onAddQueueItem].
 *//*

        fun onAddQueueItem(player: Player?, description: MediaDescriptionCompat?)

        */
/**
 * See [MediaSessionCompat.Callback.onAddQueueItem].
 *//*

        fun onAddQueueItem(player: Player?, description: MediaDescriptionCompat?, index: Int)

        */
/**
 * See [MediaSessionCompat.Callback.onRemoveQueueItem].
 *//*

        fun onRemoveQueueItem(player: Player?, description: MediaDescriptionCompat?)
    }

    */
/** Callback receiving a user rating for the active media item.  *//*

    interface RatingCallback : CommandReceiver {
        */
/** See [MediaSessionCompat.Callback.onSetRating].  *//*

        fun onSetRating(player: Player?, rating: RatingCompat?)

        */
/** See [MediaSessionCompat.Callback.onSetRating].  *//*

        fun onSetRating(player: Player?, rating: RatingCompat?, extras: Bundle?)
    }

    */
/** Handles a media button event.  *//*

    interface MediaButtonEventHandler {
        */
/**
 * See [MediaSessionCompat.Callback.onMediaButtonEvent].
 *
 * @param player The [Player].
 * @param controlDispatcher A [ControlDispatcher] that should be used for dispatching
 * changes to the player.
 * @param mediaButtonEvent The [Intent].
 * @return True if the event was handled, false otherwise.
 *//*

        fun onMediaButtonEvent(
                player: Player?, controlDispatcher: ControlDispatcher?, mediaButtonEvent: Intent?): Boolean
    }

    */
/**
 * Provides a [PlaybackStateCompat.CustomAction] to be published and handles the action when
 * sent by a media controller.
 *//*

    interface CustomActionProvider {
        */
/**
 * Called when a custom action provided by this provider is sent to the media session.
 *
 * @param player The player connected to the media session.
 * @param controlDispatcher A [ControlDispatcher] that should be used for dispatching
 * changes to the player.
 * @param action The name of the action which was sent by a media controller.
 * @param extras Optional extras sent by a media controller.
 *//*

        fun onCustomAction(
                player: Player?, controlDispatcher: ControlDispatcher?, action: String?, extras: Bundle?)

        */
/**
 * Returns a [PlaybackStateCompat.CustomAction] which will be published to the media
 * session by the connector or `null` if this action should not be published at the given
 * player state.
 *
 * @param player The player connected to the media session.
 * @return The custom action to be included in the session playback state or `null`.
 *//*

        fun getCustomAction(player: Player?): PlaybackStateCompat.CustomAction?
    }

    */
/** Provides a [MediaMetadataCompat] for a given player state.  *//*

    interface MediaMetadataProvider {
        */
/**
 * Gets the [MediaMetadataCompat] to be published to the session.
 *
 *
 * An app may need to load metadata resources like artwork bitmaps asynchronously. In such a
 * case the app should return a [MediaMetadataCompat] object that does not contain these
 * resources as a placeholder. The app should start an asynchronous operation to download the
 * bitmap and put it into a cache. Finally, the app should call [ ][.invalidateMediaSessionMetadata]. This causes this callback to be called again and the app
 * can now return a [MediaMetadataCompat] object with all the resources included.
 *
 * @param player The player connected to the media session.
 * @return The [MediaMetadataCompat] to be published to the session.
 *//*

        fun getMetadata(player: Player): MediaMetadataCompat?
    }

    private val looper: Looper
    private val componentListener: ComponentListener
    private val commandReceivers: ArrayList<CommandReceiver?>
    private val customCommandReceivers: ArrayList<CommandReceiver>
    private var controlDispatcher: ControlDispatcher
    private var customActionProviders: Array<CustomActionProvider?>
    private var customActionMap: Map<String, CustomActionProvider?>
    private var mediaMetadataProvider: MediaMetadataProvider?
    private var player: Player? = null
    private var errorMessageProvider: ErrorMessageProvider<in ExoPlaybackException>? = null
    private var customError: Pair<Int, CharSequence>? = null
    private var customErrorExtras: Bundle? = null
    private var playbackPreparer: PlaybackPreparer? = null
    private var queueNavigator: QueueNavigator? = null
    private var queueEditor: QueueEditor? = null
    private var ratingCallback: RatingCallback? = null
    private var mediaButtonEventHandler: MediaButtonEventHandler? = null
    private var enabledPlaybackActions: Long
    private var rewindMs: Int
    private var fastForwardMs: Int
    */
/**
 * Sets the player to be connected to the media session. Must be called on the same thread that is
 * used to access the player.
 *
 * @param player The player to be connected to the `MediaSession`, or `null` to
 * disconnect the current player.
 *//*

    fun setPlayer(player: Player?) {
        Assertions.checkArgument(player == null || player.applicationLooper == looper)
        if (this.player != null) {
            this.player!!.removeListener(componentListener)
        }
        this.player = player
        player?.addListener(componentListener)
        invalidateMediaSessionPlaybackState()
        invalidateMediaSessionMetadata()
    }


    */
/**
 * Sets the [QueueNavigator] to handle queue navigation actions `ACTION_SKIP_TO_NEXT`,
 * `ACTION_SKIP_TO_PREVIOUS` and `ACTION_SKIP_TO_QUEUE_ITEM`.
 *
 * @param queueNavigator The queue navigator.
 *//*

    fun setQueueNavigator(queueNavigator: QueueNavigator) {
        if (this.queueNavigator !== queueNavigator) {
            unregisterCommandReceiver(this.queueNavigator)
            this.queueNavigator = queueNavigator
            registerCommandReceiver(queueNavigator)
        }
    }


    */
/**
 * Updates the metadata of the media session.
 *
 *
 * Apps normally only need to call this method when the backing data for a given media item has
 * changed and the metadata should be updated immediately.
 *
 *
 * The [MediaMetadataCompat] which is published to the session is obtained by calling
 * [com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.MediaMetadataProvider.getMetadata].
 *//*

    fun invalidateMediaSessionMetadata() {
        val metadata = if (mediaMetadataProvider != null && player != null) mediaMetadataProvider!!.getMetadata(player!!) else METADATA_EMPTY
        mediaSession.setMetadata(metadata ?: METADATA_EMPTY)
    }

    */
/**
 * Updates the playback state of the media session.
 *
 *
 * Apps normally only need to call this method when the custom actions provided by a [ ] changed and the playback state needs to be updated immediately.
 *//*

    fun invalidateMediaSessionPlaybackState() {
        val builder = PlaybackStateCompat.Builder()
        if (player == null) {
            builder.setActions(buildPrepareActions()).setState(PlaybackStateCompat.STATE_NONE, 0, 0f, 0)
            mediaSession.setPlaybackState(builder.build())
            return
        }
        val currentActions: MutableMap<String, CustomActionProvider?> = HashMap()
        for (customActionProvider in customActionProviders) {
            val customAction = customActionProvider!!.getCustomAction(player)
            if (customAction != null) {
                currentActions[customAction.action] = customActionProvider
                builder.addCustomAction(customAction)
            }
        }
        customActionMap = Collections.unmodifiableMap(currentActions)
        val playbackState = player!!.playbackState
        val extras = Bundle()
        val playbackError = if (playbackState == Player.STATE_IDLE) player!!.playbackError else null
        val reportError = playbackError != null || customError != null
        val sessionPlaybackState = if (reportError) PlaybackStateCompat.STATE_ERROR else mapPlaybackState(player!!.playbackState, player!!.playWhenReady)
        if (customError != null) {
            builder.setErrorMessage(customError!!.first, customError!!.second)
            if (customErrorExtras != null) {
                extras.putAll(customErrorExtras)
            }
        } else if (playbackError != null && errorMessageProvider != null) {
            val message = errorMessageProvider!!.getErrorMessage(playbackError)
            builder.setErrorMessage(message.first, message.second)
        }
        val activeQueueItemId = if (queueNavigator != null) queueNavigator!!.getActiveQueueItemId(player) else MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong()
        extras.putFloat(EXTRAS_PITCH, player!!.playbackParameters.pitch)
        builder
                .setActions(buildPrepareActions() or buildPlaybackActions(player!!))
                .setActiveQueueItemId(activeQueueItemId)
                .setBufferedPosition(player!!.bufferedPosition)
                .setState(
                        sessionPlaybackState,
                        player!!.currentPosition,
                        player!!.playbackParameters.speed,
                        SystemClock.elapsedRealtime())
                .setExtras(extras)
        mediaSession.setPlaybackState(builder.build())
    }

    */
/**
 * Updates the queue of the media session by calling [ ][com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.QueueNavigator.onTimelineChanged].
 *
 *
 * Apps normally only need to call this method when the backing data for a given queue item has
 * changed and the queue should be updated immediately.
 *//*

    fun invalidateMediaSessionQueue() {
        if (queueNavigator != null && player != null) {
            queueNavigator!!.onTimelineChanged(player)
        }
    }


    private fun registerCommandReceiver(commandReceiver: CommandReceiver?) {
        if (!commandReceivers.contains(commandReceiver)) {
            commandReceivers.add(commandReceiver)
        }
    }

    private fun unregisterCommandReceiver(commandReceiver: CommandReceiver?) {
        commandReceivers.remove(commandReceiver)
    }

    private fun buildPrepareActions(): Long {
        return if (playbackPreparer == null) 0 else PlaybackPreparer.ACTIONS and playbackPreparer!!.supportedPrepareActions
    }

    private fun buildPlaybackActions(player: Player): Long {
        var enableSeeking = false
        var enableRewind = false
        var enableFastForward = false
        var enableSetRating = false
        val timeline = player.currentTimeline
        if (!timeline.isEmpty && !player.isPlayingAd) {
            enableSeeking = player.isCurrentWindowSeekable
            enableRewind = enableSeeking && rewindMs > 0
            enableFastForward = enableSeeking && fastForwardMs > 0
            enableSetRating = true
        }
        var playbackActions = BASE_PLAYBACK_ACTIONS
        if (enableSeeking) {
            playbackActions = playbackActions or PlaybackStateCompat.ACTION_SEEK_TO
        }
        if (enableFastForward) {
            playbackActions = playbackActions or PlaybackStateCompat.ACTION_FAST_FORWARD
        }
        if (enableRewind) {
            playbackActions = playbackActions or PlaybackStateCompat.ACTION_REWIND
        }
        playbackActions = playbackActions and enabledPlaybackActions
        var actions = playbackActions
        if (queueNavigator != null) {
            actions = actions or (QueueNavigator.ACTIONS and queueNavigator!!.getSupportedQueueNavigatorActions(player))
        }
        if (ratingCallback != null && enableSetRating) {
            actions = actions or PlaybackStateCompat.ACTION_SET_RATING
        }
        return actions
    }

    private fun mapPlaybackState(exoPlayerPlaybackState: Int, playWhenReady: Boolean): Int {
        return when (exoPlayerPlaybackState) {
            Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
            Player.STATE_READY -> if (playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            else -> PlaybackStateCompat.STATE_NONE
        }
    }

    private fun canDispatchPlaybackAction(action: Long): Boolean {
        return player != null && enabledPlaybackActions and action != 0L
    }

    private fun canDispatchToPlaybackPreparer(action: Long): Boolean {
        return (playbackPreparer != null
                && playbackPreparer!!.supportedPrepareActions and action != 0L)
    }

    private fun canDispatchToQueueNavigator(action: Long): Boolean {
        return player != null && queueNavigator != null && queueNavigator!!.getSupportedQueueNavigatorActions(player) and action != 0L
    }

    private fun canDispatchSetRating(): Boolean {
        return player != null && ratingCallback != null
    }

    private fun canDispatchQueueEdit(): Boolean {
        return player != null && queueEditor != null
    }

    private fun canDispatchMediaButtonEvent(): Boolean {
        return player != null && mediaButtonEventHandler != null
    }

    private fun rewind(player: Player?) {
        if (player!!.isCurrentWindowSeekable && rewindMs > 0) {
            seekTo(player, player.currentPosition - rewindMs)
        }
    }

    private fun fastForward(player: Player?) {
        if (player!!.isCurrentWindowSeekable && fastForwardMs > 0) {
            seekTo(player, player.currentPosition + fastForwardMs)
        }
    }

    private fun seekTo(player: Player?, positionMs: Long) {
        seekTo(player, player!!.currentWindowIndex, positionMs)
    }

    private fun seekTo(player: Player?, windowIndex: Int, positionMs: Long) {
        var positionMs = positionMs
        val durationMs = player!!.duration
        if (durationMs != C.TIME_UNSET) {
            positionMs = Math.min(positionMs, durationMs)
        }
        positionMs = Math.max(positionMs, 0)
        controlDispatcher.dispatchSeekTo(player, windowIndex, positionMs)
    }

    */
/**
 * Provides a default [MediaMetadataCompat] with properties and extras taken from the [ ] of the [MediaSessionCompat.QueueItem] of the active queue item.
 *//*

    class DefaultMediaMetadataProvider(
            private val mediaController: MediaControllerCompat, metadataExtrasPrefix: String?) : MediaMetadataProvider {
        private val metadataExtrasPrefix: String = metadataExtrasPrefix ?: ""
        override fun getMetadata(player: Player): MediaMetadataCompat? {
            if (player.currentTimeline.isEmpty) {
                return METADATA_EMPTY
            }
            val builder = MediaMetadataCompat.Builder()
            if (player.isPlayingAd) {
                builder.putLong(MediaMetadataCompat.METADATA_KEY_ADVERTISEMENT, 1)
            }
            builder.putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    if (player.duration == C.TIME_UNSET) -1 else player.duration)
            val activeQueueItemId = mediaController.playbackState.activeQueueItemId
            if (activeQueueItemId != MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong()) {
                val queue = mediaController.queue
                var i = 0
                while (queue != null && i < queue.size) {
                    val queueItem = queue[i]
                    if (queueItem.queueId == activeQueueItemId) {
                        val description = queueItem.description
                        val extras = description.extras
                        if (extras != null) {
                            for (key in extras.keySet()) {
                                val value = extras[key]
                                if (value is String) {
                                    builder.putString(metadataExtrasPrefix + key, value as String?)
                                } else if (value is CharSequence) {
                                    builder.putText(metadataExtrasPrefix + key, value as CharSequence?)
                                } else if (value is Long) {
                                    builder.putLong(metadataExtrasPrefix + key, (value as Long?)!!)
                                } else if (value is Int) {
                                    builder.putLong(metadataExtrasPrefix + key, value as Long)
                                } else if (value is Bitmap) {
                                    builder.putBitmap(metadataExtrasPrefix + key, value as Bitmap?)
                                } else if (value is RatingCompat) {
                                    builder.putRating(metadataExtrasPrefix + key, value as RatingCompat?)
                                }
                            }
                        }
                        if (description.title != null) {
                            val title = description.title.toString()
                            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                            builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                        }
                        if (description.subtitle != null) {
                            builder.putString(
                                    MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, description.subtitle.toString())
                        }
                        if (description.description != null) {
                            builder.putString(
                                    MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, description.description.toString())
                        }
                        if (description.iconBitmap != null) {
                            builder.putBitmap(
                                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, description.iconBitmap)
                        }
                        if (description.iconUri != null) {
                            builder.putString(
                                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, description.iconUri.toString())
                        }
                        if (description.mediaId != null) {
                            builder.putString(
                                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID, description.mediaId.toString())
                        }
                        if (description.mediaUri != null) {
                            builder.putString(
                                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI, description.mediaUri.toString())
                        }
                        break
                    }
                    i++
                }
            }
            return builder.build()
        }

    }

    private inner class ComponentListener : MediaSessionCompat.Callback(), Player.EventListener {
        private var currentWindowIndex = 0
        private var currentWindowCount = 0
        // Player.EventListener implementation.
        override fun onTimelineChanged(
                timeline: Timeline, manifest: Any?, @TimelineChangeReason reason: Int) {
            val windowCount = player!!.currentTimeline.windowCount
            val windowIndex = player!!.currentWindowIndex
            if (queueNavigator != null) {
                queueNavigator!!.onTimelineChanged(player)
                invalidateMediaSessionPlaybackState()
            } else if (currentWindowCount != windowCount || currentWindowIndex != windowIndex) { // active queue item and queue navigation actions may need to be updated
                invalidateMediaSessionPlaybackState()
            }
            currentWindowCount = windowCount
            currentWindowIndex = windowIndex
            invalidateMediaSessionMetadata()
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            invalidateMediaSessionPlaybackState()
        }

        override fun onRepeatModeChanged(@Player.RepeatMode repeatMode: Int) {
            mediaSession.setRepeatMode(
                    if (repeatMode == Player.REPEAT_MODE_ONE) PlaybackStateCompat.REPEAT_MODE_ONE else if (repeatMode == Player.REPEAT_MODE_ALL) PlaybackStateCompat.REPEAT_MODE_ALL else PlaybackStateCompat.REPEAT_MODE_NONE)
            invalidateMediaSessionPlaybackState()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            mediaSession.setShuffleMode(
                    if (shuffleModeEnabled) PlaybackStateCompat.SHUFFLE_MODE_ALL else PlaybackStateCompat.SHUFFLE_MODE_NONE)
            invalidateMediaSessionPlaybackState()
            invalidateMediaSessionQueue()
        }

        override fun onPositionDiscontinuity(@DiscontinuityReason reason: Int) {
            if (currentWindowIndex != player!!.currentWindowIndex) {
                if (queueNavigator != null) {
                    queueNavigator!!.onCurrentWindowIndexChanged(player)
                }
                currentWindowIndex = player!!.currentWindowIndex
                // Update playback state after queueNavigator.onCurrentWindowIndexChanged has been called
// and before updating metadata.
                invalidateMediaSessionPlaybackState()
                invalidateMediaSessionMetadata()
                return
            }
            invalidateMediaSessionPlaybackState()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            invalidateMediaSessionPlaybackState()
        }

        // MediaSessionCompat.Callback implementation.
        override fun onPlay() {
            if (canDispatchPlaybackAction(PlaybackStateCompat.ACTION_PLAY)) {
                if (player!!.playbackState == Player.STATE_IDLE) {
                    if (playbackPreparer != null) {
                        playbackPreparer!!.onPrepare( */
/* playWhenReady= *//*
true)
                    }
                } else if (player!!.playbackState == Player.STATE_ENDED) {
                    controlDispatcher.dispatchSeekTo(player, player!!.currentWindowIndex, C.TIME_UNSET)
                }
                controlDispatcher.dispatchSetPlayWhenReady(Assertions.checkNotNull(player),  */
/* playWhenReady= *//*
true)
                audioForegroundService.changeAudioState(false)

            }
        }

        override fun onPause() {
            if (canDispatchPlaybackAction(PlaybackStateCompat.ACTION_PAUSE)) {
                controlDispatcher.dispatchSetPlayWhenReady(player,  */
/* playWhenReady= *//*
false)
                audioForegroundService.changeAudioState(false)

            }
        }

        override fun onSeekTo(positionMs: Long) {
            if (canDispatchPlaybackAction(PlaybackStateCompat.ACTION_SEEK_TO)) {
                seekTo(player, positionMs)
            }
        }

        override fun onFastForward() {
            if (canDispatchPlaybackAction(PlaybackStateCompat.ACTION_FAST_FORWARD)) {
                fastForward(player)
            }
        }

        override fun onRewind() {
            if (canDispatchPlaybackAction(PlaybackStateCompat.ACTION_REWIND)) {
                rewind(player)
            }
        }

        override fun onStop() {
            if (canDispatchPlaybackAction(PlaybackStateCompat.ACTION_STOP)) {
                audioForegroundService.onStop()
                controlDispatcher.dispatchStop(player,  */
/* reset= *//*
false)
            }
        }

        override fun onSetShuffleMode(@ShuffleMode shuffleMode: Int) {
            if (canDispatchPlaybackAction(PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)) {
                val shuffleModeEnabled: Boolean = when (shuffleMode) {
                    PlaybackStateCompat.SHUFFLE_MODE_ALL, PlaybackStateCompat.SHUFFLE_MODE_GROUP -> true
                    PlaybackStateCompat.SHUFFLE_MODE_NONE, PlaybackStateCompat.SHUFFLE_MODE_INVALID -> false
                    else -> false
                }
                controlDispatcher.dispatchSetShuffleModeEnabled(player, shuffleModeEnabled)
            }
        }

        override fun onSetRepeatMode(@PlaybackStateCompat.RepeatMode mediaSessionRepeatMode: Int) {
            if (canDispatchPlaybackAction(PlaybackStateCompat.ACTION_SET_REPEAT_MODE)) {
                @RepeatToggleModes val repeatMode: Int = when (mediaSessionRepeatMode) {
                    PlaybackStateCompat.REPEAT_MODE_ALL, PlaybackStateCompat.REPEAT_MODE_GROUP -> Player.REPEAT_MODE_ALL
                    PlaybackStateCompat.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ONE
                    PlaybackStateCompat.REPEAT_MODE_NONE, PlaybackStateCompat.REPEAT_MODE_INVALID -> Player.REPEAT_MODE_OFF
                    else -> Player.REPEAT_MODE_OFF
                }
                controlDispatcher.dispatchSetRepeatMode(player, repeatMode)
            }
        }

        override fun onSkipToNext() {
            Log.v("playerControllerSession","yeaaaaaaaha")

            if (canDispatchToQueueNavigator(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)) {
                queueNavigator!!.onSkipToNext(player, controlDispatcher)
                audioForegroundService.goToNext(false)

            }
        }

        override fun onSkipToPrevious() {
            if (canDispatchToQueueNavigator(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)) {
                queueNavigator!!.onSkipToPrevious(player, controlDispatcher)
                audioForegroundService.goToPrevious(false)

            }
        }

        override fun onSkipToQueueItem(id: Long) {
            if (canDispatchToQueueNavigator(PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM)) {
                queueNavigator!!.onSkipToQueueItem(player, controlDispatcher, id)
            }
        }

        override fun onCustomAction(action: String, extras: Bundle?) {
            if (player != null && customActionMap.containsKey(action)) {
                customActionMap[action]!!.onCustomAction(player, controlDispatcher, action, extras)
                invalidateMediaSessionPlaybackState()
            }
        }

        override fun onCommand(command: String, extras: Bundle, cb: ResultReceiver) {
            if (player != null) {
                for (i in commandReceivers.indices) {
                    if (commandReceivers[i]!!.onCommand(player, controlDispatcher, command, extras, cb)) {
                        return
                    }
                }
                for (i in customCommandReceivers.indices) {
                    if (customCommandReceivers[i]
                                    .onCommand(player, controlDispatcher, command, extras, cb)) {
                        return
                    }
                }
            }
        }

        override fun onPrepare() {
            if (canDispatchToPlaybackPreparer(PlaybackStateCompat.ACTION_PREPARE)) {
                playbackPreparer!!.onPrepare( */
/* playWhenReady= *//*
false)
            }
        }

        override fun onPrepareFromMediaId(mediaId: String, extras: Bundle) {
            if (canDispatchToPlaybackPreparer(PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID)) {
                playbackPreparer!!.onPrepareFromMediaId(mediaId,  */
/* playWhenReady= *//*
false, extras)
            }
        }

        override fun onPrepareFromSearch(query: String, extras: Bundle) {
            if (canDispatchToPlaybackPreparer(PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH)) {
                playbackPreparer!!.onPrepareFromSearch(query,  */
/* playWhenReady= *//*
false, extras)
            }
        }

        override fun onPrepareFromUri(uri: Uri, extras: Bundle) {
            if (canDispatchToPlaybackPreparer(PlaybackStateCompat.ACTION_PREPARE_FROM_URI)) {
                playbackPreparer!!.onPrepareFromUri(uri,  */
/* playWhenReady= *//*
false, extras)
            }
        }

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
            if (canDispatchToPlaybackPreparer(PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)) {
                playbackPreparer!!.onPrepareFromMediaId(mediaId,  */
/* playWhenReady= *//*
true, extras)
            }
        }

        override fun onPlayFromSearch(query: String, extras: Bundle) {
            if (canDispatchToPlaybackPreparer(PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH)) {
                playbackPreparer!!.onPrepareFromSearch(query,  */
/* playWhenReady= *//*
true, extras)
            }
        }

        override fun onPlayFromUri(uri: Uri, extras: Bundle) {
            if (canDispatchToPlaybackPreparer(PlaybackStateCompat.ACTION_PLAY_FROM_URI)) {
                playbackPreparer!!.onPrepareFromUri(uri,  */
/* playWhenReady= *//*
true, extras)
            }
        }

        override fun onSetRating(rating: RatingCompat) {
            if (canDispatchSetRating()) {
                ratingCallback!!.onSetRating(player, rating)
            }
        }

        override fun onSetRating(rating: RatingCompat, extras: Bundle) {
            if (canDispatchSetRating()) {
                ratingCallback!!.onSetRating(player, rating, extras)
            }
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat) {
            if (canDispatchQueueEdit()) {
                queueEditor!!.onAddQueueItem(player, description)
            }
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat, index: Int) {
            if (canDispatchQueueEdit()) {
                queueEditor!!.onAddQueueItem(player, description, index)
            }
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat) {
            if (canDispatchQueueEdit()) {
                queueEditor!!.onRemoveQueueItem(player, description)
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val isHandled = (canDispatchMediaButtonEvent()
                    && mediaButtonEventHandler!!.onMediaButtonEvent(
                    player, controlDispatcher, mediaButtonEvent))
            return isHandled || super.onMediaButtonEvent(mediaButtonEvent)
        }
    }

    */
/**
 * Creates an instance.
 *
 * @param mediaSession The [MediaSessionCompat] to connect to.
 *//*

    init {
        looper = Util.getLooper()
        componentListener = ComponentListener()
        commandReceivers = ArrayList()
        customCommandReceivers = ArrayList()
        controlDispatcher = DefaultControlDispatcher()
        customActionProviders = arrayOfNulls(0)
        customActionMap = emptyMap()
        mediaMetadataProvider = DefaultMediaMetadataProvider(mediaSession.controller, null)
        enabledPlaybackActions = DEFAULT_PLAYBACK_ACTIONS
        rewindMs = DEFAULT_REWIND_MS
        fastForwardMs = DEFAULT_FAST_FORWARD_MS
        mediaSession.setFlags(BASE_MEDIA_SESSION_FLAGS)
        mediaSession.setCallback(componentListener, Handler(looper))

    }


}

*/
/**
 * An abstract implementation of the [MediaSessionConnector.QueueNavigator] that maps the
 * windows of a [Player]'s [Timeline] to the media session queue.
 *//*

abstract class TimelineQueueNavigator @JvmOverloads constructor
(mediaSession: MediaSessionCompat, maxQueueSize: Int = DEFAULT_MAX_QUEUE_SIZE)
    : com.example.mediaplayer.audioPlayer.MediaSessionConnector.QueueNavigator {
    private val mediaSession: MediaSessionCompat
    private val window: Timeline.Window
    private val maxQueueSize: Int
    private var activeQueueItemId: Long
    */
/**
 * Gets the [MediaDescriptionCompat] for a given timeline window index.
 *
 *
 * Often artworks and icons need to be loaded asynchronously. In such a case, return a [ ] without the images, load your images asynchronously off the main thread
 * and then call [MediaSessionConnector.invalidateMediaSessionQueue] to make the connector
 * update the queue by calling [.getMediaDescription] again.
 *
 * @param player The current player.
 * @param windowIndex The timeline window index for which to provide a description.
 * @return A [MediaDescriptionCompat].
 *//*

    abstract fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat?


    override fun getSupportedQueueNavigatorActions(player: Player?): Long {
        var enableSkipTo = false
        var enablePrevious = false
        var enableNext = false
        val timeline = player!!.currentTimeline
        if (!timeline.isEmpty && !player.isPlayingAd) {
            timeline.getWindow(player.currentWindowIndex, window)
            enableSkipTo = timeline.windowCount > 1
            enablePrevious = window.isSeekable || !window.isDynamic || player.hasPrevious()
            enableNext = window.isDynamic || player.hasNext()
        }
        var actions: Long = 0
        if (enableSkipTo) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
        }
        if (enablePrevious) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        }
        if (enableNext) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        }
        return actions
    }

    override fun onTimelineChanged(player: Player?) {
        publishFloatingQueueWindow(player!!)
    }

    override fun onCurrentWindowIndexChanged(player: Player?) {
        if (activeQueueItemId == MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong() || player!!.currentTimeline.windowCount > maxQueueSize) {
            publishFloatingQueueWindow(player!!)
        } else if (!player.currentTimeline.isEmpty) {
            activeQueueItemId = player.currentWindowIndex.toLong()
        }
    }

    override fun getActiveQueueItemId(player: Player?): Long {
        return activeQueueItemId
    }

    override fun onSkipToPrevious(player: Player?, controlDispatcher: ControlDispatcher?) {
        val timeline = player!!.currentTimeline
        if (timeline.isEmpty || player.isPlayingAd) {
            return
        }
        val windowIndex = player.currentWindowIndex
        timeline.getWindow(windowIndex, window)
        val previousWindowIndex = player.previousWindowIndex
        if (previousWindowIndex != C.INDEX_UNSET
                && (player.currentPosition <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                        || window.isDynamic && !window.isSeekable)) {
            controlDispatcher!!.dispatchSeekTo(player, previousWindowIndex, C.TIME_UNSET)
        } else {
            controlDispatcher!!.dispatchSeekTo(player, windowIndex, 0)
        }
    }

    override fun onSkipToQueueItem(player: Player?, controlDispatcher: ControlDispatcher?, id: Long) {
        val timeline = player!!.currentTimeline
        if (timeline.isEmpty || player.isPlayingAd) {
            return
        }
        val windowIndex = id.toInt()
        if (0 <= windowIndex && windowIndex < timeline.windowCount) {
            controlDispatcher!!.dispatchSeekTo(player, windowIndex, C.TIME_UNSET)
        }
    }

    override fun onSkipToNext(player: Player?, controlDispatcher: ControlDispatcher?) {
        val timeline = player!!.currentTimeline
        if (timeline.isEmpty || player.isPlayingAd) {
            return
        }
        val windowIndex = player.currentWindowIndex
        val nextWindowIndex = player.nextWindowIndex
        if (nextWindowIndex != C.INDEX_UNSET) {
            controlDispatcher!!.dispatchSeekTo(player, nextWindowIndex, C.TIME_UNSET)
        } else if (timeline.getWindow(windowIndex, window).isDynamic) {
            controlDispatcher!!.dispatchSeekTo(player, windowIndex, C.TIME_UNSET)
        }
    }

    // CommandReceiver implementation.
    override fun onCommand(
            player: Player?,
            controlDispatcher: ControlDispatcher?,
            command: String?,
            extras: Bundle?,
            cb: ResultReceiver?): Boolean {
        return false
    }

    // Helper methods.
    private fun publishFloatingQueueWindow(player: Player) {
        val timeline = player.currentTimeline
        if (timeline.isEmpty) {
            mediaSession.setQueue(emptyList())
            activeQueueItemId = MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong()
            return
        }
        val queue = ArrayDeque<MediaSessionCompat.QueueItem>()
        val queueSize = Math.min(maxQueueSize, timeline.windowCount)
        // Add the active queue item.
        val currentWindowIndex = player.currentWindowIndex
        queue.add(
                MediaSessionCompat.QueueItem(
                        getMediaDescription(player, currentWindowIndex), currentWindowIndex.toLong()))
        // Fill queue alternating with next and/or previous queue items.
        var firstWindowIndex = currentWindowIndex
        var lastWindowIndex = currentWindowIndex
        val shuffleModeEnabled = player.shuffleModeEnabled
        while ((firstWindowIndex != C.INDEX_UNSET || lastWindowIndex != C.INDEX_UNSET)
                && queue.size < queueSize) { // Begin with next to have a longer tail than head if an even sized queue needs to be trimmed.
            if (lastWindowIndex != C.INDEX_UNSET) {
                lastWindowIndex = timeline.getNextWindowIndex(
                        lastWindowIndex, Player.REPEAT_MODE_OFF, shuffleModeEnabled)
                if (lastWindowIndex != C.INDEX_UNSET) {
                    queue.add(
                            MediaSessionCompat.QueueItem(
                                    getMediaDescription(player, lastWindowIndex), lastWindowIndex.toLong()))
                }
            }
            if (firstWindowIndex != C.INDEX_UNSET && queue.size < queueSize) {
                firstWindowIndex = timeline.getPreviousWindowIndex(
                        firstWindowIndex, Player.REPEAT_MODE_OFF, shuffleModeEnabled)
                if (firstWindowIndex != C.INDEX_UNSET) {
                    queue.addFirst(
                            MediaSessionCompat.QueueItem(
                                    getMediaDescription(player, firstWindowIndex), firstWindowIndex.toLong()))
                }
            }
        }
        mediaSession.setQueue(ArrayList(queue))
        activeQueueItemId = currentWindowIndex.toLong()
    }

    companion object {
        const val MAX_POSITION_FOR_SEEK_TO_PREVIOUS: Long = 3000
        const val DEFAULT_MAX_QUEUE_SIZE = 10
    }
    */
/**
 * Creates an instance for a given [MediaSessionCompat] and maximum queue size.
 *
 *
 * If the number of windows in the [Player]'s [Timeline] exceeds `maxQueueSize`,
 * the media session queue will correspond to `maxQueueSize` windows centered on the one
 * currently being played.
 *
 * @param mediaSession The [MediaSessionCompat].
 * @param maxQueueSize The maximum queue size.
 *//*

    */
/**
 * Creates an instance for a given [MediaSessionCompat].
 *
 *
 * Equivalent to `TimelineQueueNavigator(mediaSession, DEFAULT_MAX_QUEUE_SIZE)`.
 *
 * @param mediaSession The [MediaSessionCompat].
 *//*

    init {
        Assertions.checkState(maxQueueSize > 0)
        this.mediaSession = mediaSession
        this.maxQueueSize = maxQueueSize
        activeQueueItemId = MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong()
        window = Timeline.Window()
    }
}

*/
