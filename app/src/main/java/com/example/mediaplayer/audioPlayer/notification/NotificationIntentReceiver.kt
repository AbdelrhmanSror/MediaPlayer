package com.example.mediaplayer.audioPlayer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import com.example.mediaplayer.audioPlayer.EventDispatcher

class NotificationIntentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || Intent.ACTION_MEDIA_BUTTON != intent.action
                || !intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            return
        }
        val event: KeyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)!!
        Log.v("registeringAudioSession", " event :$event ")
        EventDispatcher(context).dispatchMediaKeyEvent(event.keyCode)
    }
}