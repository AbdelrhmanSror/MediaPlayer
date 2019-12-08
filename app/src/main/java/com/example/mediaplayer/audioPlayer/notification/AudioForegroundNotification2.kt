package com.example.mediaplayer.audioPlayer.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BadgeIconType
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.CHANNEL_ID
import com.example.mediaplayer.NOTIFICATION_ID
import com.example.mediaplayer.R
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.model.SongModel
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


class AudioForegroundNotification2 private constructor(private val context: Service,
                                                       private val mediaSessionCompat: MediaSessionCompat
                                                       , player: Player,
                                                       private val songModel: List<SongModel>?) {
    private var playerNotificationManager: PlayerNotificationManager


    init {
        playerNotificationManager = PlayerNotificationManager(context, CHANNEL_ID, NOTIFICATION_ID,
                DescriptionAdapter(), object : PlayerNotificationManager.NotificationListener {


            override fun onNotificationPosted(notificationId: Int, notification: Notification?, ongoing: Boolean) {
                super.onNotificationPosted(notificationId, notification, ongoing)
                context.startForeground(notificationId, notification)

            }
        })

        playerNotificationManager.setMediaSessionToken(mediaSessionCompat.sessionToken)
        playerNotificationManager.setPlayer(player)
        playerNotificationManager.setColor(Color.BLACK)
        playerNotificationManager.setColorized(true)
        playerNotificationManager.setUseNavigationActionsInCompactView(true)
        playerNotificationManager.setUseChronometer(true)
        playerNotificationManager.setSmallIcon(R.drawable.exo_notification_small_icon)
        playerNotificationManager.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
        playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


    }

    companion object {
        private lateinit var audioForegroundNotification2: AudioForegroundNotification2
        fun create(context: Service,
                   mediaSessionCompat: MediaSessionCompat,
                   player: AudioPlayer,
                   songModel: List<SongModel>?): AudioForegroundNotification2 {
            return if (::audioForegroundNotification2.isInitialized) {
                audioForegroundNotification2
            } else
                AudioForegroundNotification2(context, mediaSessionCompat, player.player!!, songModel)
        }
    }

    fun release() {
        playerNotificationManager.setPlayer(null)
    }

    inner class DescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String? {
            val window = player.currentWindowIndex
            return songModel?.get(window)?.title
        }

        override fun getCurrentContentText(player: Player): String? {
            val window = player.currentWindowIndex
            return songModel?.get(window)?.actor
        }

        override fun getCurrentLargeIcon(player: Player,
                                         callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            val window = player.currentWindowIndex
            return BitmapFactory.decodeFile(songModel?.get(window)?.albumCoverUri)
                    ?: BitmapFactory.decodeResource(context.resources, R.drawable.default_image)
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val window = player.currentWindowIndex
            return NotificationActions.contentIntentNotification(context, window)
        }
    }
}


