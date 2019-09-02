package com.example.mediaplayer.chosenSong;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.mediaplayer.playlist.PlayListModel;
import java.util.ArrayList;

public  class ChosenSongViewModelFactory implements ViewModelProvider.Factory {
    private ArrayList<PlayListModel> playListModels;
    private int chosenSongIndex;

     ChosenSongViewModelFactory( ArrayList<PlayListModel> playListModels, int chosenSongIndex) {
        this.playListModels=playListModels;
        this.chosenSongIndex=chosenSongIndex;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(ChosenSongViewModel.class)) {
            return (T) new ChosenSongViewModel(playListModels,chosenSongIndex);

        }
        throw new IllegalArgumentException("unknown class");
    }
}
