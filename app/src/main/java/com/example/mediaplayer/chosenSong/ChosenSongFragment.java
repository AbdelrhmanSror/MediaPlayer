package com.example.mediaplayer.chosenSong;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mediaplayer.R;
import com.example.mediaplayer.chosenSong.ChosenSongService.SongBinder;
import com.example.mediaplayer.databinding.FragmentChosenSongBinding;
import com.example.mediaplayer.playlist.PlayListModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player.EventListener;
import java.util.ArrayList;
import java.util.Objects;
import static com.example.mediaplayer.constants.ACTION_FOREGROUND;
import static com.example.mediaplayer.constants.ACTION_PLAYING_AUDIO_FOREGROUND;
import static com.example.mediaplayer.constants.AUDIO_FOREGROUND_NOTIFICATION;
import static com.example.mediaplayer.constants.CHOSEN_SONG_INDEX;
import static com.example.mediaplayer.constants.LIST_SONG;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChosenSongFragment extends Fragment {
    private ChosenSongViewModel viewModel;
    private FragmentChosenSongBinding binding;
    private ChosenSongService mService;
    private Intent foregroundIntent;
    private String purposeOfFragment;

    public ChosenSongFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChosenSongBinding.inflate(inflater);
        setUpViewModel();
        startForeground();
        setUpPlayerView();
        return binding.getRoot();
    }



    private void setUpViewModel() {
        ArrayList<PlayListModel> playListModels = null;
        int chosenSongIndex = 0;
        if (getArguments() != null) {
            playListModels = getArguments().getParcelableArrayList(LIST_SONG);
            chosenSongIndex = getArguments().getInt(CHOSEN_SONG_INDEX, 0);
            purposeOfFragment=getArguments().getString(AUDIO_FOREGROUND_NOTIFICATION,ACTION_PLAYING_AUDIO_FOREGROUND);
            //clearing the bundle so when fragment stops no huge parcelable error occurs
            //avoid causing TransactionTooLargeException
            getArguments().clear();
        }
        ChosenSongViewModelFactory factory = new ChosenSongViewModelFactory(playListModels,chosenSongIndex);
        viewModel = ViewModelProviders.of(this, factory).get(ChosenSongViewModel.class);

    }

    private void setUpPlayerView() {
        binding.playerView.setUseController(true);
        binding.playerView.showController();
        binding.playerView.setControllerAutoShow(true);
    }

    private void setSongName(int index) {
        ((TextView) binding.playerView.findViewById(R.id.songName)).setText(viewModel.getPlayListModels().get(index).getTitle());

    }

    private void setActorName(int index) {
        ((TextView) binding.playerView.findViewById(R.id.songActor)).setText(viewModel.getPlayListModels().get(index).getActor());

    }

    private void initializePlayer() {
        ExoPlayer player = mService.getPlayer();
        //Attaching the player to a view
        binding.playerView.setPlayer(player);
        mService.getPlayer().addListener(new EventListener() {
            @Override
            public void onSeekProcessed() {
                //if any changes happened to player transfer these changes to viewmodel Live data
                viewModel.setChosenSongIndex(mService.getPlayer().getCurrentWindowIndex());

            }

        });


    }


    @Override
    public void onStart() {
        super.onStart();
        //binding this fragment to service
        Objects.requireNonNull(getActivity()).bindService(foregroundIntent, connection, Context.BIND_AUTO_CREATE);


    }

    @Override
    public void onStop() {
        super.onStop();
        //unBinded fragment from service
        Objects.requireNonNull(getActivity()).unbindService(connection);

    }


    private void startForeground() {
        foregroundIntent = new Intent(getActivity(), ChosenSongService.class);
        foregroundIntent.setAction(ACTION_FOREGROUND);
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, viewModel.getChosenSongIndex().getValue());
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, viewModel.getPlayListModels());
        //Start service:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Objects.requireNonNull(getActivity()).startForegroundService(foregroundIntent);

        } else {
            Objects.requireNonNull(getActivity()).startService(foregroundIntent);

        }
    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SongBinder binder = (SongBinder) service;
            mService = binder.getService();
            initializePlayer();
            viewModel.getChosenSongIndex().observe(getViewLifecycleOwner(), chosenSongIndex -> {
                if(chosenSongIndex!=null) {
                    //to avoid setup player again when configuration changes happen or if player fragment has opened through service from notification
                    //if player is already setuped no need to setup it again
                    if (!viewModel.isServiceCreatedBefore() && !purposeOfFragment.equals(AUDIO_FOREGROUND_NOTIFICATION))
                        mService.setUpPlayer(viewModel.getPlayListModels(), chosenSongIndex);
                    setSongName(chosenSongIndex);
                    setActorName(chosenSongIndex);
                }
            });
            //if this fragment is opened through service then the source of truth is service and set the value of chosenSongIndex from service
            if(purposeOfFragment.equals(AUDIO_FOREGROUND_NOTIFICATION))
            {
                viewModel.setChosenSongIndex(mService.getPlayer().getCurrentWindowIndex());
            }
            //set this service as created before so we do not setupPlayer again
            viewModel.setServiceCreatedBefore();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

}
