package com.example.mediaplayer.ui.chosenSong

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.FRAGMENT_PURPOSE
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

        val fragment = ChosenSongFragment()
        val bundle = Bundle()
        bundle.putParcelableArrayList(LIST_SONG, intent.getParcelableArrayListExtra(LIST_SONG))
        bundle.putInt(CHOSEN_SONG_INDEX, intent.getIntExtra(CHOSEN_SONG_INDEX, 0))

        //send the action coming as result of clicking on notification to fragment
        bundle.putString(FRAGMENT_PURPOSE, intent.action)
        fragment.arguments = bundle
        val ft = supportFragmentManager.beginTransaction().replace(R.id.chosenSongContainer, fragment)
        ft.commit()

    }
}

