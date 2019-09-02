package com.example.mediaplayer.chosenSong;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mediaplayer.playlist.PlayListModel;

import java.util.ArrayList;

 class ChosenSongViewModel extends ViewModel {
    private ArrayList<PlayListModel> playListModels;
    private MutableLiveData<Integer> _chosenSongIndex=new MutableLiveData<>();
    private boolean serviceCreatedBefore=false;

     ChosenSongViewModel( ArrayList<PlayListModel> playListModels,int chosenSongIndex) {
        this.playListModels = playListModels;
        _chosenSongIndex.setValue(chosenSongIndex);

    }

      MutableLiveData<Integer> getChosenSongIndex() {
         return _chosenSongIndex;
     }

     void setChosenSongIndex(int chosenSongIndex) {
         _chosenSongIndex.setValue(chosenSongIndex);
     }

     ArrayList<PlayListModel> getPlayListModels() {
        return playListModels;
    }


     boolean isServiceCreatedBefore() {
        return serviceCreatedBefore;
    }

     void setServiceCreatedBefore() {
        this.serviceCreatedBefore = true;
    }


 }
