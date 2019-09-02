package com.example.mediaplayer.chosenSong;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.mediaplayer.R;
import com.example.mediaplayer.playlist.PlayListModel;
import java.util.ArrayList;
import java.util.List;
import static com.example.mediaplayer.constants.AUDIO_FOREGROUND_NOTIFICATION;
import static com.example.mediaplayer.constants.CHANNEL_ID;
import static com.example.mediaplayer.constants.CHOSEN_SONG_INDEX;
import static com.example.mediaplayer.constants.LIST_SONG;
import static com.example.mediaplayer.constants.NEXT_ACTION;
import static com.example.mediaplayer.constants.NOTIFICATION_ID;
import static com.example.mediaplayer.constants.PAUSE_ACTION;
import static com.example.mediaplayer.constants.PLAY_ACTION;
import static com.example.mediaplayer.constants.PREVIOUS_ACTION;

 class NotificationController  {
    private List<PlayListModel> playListModels;
    private Context context;
     NotificationController(List<PlayListModel> playListModels, Context context)
    {
        this.playListModels=playListModels;
        this.context=context;
    }

    private  void createNotificationChannel() {
        NotificationManagerCompat notifManager = NotificationManagerCompat.from(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Create a NotificationController
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Media Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(null,null);
            notificationChannel.setVibrationPattern(null);
            notificationChannel.setDescription("MediaPlayer");
            notifManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     *
     * @param isPlaying variable indicates whether the current audio is playing or not
     * and based on it we chose to display play or pause drawable
     * @param chosenSongIndex indicates to the index of current playing song
     *
     * @return notification
     */
      Notification buildNotification(Boolean isPlaying,int chosenSongIndex){
        NotificationCompat.Action playPauseAction;
        PlayListModel item=playListModels.get(chosenSongIndex);
        if(isPlaying)
        {
            playPauseAction =actionPause();
        }
        else {
            playPauseAction =actionPlay();
        }
        Intent songFragmentIntent = new Intent(context, ChosenSongActivity.class);
        songFragmentIntent.setAction(AUDIO_FOREGROUND_NOTIFICATION);
        songFragmentIntent.putParcelableArrayListExtra(LIST_SONG,(ArrayList) playListModels);
        songFragmentIntent.putExtra(CHOSEN_SONG_INDEX,chosenSongIndex);
        //creating notification channel necessary for android version 26 and above
        createNotificationChannel();
        Bitmap albumCoverImage=BitmapFactory.decodeFile(item.getAlbumCoverUri());
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                // the metadata for the currently playing track
                .setContentTitle(item.getTitle())
                .setContentText(item.getActor())
                .setSubText(item.getTitle())
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                .setSmallIcon(R.drawable.exo_icon_play)
                .setColor(ContextCompat.getColor(context,R.color.blue))
                .setContentIntent(PendingIntent.getActivity(context,
                        NOTIFICATION_ID, songFragmentIntent, 0))
                //Add a previous button
                .addAction(actionPrevious()) // #0
                //Add a pause button
                .addAction(playPauseAction) // #1
                //Add a next button
                .addAction(actionNext())     // #2
                // Apply the media style template
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0,1,2))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

                .setLargeIcon(albumCoverImage).build();
    }
    private  NotificationCompat.Action actionPause()
    {
        Intent pauseIntent = new Intent(context, ChosenSongService.class);
        pauseIntent.setAction(PAUSE_ACTION);
        PendingIntent notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action(R.drawable.ic_pause,context.getString(R.string.pause),notificationPendingIntent);
    }
    private  NotificationCompat.Action actionPrevious()
    {
        Intent prevIntent = new Intent(context, ChosenSongService.class);
        prevIntent.setAction(PREVIOUS_ACTION);
        PendingIntent notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action(R.drawable.ic_previous,context.getString(R.string.previous),notificationPendingIntent);
    }
    private  NotificationCompat.Action actionPlay()
    {
        Intent playIntent = new Intent(context, ChosenSongService.class);
        playIntent.setAction(PLAY_ACTION);
        PendingIntent notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action(R.drawable.ic_play,context.getString(R.string.play),notificationPendingIntent);
    }
    private NotificationCompat.Action actionNext()
    {
        Intent nextIntent = new Intent(context, ChosenSongService.class);
        nextIntent.setAction(NEXT_ACTION);
        PendingIntent notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action(R.drawable.ic_play_next,context.getString(R.string.next),notificationPendingIntent);
    }
}