class PlayerNotificationManager @JvmOverloads constructor(
        context: Context,
        channelId: String?,
        notificationId: Int,
        mediaDescriptionAdapter: MediaDescriptionAdapter,
        notificationListener: NotificationListener? =  /* notificationListener= */
                null,
        customActionReceiver: CustomActionReceiver? =  /* customActionReceiver*/
                null) {
    /** An adapter to provide content assets of the media currently playing.  */
    interface MediaDescriptionAdapter {
        /**
         * Gets the content title for the current media item.
         *
         *
         * See [NotificationCompat.Builder.setContentTitle].
         *
         * @param player The [Player] for which a notification is being built.
         */
        fun getCurrentContentTitle(player: Player): String?

        /**
         * Creates a content intent for the current media item.
         *
         *
         * See [NotificationCompat.Builder.setContentIntent].
         *
         * @param player The [Player] for which a notification is being built.
         */
        fun createCurrentContentIntent(player: Player): PendingIntent?

        /**
         * Gets the content text for the current media item.
         *
         *
         * See [NotificationCompat.Builder.setContentText].
         *
         * @param player The [Player] for which a notification is being built.
         */
        fun getCurrentContentText(player: Player): String?

        /**
         * Gets the content sub text for the current media item.
         *
         *
         * See [NotificationCompat.Builder.setSubText].
         *
         * @param player The [Player] for which a notification is being built.
         */
        fun getCurrentSubText(player: Player): String? {
            return null
        }

        /**
         * Gets the large icon for the current media item.
         *
         *
         * When a bitmap initially needs to be asynchronously loaded, a placeholder (or null) can be
         * returned and the bitmap asynchronously passed to the [BitmapCallback] once it is
         * loaded. Because the adapter may be called multiple times for the same media item, the bitmap
         * should be cached by the app and whenever possible be returned synchronously at subsequent
         * calls for the same media item.
         *
         *
         * See [NotificationCompat.Builder.setLargeIcon].
         *
         * @param player The [Player] for which a notification is being built.
         * @param callback A [BitmapCallback] to provide a [Bitmap] asynchronously.
         */
        fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap?
    }

    /** Defines and handles custom actions.  */
    interface CustomActionReceiver {
        /**
         * Gets the actions handled by this receiver.
         *
         *
         * If multiple [PlayerNotificationManager] instances are in use at the same time, the
         * `instanceId` must be set as an intent extra with key [ ][PlayerNotificationManager.EXTRA_INSTANCE_ID] to avoid sending the action to every custom
         * action receiver. It's also necessary to ensure something is different about the actions. This
         * may be any of the [Intent] attributes considered by [Intent.filterEquals], or
         * different request code integers when creating the [PendingIntent]s with [ ][PendingIntent.getBroadcast]. The easiest approach is to use the `instanceId` as the
         * request code.
         *
         * @param context The [Context].
         * @param instanceId The instance id of the [PlayerNotificationManager].
         * @return A map of custom actions.
         */
        fun createCustomActions(context: Context?, instanceId: Int): Map<String, NotificationCompat.Action>?

        /**
         * Gets the actions to be included in the notification given the current player state.
         *
         * @param player The [Player] for which a notification is being built.
         * @return The actions to be included in the notification.
         */
        fun getCustomActions(player: Player?): ArrayList<String?>?

        /**
         * Called when a custom action has been received.
         *
         * @param player The player.
         * @param action The action from [Intent.getAction].
         * @param intent The received [Intent].
         */
        fun onCustomAction(player: Player?, action: String?, intent: Intent?)
    }

    /** A listener for changes to the notification.  */
    interface NotificationListener {
        /**
         * Called after the notification has been started.
         *
         * @param notificationId The id with which the notification has been posted.
         * @param notification The [Notification].
         */
        @Deprecated("Use {@link #onNotificationPosted(int, Notification, boolean)} instead.")
        fun onNotificationStarted(notificationId: Int, notification: Notification?) {
        }

        /**
         * Called after the notification has been cancelled.
         *
         * @param notificationId The id of the notification which has been cancelled.
         */
        @Deprecated("Use {@link #onNotificationCancelled(int, boolean)}.")
        fun onNotificationCancelled(notificationId: Int) {
        }

        /**
         * Called after the notification has been cancelled.
         *
         * @param notificationId The id of the notification which has been cancelled.
         * @param dismissedByUser `true` if the notification is cancelled because the user
         * dismissed the notification.
         */
        fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {}

        /**
         * Called each time after the notification has been posted.
         *
         *
         * For a service, the `ongoing` flag can be used as an indicator as to whether it
         * should be in the foreground.
         *
         * @param notificationId The id of the notification which has been posted.
         * @param notification The [Notification].
         * @param ongoing Whether the notification is ongoing.
         */
        fun onNotificationPosted(
                notificationId: Int, notification: Notification?, ongoing: Boolean) {
        }
    }

    /** Receives a [Bitmap].  */
    inner class BitmapCallback
    /** Create the receiver.  */(private val notificationTag: Int) {
        /**
         * Called when [Bitmap] is available.
         *
         * @param bitmap The bitmap to use as the large icon of the notification.
         */
        fun onBitmap(bitmap: Bitmap?) {
            if (bitmap != null) {
                mainHandler.post {
                    if (player != null && notificationTag == currentNotificationTag && isNotificationStarted) {
                        startOrUpdateNotification(bitmap)
                    }
                }
            }
        }

    }

    /**
     * Visibility of notification on the lock screen. One of [ ][NotificationCompat.VISIBILITY_PRIVATE], [NotificationCompat.VISIBILITY_PUBLIC] or [ ][NotificationCompat.VISIBILITY_SECRET].
     */
    @MustBeDocumented
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(NotificationCompat.VISIBILITY_PRIVATE, NotificationCompat.VISIBILITY_PUBLIC, NotificationCompat.VISIBILITY_SECRET)
    annotation class Visibility

    /**
     * Priority of the notification (required for API 25 and lower). One of [ ][NotificationCompat.PRIORITY_DEFAULT], [NotificationCompat.PRIORITY_MAX], [ ][NotificationCompat.PRIORITY_HIGH], [NotificationCompat.PRIORITY_LOW]or [ ][NotificationCompat.PRIORITY_MIN].
     */
    @MustBeDocumented
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(NotificationCompat.PRIORITY_DEFAULT, NotificationCompat.PRIORITY_MAX, NotificationCompat.PRIORITY_HIGH, NotificationCompat.PRIORITY_LOW, NotificationCompat.PRIORITY_MIN)
    annotation class Priority

    private val context: Context
    private val channelId: String?
    private val notificationId: Int
    private val mediaDescriptionAdapter: MediaDescriptionAdapter
    private val customActionReceiver: CustomActionReceiver?
    private val mainHandler: Handler
    private val notificationManager: NotificationManagerCompat
    private val intentFilter: IntentFilter
    private val playerListener: Player.EventListener
    private val notificationBroadcastReceiver: NotificationBroadcastReceiver
    private val playbackActions: Map<String, NotificationCompat.Action?>
    private val customActions: Map<String, NotificationCompat.Action?>
    private val dismissPendingIntent: PendingIntent
    private val instanceId: Int
    private val window: Timeline.Window
    private var builder: NotificationCompat.Builder? = null
    private var builderActions: ArrayList<NotificationCompat.Action>? = null
    private var player: Player? = null
    private var playbackPreparer: PlaybackPreparer? = null
    private var controlDispatcher: ControlDispatcher
    private var isNotificationStarted = false
    private var currentNotificationTag = 0
    private var notificationListener: NotificationListener?
    private var mediaSessionToken: MediaSessionCompat.Token? = null
    private var useNavigationActions: Boolean
    private var useNavigationActionsInCompactView = false
    private var usePlayPauseActions: Boolean
    private var useStopAction = false
    private var fastForwardMs: Long
    private var rewindMs: Long
    private var badgeIconType: Int
    private var colorized: Boolean
    private var defaults: Int
    private var color: Int
    @DrawableRes
    private var smallIconResourceId: Int
    private var visibility: Int
    @PlayerNotificationManager.Priority
    private var priority: Int
    private var useChronometer: Boolean
    private var wasPlayWhenReady = false
    private var lastPlaybackState = 0

    /**
     * Sets the [Player].
     *
     *
     * Setting the player starts a notification immediately unless the player is in [ ][Player.STATE_IDLE], in which case the notification is started as soon as the player transitions
     * away from being idle.
     *
     *
     * If the player is released it must be removed from the manager by calling `setPlayer(null)`. This will cancel the notification.
     *
     * @param player The [Player] to use, or `null` to remove the current player. Only
     * players which are accessed on the main thread are supported (`player.getApplicationLooper() == Looper.getMainLooper()`).
     */
    fun setPlayer(player: Player?) {
        Assertions.checkState(Looper.myLooper() == Looper.getMainLooper())
        Assertions.checkArgument(
                player == null || player.applicationLooper == Looper.getMainLooper())
        if (this.player === player) {
            return
        }
        if (this.player != null) {
            this.player!!.removeListener(playerListener)
            if (player == null) {
                stopNotification( /* dismissedByUser= */false)
            }
        }
        this.player = player
        if (player != null) {
            wasPlayWhenReady = player.playWhenReady
            lastPlaybackState = player.playbackState
            player.addListener(playerListener)
            startOrUpdateNotification()
        }
    }

    /**
     * Sets the [PlaybackPreparer].
     *
     * @param playbackPreparer The [PlaybackPreparer].
     */
    fun setPlaybackPreparer(playbackPreparer: PlaybackPreparer?) {
        this.playbackPreparer = playbackPreparer
    }

    /**
     * Sets the [ControlDispatcher].
     *
     * @param controlDispatcher The [ControlDispatcher], or null to use [     ].
     */
    fun setControlDispatcher(controlDispatcher: ControlDispatcher?) {
        this.controlDispatcher = controlDispatcher ?: DefaultControlDispatcher()
    }

    /**
     * Sets the [NotificationListener].
     *
     *
     * Please note that you should call this method before you call [.setPlayer] or
     * you may not get the [NotificationListener.onNotificationStarted]
     * called on your listener.
     *
     * @param notificationListener The [NotificationListener].
     */
    @Deprecated("Pass the notification listener to the constructor instead.")
    fun setNotificationListener(notificationListener: NotificationListener?) {
        this.notificationListener = notificationListener
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds. A value of zero will cause the
     * fast forward action to be disabled.
     */
    fun setFastForwardIncrementMs(fastForwardMs: Long) {
        if (this.fastForwardMs == fastForwardMs) {
            return
        }
        this.fastForwardMs = fastForwardMs
        invalidate()
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds. A value of zero will cause the rewind
     * action to be disabled.
     */
    fun setRewindIncrementMs(rewindMs: Long) {
        if (this.rewindMs == rewindMs) {
            return
        }
        this.rewindMs = rewindMs
        invalidate()
    }

    /**
     * Sets whether the navigation actions should be used.
     *
     * @param useNavigationActions Whether to use navigation actions or not.
     */
    fun setUseNavigationActions(useNavigationActions: Boolean) {
        if (this.useNavigationActions != useNavigationActions) {
            this.useNavigationActions = useNavigationActions
            invalidate()
        }
    }

    /**
     * Sets whether navigation actions should be displayed in compact view.
     *
     *
     * If [.useNavigationActions] is set to `false` navigation actions are displayed
     * neither in compact nor in full view mode of the notification.
     *
     * @param useNavigationActionsInCompactView Whether the navigation actions should be displayed in
     * compact view.
     */
    fun setUseNavigationActionsInCompactView(
            useNavigationActionsInCompactView: Boolean) {
        if (this.useNavigationActionsInCompactView != useNavigationActionsInCompactView) {
            this.useNavigationActionsInCompactView = useNavigationActionsInCompactView
            invalidate()
        }
    }

    /**
     * Sets whether the play and pause actions should be used.
     *
     * @param usePlayPauseActions Whether to use play and pause actions.
     */
    fun setUsePlayPauseActions(usePlayPauseActions: Boolean) {
        if (this.usePlayPauseActions != usePlayPauseActions) {
            this.usePlayPauseActions = usePlayPauseActions
            invalidate()
        }
    }

    /**
     * Sets whether the stop action should be used.
     *
     * @param useStopAction Whether to use the stop action.
     */
    fun setUseStopAction(useStopAction: Boolean) {
        if (this.useStopAction == useStopAction) {
            return
        }
        this.useStopAction = useStopAction
        invalidate()
    }

    /**
     * Sets the [MediaSessionCompat.Token].
     *
     * @param token The [MediaSessionCompat.Token].
     */
    fun setMediaSessionToken(token: MediaSessionCompat.Token?) {
        if (!Util.areEqual(mediaSessionToken, token)) {
            mediaSessionToken = token
            invalidate()
        }
    }


    fun setBadgeIconType(@BadgeIconType badgeIconType: Int) {
        if (this.badgeIconType == badgeIconType) {
            return
        }
        when (badgeIconType) {
            NotificationCompat.BADGE_ICON_NONE, NotificationCompat.BADGE_ICON_SMALL, NotificationCompat.BADGE_ICON_LARGE -> this.badgeIconType = badgeIconType
            else -> throw IllegalArgumentException()
        }
        invalidate()
    }

    fun setColorized(colorized: Boolean) {
        if (this.colorized != colorized) {
            this.colorized = colorized
            invalidate()
        }
    }

    fun setDefaults(defaults: Int) {
        if (this.defaults != defaults) {
            this.defaults = defaults
            invalidate()
        }
    }


    fun setColor(color: Int) {
        if (this.color != color) {
            this.color = color
            invalidate()
        }
    }


    fun setPriority(@PlayerNotificationManager.Priority priority: Int) {
        if (this.priority == priority) {
            return
        }
        when (priority) {
            NotificationCompat.PRIORITY_DEFAULT, NotificationCompat.PRIORITY_MAX, NotificationCompat.PRIORITY_HIGH, NotificationCompat.PRIORITY_LOW, NotificationCompat.PRIORITY_MIN -> this.priority = priority
            else -> throw IllegalArgumentException()
        }
        invalidate()
    }


    fun setSmallIcon(@DrawableRes smallIconResourceId: Int) {
        if (this.smallIconResourceId != smallIconResourceId) {
            this.smallIconResourceId = smallIconResourceId
            invalidate()
        }
    }

    fun setUseChronometer(useChronometer: Boolean) {
        if (this.useChronometer != useChronometer) {
            this.useChronometer = useChronometer
            invalidate()
        }
    }


    fun setVisibility(@PlayerNotificationManager.Visibility visibility: Int) {
        if (this.visibility == visibility) {
            return
        }
        when (visibility) {
            NotificationCompat.VISIBILITY_PRIVATE, NotificationCompat.VISIBILITY_PUBLIC, NotificationCompat.VISIBILITY_SECRET -> this.visibility = visibility
            else -> throw IllegalStateException()
        }
        invalidate()
    }

    /** Forces an update of the notification if already started.  */
    fun invalidate() {
        if (isNotificationStarted && player != null) {
            startOrUpdateNotification()
        }
    }

    private fun startOrUpdateNotification(): Notification? {
        Assertions.checkNotNull(player)
        return startOrUpdateNotification( /* bitmap= */null)
    }

    private fun startOrUpdateNotification(bitmap: Bitmap?): Notification? {
        val player = player
        val ongoing = getOngoing(player)
        builder = createNotification(player, builder, ongoing, bitmap)
        if (builder == null) {
            stopNotification( /* dismissedByUser= */false)
            return null
        }
        val notification = builder!!.build()
        notificationManager.notify(notificationId, notification)
        if (!isNotificationStarted) {
            isNotificationStarted = true
            context.registerReceiver(notificationBroadcastReceiver, intentFilter)
            if (notificationListener != null) {
                notificationListener!!.onNotificationStarted(notificationId, notification)
            }
        }
        val listener = notificationListener
        listener?.onNotificationPosted(notificationId, notification, ongoing)
        return notification
    }

    private fun stopNotification(dismissedByUser: Boolean) {
        if (isNotificationStarted) {
            isNotificationStarted = false
            notificationManager.cancel(notificationId)
            context.unregisterReceiver(notificationBroadcastReceiver)
            if (notificationListener != null) {
                notificationListener!!.onNotificationCancelled(notificationId, dismissedByUser)
                notificationListener!!.onNotificationCancelled(notificationId)
            }
        }
    }

    private fun createNotification(
            player: Player?,
            builder: NotificationCompat.Builder?,
            ongoing: Boolean,
            largeIcon: Bitmap?): NotificationCompat.Builder? {
        var mBuilder = builder
        var mLargeIcon = largeIcon
        if (player!!.playbackState == Player.STATE_IDLE) {
            builderActions = null
            return null
        }
        val actionNames = getActions(player)
        val actions = ArrayList<NotificationCompat.Action>(actionNames.size)
        for (i in actionNames.indices) {
            val actionName = actionNames[i]
            val action = if (playbackActions.containsKey(actionName)) playbackActions[actionName] else customActions[actionName]
            if (action != null) {
                actions.add(action)
            }
        }
        if (mBuilder == null || actions != builderActions) {
            mBuilder = NotificationCompat.Builder(context, channelId!!)
            builderActions = actions
            for (i in actions.indices) {
                mBuilder.addAction(actions[i])
            }
        }
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
        if (mediaSessionToken != null) {
            mediaStyle.setMediaSession(mediaSessionToken)
        }
        mediaStyle.setShowActionsInCompactView(*getActionIndicesForCompactView(actionNames, player))
        // Configure dismiss action prior to API 21 ('x' button).
        mediaStyle.setShowCancelButton(!ongoing)
        mediaStyle.setCancelButtonIntent(dismissPendingIntent)
        mBuilder.setStyle(mediaStyle)
        // Set intent which is sent if the user selects 'clear all'
        mBuilder.setDeleteIntent(dismissPendingIntent)
        // Set notification properties from getters.
        mBuilder
                .setBadgeIconType(badgeIconType)
                .setOngoing(ongoing)
                .setColor(color)
                .setColorized(colorized)
                .setSmallIcon(smallIconResourceId)
                .setVisibility(visibility)
                .setPriority(priority)
                .setDefaults(defaults)
        // Changing "showWhen" causes notification flicker if SDK_INT < 21.
        if (Util.SDK_INT >= 21 && useChronometer
                && !player.isPlayingAd
                && !player.isCurrentWindowDynamic
                && player.playWhenReady
                && player.playbackState == Player.STATE_READY) {
            mBuilder
                    .setWhen(System.currentTimeMillis() - player.contentPosition)
                    .setShowWhen(true)
                    .setUsesChronometer(true)
        } else {
            mBuilder.setShowWhen(false).setUsesChronometer(false)
        }
        // Set media specific notification properties from MediaDescriptionAdapter.
        mBuilder.setContentTitle(mediaDescriptionAdapter.getCurrentContentTitle(player))
        mBuilder.setContentText(mediaDescriptionAdapter.getCurrentContentText(player))
        mBuilder.setSubText(mediaDescriptionAdapter.getCurrentSubText(player))
        if (mLargeIcon == null) {
            mLargeIcon = mediaDescriptionAdapter.getCurrentLargeIcon(
                    player, BitmapCallback(++currentNotificationTag))
        }
        setLargeIcon(mBuilder, mLargeIcon)
        mBuilder.setContentIntent(mediaDescriptionAdapter.createCurrentContentIntent(player))
        return mBuilder
    }

    private fun getActions(player: Player?): List<String> {
        var enablePrevious = false
        var enableRewind = false
        var enableFastForward = false
        var enableNext = false
        val timeline = player!!.currentTimeline
        if (!timeline.isEmpty && !player.isPlayingAd) {
            timeline.getWindow(player.currentWindowIndex, window)
            enablePrevious = window.isSeekable || !window.isDynamic || player.hasPrevious()
            enableRewind = rewindMs > 0
            enableFastForward = fastForwardMs > 0
            enableNext = window.isDynamic || player.hasNext()
        }
        val stringActions = ArrayList<String>()
        if (useNavigationActions && enablePrevious) {
            stringActions.add(ACTION_PREVIOUS)
        }
        if (enableRewind) {
            stringActions.add(ACTION_REWIND)
        }
        if (usePlayPauseActions) {
            if (isPlaying(player)) {
                stringActions.add(ACTION_PAUSE)
            } else {
                stringActions.add(ACTION_PLAY)
            }
        }
        if (enableFastForward) {
            stringActions.add(ACTION_FAST_FORWARD)
        }
        if (useNavigationActions && enableNext) {
            stringActions.add(ACTION_NEXT)
        }
        if (customActionReceiver != null) {
            stringActions.addAll(customActionReceiver.getCustomActions(player) as Collection<String>)
        }
        if (useStopAction) {
            stringActions.add(ACTION_STOP)
        }
        return stringActions
    }


    private fun getActionIndicesForCompactView(actionNames: List<String>, player: Player?): IntArray {
        val pauseActionIndex = actionNames.indexOf(ACTION_PAUSE)
        val playActionIndex = actionNames.indexOf(ACTION_PLAY)
        val skipPreviousActionIndex = if (useNavigationActionsInCompactView) actionNames.indexOf(ACTION_PREVIOUS) else -1
        val skipNextActionIndex = if (useNavigationActionsInCompactView) actionNames.indexOf(ACTION_NEXT) else -1
        val actionIndices = IntArray(3)
        var actionCounter = 0
        if (skipPreviousActionIndex != -1) {
            actionIndices[actionCounter++] = skipPreviousActionIndex
        }
        val playWhenReady = player!!.playWhenReady
        if (pauseActionIndex != -1 && playWhenReady) {
            actionIndices[actionCounter++] = pauseActionIndex
        } else if (playActionIndex != -1 && !playWhenReady) {
            actionIndices[actionCounter++] = playActionIndex
        }
        if (skipNextActionIndex != -1) {
            actionIndices[actionCounter++] = skipNextActionIndex
        }
        return actionIndices.copyOf(actionCounter)
    }

    /** Returns whether the generated notification should be ongoing.  */
    private fun getOngoing(player: Player?): Boolean {
        val playbackState = player!!.playbackState
        return ((playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY)
                && player.playWhenReady)
    }

    private fun previous(player: Player) {
        val timeline = player.currentTimeline
        if (timeline.isEmpty || player.isPlayingAd) {
            return
        }
        val windowIndex = player.currentWindowIndex
        timeline.getWindow(windowIndex, window)
        val previousWindowIndex = player.previousWindowIndex
        if (previousWindowIndex != C.INDEX_UNSET
                && (player.currentPosition <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                        || window.isDynamic && !window.isSeekable)) {
            seekTo(player, previousWindowIndex, C.TIME_UNSET)
        } else {
            seekTo(player, 0)
        }
    }

    private fun next(player: Player) {
        val timeline = player.currentTimeline
        if (timeline.isEmpty || player.isPlayingAd) {
            return
        }
        val windowIndex = player.currentWindowIndex
        val nextWindowIndex = player.nextWindowIndex
        if (nextWindowIndex != C.INDEX_UNSET) {
            seekTo(player, nextWindowIndex, C.TIME_UNSET)
        } else if (timeline.getWindow(windowIndex, window).isDynamic) {
            seekTo(player, windowIndex, C.TIME_UNSET)
        }
    }

    private fun rewind(player: Player) {
        if (player.isCurrentWindowSeekable && rewindMs > 0) {
            seekTo(player, max(player.currentPosition - rewindMs, 0))
        }
    }

    private fun fastForward(player: Player) {
        if (player.isCurrentWindowSeekable && fastForwardMs > 0) {
            seekTo(player, player.currentPosition + fastForwardMs)
        }
    }

    private fun seekTo(player: Player, positionMs: Long) {
        seekTo(player, player.currentWindowIndex, positionMs)
    }

    private fun seekTo(player: Player, windowIndex: Int, positionMs: Long) {
        var mPositionMs = positionMs
        val duration = player.duration
        if (duration != C.TIME_UNSET) {
            mPositionMs = min(mPositionMs, duration)
        }
        mPositionMs = max(mPositionMs, 0)
        controlDispatcher.dispatchSeekTo(player, windowIndex, mPositionMs)
    }

    private fun isPlaying(player: Player?): Boolean {
        return player!!.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE && player.playWhenReady
    }

    private inner class PlayerListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (wasPlayWhenReady != playWhenReady || lastPlaybackState != playbackState) {
                startOrUpdateNotification()
                wasPlayWhenReady = playWhenReady
                lastPlaybackState = playbackState
            }
        }

        override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
            startOrUpdateNotification()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            startOrUpdateNotification()
        }

        override fun onPositionDiscontinuity(reason: Int) {
            startOrUpdateNotification()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            startOrUpdateNotification()
        }
    }

    private inner class NotificationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val player = player
            if (player == null || !isNotificationStarted
                    || intent.getIntExtra(EXTRA_INSTANCE_ID, instanceId) != instanceId) {
                return
            }
            val action = intent.action
            if (ACTION_PLAY == action) {
                if (player.playbackState == Player.STATE_IDLE) {
                    if (playbackPreparer != null) {
                        playbackPreparer!!.preparePlayback()
                    }
                } else if (player.playbackState == Player.STATE_ENDED) {
                    controlDispatcher.dispatchSeekTo(player, player.currentWindowIndex, C.TIME_UNSET)
                }
                controlDispatcher.dispatchSetPlayWhenReady(player,  /* playWhenReady= */true)
            } else if (ACTION_PAUSE == action) {
                controlDispatcher.dispatchSetPlayWhenReady(player,  /* playWhenReady= */false)
            } else if (ACTION_PREVIOUS == action) {
                previous(player)
            } else if (ACTION_REWIND == action) {
                rewind(player)
            } else if (ACTION_FAST_FORWARD == action) {
                fastForward(player)
            } else if (ACTION_NEXT == action) {
                next(player)
            } else if (ACTION_STOP == action) {
                controlDispatcher.dispatchStop(player,  /* reset= */true)
            } else if (ACTION_DISMISS == action) {
                stopNotification( /* dismissedByUser= */true)
            } else if (action != null && customActionReceiver != null && customActions.containsKey(action)) {
                customActionReceiver.onCustomAction(player, action, intent)
            }
        }
    }

    companion object {
        /** The action which starts playback.  */
        const val ACTION_PLAY = "com.google.android.exoplayer.play"
        /** The action which pauses playback.  */
        const val ACTION_PAUSE = "com.google.android.exoplayer.pause"
        /** The action which skips to the previous window.  */
        const val ACTION_PREVIOUS = "com.google.android.exoplayer.prev"
        /** The action which skips to the next window.  */
        const val ACTION_NEXT = "com.google.android.exoplayer.next"
        /** The action which fast forwards.  */
        const val ACTION_FAST_FORWARD = "com.google.android.exoplayer.ffwd"
        /** The action which rewinds.  */
        const val ACTION_REWIND = "com.google.android.exoplayer.rewind"
        /** The action which stops playback.  */
        const val ACTION_STOP = "com.google.android.exoplayer.stop"
        /** The extra key of the instance id of the player notification manager.  */
        const val EXTRA_INSTANCE_ID = "INSTANCE_ID"
        /**
         * The action which is executed when the notification is dismissed. It cancels the notification
         * and calls [NotificationListener.onNotificationCancelled].
         */
        private const val ACTION_DISMISS = "com.google.android.exoplayer.dismiss"
        /** The default fast forward increment, in milliseconds.  */
        const val DEFAULT_FAST_FORWARD_MS = 15000
        /** The default rewind increment, in milliseconds.  */
        const val DEFAULT_REWIND_MS = 5000
        private const val MAX_POSITION_FOR_SEEK_TO_PREVIOUS: Long = 3000
        private var instanceIdCounter = 0

        fun createWithNotificationChannel(
                context: Context?,
                channelId: String?,
                @StringRes channelName: Int,
                notificationId: Int,
                mediaDescriptionAdapter: MediaDescriptionAdapter?): PlayerNotificationManager {
            NotificationUtil.createNotificationChannel(
                    context, channelId, channelName, NotificationUtil.IMPORTANCE_LOW)
            return PlayerNotificationManager(
                    context!!, channelId, notificationId, mediaDescriptionAdapter!!)
        }


        fun createWithNotificationChannel(
                context: Context?,
                channelId: String?,
                @StringRes channelName: Int,
                notificationId: Int,
                mediaDescriptionAdapter: MediaDescriptionAdapter?,
                notificationListener: NotificationListener?): PlayerNotificationManager {
            NotificationUtil.createNotificationChannel(
                    context, channelId, channelName, NotificationUtil.IMPORTANCE_LOW)
            return PlayerNotificationManager(
                    context!!, channelId, notificationId, mediaDescriptionAdapter!!, notificationListener)
        }

        private fun createPlaybackActions(
                context: Context, instanceId: Int): Map<String, NotificationCompat.Action> {
            val actions: MutableMap<String, NotificationCompat.Action> = HashMap()
            actions[ACTION_PLAY] = NotificationCompat.Action(
                    R.drawable.vd_play_big,
                    context.getString(R.string.exo_controls_play_description),
                    createBroadcastIntent(ACTION_PLAY, context, instanceId))
            actions[ACTION_PAUSE] = NotificationCompat.Action(
                    R.drawable.vd_pause_big,
                    context.getString(R.string.exo_controls_pause_description),
                    createBroadcastIntent(ACTION_PAUSE, context, instanceId))
            actions[ACTION_STOP] = NotificationCompat.Action(
                    R.drawable.exo_notification_stop,
                    context.getString(R.string.exo_controls_stop_description),
                    createBroadcastIntent(ACTION_STOP, context, instanceId))
            actions[ACTION_REWIND] = NotificationCompat.Action(
                    R.drawable.exo_notification_rewind,
                    context.getString(R.string.exo_controls_rewind_description),
                    createBroadcastIntent(ACTION_REWIND, context, instanceId))
            actions[ACTION_FAST_FORWARD] = NotificationCompat.Action(
                    R.drawable.exo_notification_fastforward,
                    context.getString(R.string.exo_controls_fastforward_description),
                    createBroadcastIntent(ACTION_FAST_FORWARD, context, instanceId))
            actions[ACTION_PREVIOUS] = NotificationCompat.Action(
                    R.drawable.vd_skip_previous,
                    context.getString(R.string.exo_controls_previous_description),
                    createBroadcastIntent(ACTION_PREVIOUS, context, instanceId))
            actions[ACTION_NEXT] = NotificationCompat.Action(
                    R.drawable.vd_skip_next,
                    context.getString(R.string.exo_controls_next_description),
                    createBroadcastIntent(ACTION_NEXT, context, instanceId))
            return actions
        }

        private fun createBroadcastIntent(
                action: String, context: Context, instanceId: Int): PendingIntent {
            val intent = Intent(action).setPackage(context.packageName)
            intent.putExtra(EXTRA_INSTANCE_ID, instanceId)
            return PendingIntent.getBroadcast(
                    context, instanceId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun setLargeIcon(builder: NotificationCompat.Builder, largeIcon: Bitmap?) {
            builder.setLargeIcon(largeIcon)
        }
    }

    init {
        var mContext = context
        mContext = mContext.applicationContext
        this.context = mContext
        this.channelId = channelId
        this.notificationId = notificationId
        this.mediaDescriptionAdapter = mediaDescriptionAdapter
        this.notificationListener = notificationListener
        this.customActionReceiver = customActionReceiver
        controlDispatcher = DefaultControlDispatcher()
        window = Timeline.Window()
        instanceId = instanceIdCounter++
        mainHandler = Handler(Looper.getMainLooper())
        notificationManager = NotificationManagerCompat.from(mContext)
        playerListener = PlayerListener()
        notificationBroadcastReceiver = NotificationBroadcastReceiver()
        intentFilter = IntentFilter()
        useNavigationActions = true
        usePlayPauseActions = true
        colorized = true
        useChronometer = true
        color = Color.TRANSPARENT
        smallIconResourceId = R.drawable.exo_notification_small_icon
        defaults = 0
        priority = NotificationCompat.PRIORITY_LOW
        fastForwardMs = DEFAULT_FAST_FORWARD_MS.toLong()
        rewindMs = DEFAULT_REWIND_MS.toLong()
        badgeIconType = NotificationCompat.BADGE_ICON_SMALL
        visibility = NotificationCompat.VISIBILITY_PUBLIC
        // initialize actions
        playbackActions = createPlaybackActions(mContext, instanceId)
        for (action in playbackActions.keys) {
            intentFilter.addAction(action)
        }
        customActions = if (customActionReceiver != null) customActionReceiver.createCustomActions(mContext, instanceId)!! else emptyMap()
        for (action in customActions.keys) {
            intentFilter.addAction(action)
        }
        dismissPendingIntent = createBroadcastIntent(ACTION_DISMISS, mContext, instanceId)
        intentFilter.addAction(ACTION_DISMISS)
    }
}


