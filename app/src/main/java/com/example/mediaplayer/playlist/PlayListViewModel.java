package com.example.mediaplayer.playlist;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import java.util.ArrayList;
import java.util.List;

public class PlayListViewModel extends AndroidViewModel {
    private Application application;
    private List<PlayListModel>playListModels;

    public PlayListViewModel(@NonNull Application application) {
        super(application);
        this.application=application;
        playListModels=getAudiosUri();
    }


     List<PlayListModel> getPlayLists() {
        return playListModels;
    }
    /**
     * method to get list of audio uris on the device
     * @return list of audio uris
     */
    private List<PlayListModel> getAudiosUri() {
        ContentResolver contentResolver = application.getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            // query failed, handle error.
            // no media on the device
            return null;
        } else {
            int titleId=cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int actorId=cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int idAlbum=cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            List<PlayListModel> playLists = new ArrayList<>();
            do {
                long thisId = cursor.getLong(idColumn);
                String title=cursor.getString(titleId);
                String actor=cursor.getString(actorId);
                String albumPath=getAlbumArtPath(idAlbum);
                Uri contentUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId);
                playLists.add(new PlayListModel(title,actor,contentUri,albumPath));
            }
            while (cursor.moveToNext());
            cursor.close();
            return playLists;

        }
    }
    /**
     * method to get the album art image for specific audio
     * @param albumId of the audio to get its specific album art
     * @return uri of the album art image
     */
    private String getAlbumArtPath(int albumId)
    {
        Cursor cursor = application.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID+ "=?",
                new String[] {String.valueOf(albumId)},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            cursor.close();
            return path;
        }
        return null;
    }

}
