package com.example.mediaplayer.chosenSong;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import com.example.mediaplayer.R;

import static com.example.mediaplayer.constants.AUDIO_FOREGROUND_NOTIFICATION;
import static com.example.mediaplayer.constants.CHOSEN_SONG_INDEX;
import static com.example.mediaplayer.constants.LIST_SONG;
public class ChosenSongActivity extends AppCompatActivity {

    /**
     * the whole purpose of this activity is to be replaced when user click on media notification
     * to open the the current media player
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_song);
        Intent intent = getIntent();
        ChosenSongFragment fragment = new ChosenSongFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(LIST_SONG, intent.getParcelableArrayListExtra(LIST_SONG));
        bundle.putInt(CHOSEN_SONG_INDEX, intent.getIntExtra(CHOSEN_SONG_INDEX, 0));
        bundle.putString(AUDIO_FOREGROUND_NOTIFICATION,intent.getAction());
        fragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.chosenSongContainer, fragment);
        ft.commit();

    }
}

