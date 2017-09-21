package com.kb.example.day4app

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager

/**
 * Created by Karol on 2017-09-21.
 */

class MediaPlayerUtils() {

    fun initMediaPlayer(context: Context, listener: MyMediaPlayerListener) = MediaPlayer().apply {
        setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        setAudioStreamType(AudioManager.STREAM_MUSIC)
        setOnPreparedListener(listener)
        setOnErrorListener(listener)
        setOnCompletionListener(listener)
    }

}