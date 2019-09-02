package com.example.mediaplayer.chosenSong;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.core.app.NotificationManagerCompat;
import com.example.mediaplayer.playlist.PlayListModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import java.util.ArrayList;
import java.util.Objects;
import static com.example.mediaplayer.constants.ACTION_FOREGROUND;
import static com.example.mediaplayer.constants.CHOSEN_SONG_INDEX;
import static com.example.mediaplayer.constants.LIST_SONG;
import static com.example.mediaplayer.constants.NEXT_ACTION;
import static com.example.mediaplayer.constants.NOTIFICATION_ID;
import static com.example.mediaplayer.constants.PAUSE_ACTION;
import static com.example.mediaplayer.constants.PLAY_ACTION;
import static com.example.mediaplayer.constants.PREVIOUS_ACTION;

public class ChosenSongService extends Service {

    public ExoPlayer getPlayer() {
        return player;
    }

    private ExoPlayer player;
    private boolean mPlayWhenReady = true;
    private int currentWindowIndex = 0;
    private Context application;

    // indicates how to behave if the service is killed.
    int mStartMode = START_STICKY;

    // interface for clients that bind.
    IBinder mBinder = new SongBinder();
    //responsible for creating media player notification;
    NotificationController notificationController;
    //responsible for updating the notification
    NotificationManagerCompat mNotifymanager;
    //playList of songs
    ArrayList<PlayListModel> songList;

    @Override
    public void onCreate() {
        // The service is being created.
        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext());
        mNotifymanager = NotificationManagerCompat.from(ChosenSongService.this);
        application = getApplicationContext();

    }

    //creating concatenating media source for media player to play
    private MediaSource buildMediaSource(ArrayList<PlayListModel> audioUris) {
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, "MediaPlayer"));
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        if (audioUris == null) {
            return null;
        } else {
            for (PlayListModel item : audioUris) {
                concatenatingMediaSource.addMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(item.getAudioUri()));
            }
        }
        return concatenatingMediaSource;
    }

    public void setUpPlayer(ArrayList<PlayListModel> audioUris, int chosenSongIndex) {

        //to control to player the audio or video right now or wait user to play the audio himself
        player.setPlayWhenReady(true);
        MediaSource mediaSource = buildMediaSource(audioUris);
        player.prepare(mediaSource);
        //to control the starter location of audio
        player.seekTo(chosenSongIndex, 0);


    }
    private void handlePlayerEvent() {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (isPlayerStopped(playWhenReady)) {
                    mPlayWhenReady = false;
                    mNotifymanager.notify(NOTIFICATION_ID, notificationController.buildNotification(mPlayWhenReady, currentWindowIndex));

                } else if (isPlayerPlaying(playWhenReady)) {
                    mPlayWhenReady = true;
                    mNotifymanager.notify(NOTIFICATION_ID, notificationController.buildNotification(mPlayWhenReady, currentWindowIndex));


                } else if (isPlayerNext()) {
                    currentWindowIndex = player.getCurrentWindowIndex();
                    mNotifymanager.notify(NOTIFICATION_ID, notificationController.buildNotification(player.getPlayWhenReady(), currentWindowIndex));


                } else if (isPlayerPrevious()) {
                    currentWindowIndex = player.getCurrentWindowIndex();
                    mNotifymanager.notify(NOTIFICATION_ID, notificationController.buildNotification(player.getPlayWhenReady(), currentWindowIndex));

                }


            }

        });
    }

    private boolean isPlayerStopped(boolean playWhenReady) {
        return playWhenReady != mPlayWhenReady && !playWhenReady;
    }

    private boolean isPlayerPlaying(boolean playWhenReady) {
        return playWhenReady != mPlayWhenReady && playWhenReady;
    }

    private boolean isPlayerNext() {
        return player.getCurrentWindowIndex() != currentWindowIndex && player.getCurrentWindowIndex() < currentWindowIndex;

    }

    private boolean isPlayerPrevious() {
        return player.getCurrentWindowIndex() != currentWindowIndex && player.getCurrentWindowIndex() > currentWindowIndex;

    }


    class SongBinder extends Binder {
        ChosenSongService getService() {
            return ChosenSongService.this;
        }

    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case ACTION_FOREGROUND:
                    //getting the playlist of song from intent
                    songList = intent.getParcelableArrayListExtra(LIST_SONG);
                    //getting the current playing song index
                    currentWindowIndex = intent.getIntExtra(CHOSEN_SONG_INDEX, 0);
                    notificationController = new NotificationController(songList, this);
                    handlePlayerEvent();
                    startForeground(NOTIFICATION_ID, notificationController.buildNotification(mPlayWhenReady, currentWindowIndex));
                    break;
                case PAUSE_ACTION:
                    player.setPlayWhenReady(false);
                    break;
                case PLAY_ACTION:
                    player.setPlayWhenReady(true);
                    break;
                case PREVIOUS_ACTION:
                    player.previous();
                    break;
                case NEXT_ACTION:
                    player.next();
                    break;
            }

        }

    }

    @Override
    public int onStartCommand(Intent intent,
                              int flags, int startId) {
        // The service is starting, due to a call to startService().
        handleIntent(intent);
        return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    }

}
//playing an audio media on device ,app is using foreground service with banded service to gracefully handle any configuration changes and ipc.this app is using ExoPlayer library
