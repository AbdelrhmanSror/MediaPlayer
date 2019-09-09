package com.example.mediaplayer.ui.chosenSong

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplayer.AUDIO_FOREGROUND_NOTIFICATION
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.R

class ChosenSongActivity : AppCompatActivity() {

    /**
     * the whole purpose of this activity is to be replaced when user click on media notification
     * to open the the current media player
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chosen_song)

        val intent = intent
        val fragment = ChosenSongFragment()
        val bundle = Bundle()
        bundle.putParcelableArrayList(LIST_SONG, intent.getParcelableArrayListExtra(LIST_SONG))
        bundle.putInt(CHOSEN_SONG_INDEX, intent.getIntExtra(CHOSEN_SONG_INDEX, 0))
        bundle.putString(AUDIO_FOREGROUND_NOTIFICATION, intent.action)
        fragment.arguments = bundle
        val ft = supportFragmentManager.beginTransaction().replace(R.id.chosenSongContainer, fragment)
        ft.commit()

    }
}

