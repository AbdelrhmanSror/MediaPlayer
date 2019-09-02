package com.example.mediaplayer.playlist;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * class represent an item in playlist
 */
public class PlayListModel implements Parcelable {
    private  String title;
    private String actor;
    private Uri audioUri;
    private String albumCoverUri;
    public PlayListModel(String Title, String actor, Uri audioUri,String albumCoverUri)
    {
        title=Title;
        this.actor=actor;
        this.audioUri=audioUri;
        this.albumCoverUri=albumCoverUri;
    }

    protected PlayListModel(Parcel in) {
        title = in.readString();
        actor = in.readString();
        audioUri = in.readParcelable(Uri.class.getClassLoader());
        albumCoverUri = in.readString();
    }

    public static final Creator<PlayListModel> CREATOR = new Creator<PlayListModel>() {
        @Override
        public PlayListModel createFromParcel(Parcel in) {
            return new PlayListModel(in);
        }

        @Override
        public PlayListModel[] newArray(int size) {
            return new PlayListModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getActor() {
        return actor;
    }

    public  Uri getAudioUri() {
        return audioUri;
    }

    public String getAlbumCoverUri() {
        return albumCoverUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(actor);
        parcel.writeParcelable(audioUri, i);
        parcel.writeString(albumCoverUri);
    }
}
