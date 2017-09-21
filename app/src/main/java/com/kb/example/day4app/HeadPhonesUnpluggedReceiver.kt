package com.kb.example.day4app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by Karol on 2017-09-21.
 */
class HeadPhonesUnpluggedReceiver(private val player: MusicPlayer) : BroadcastReceiver() {

    private val HEADSET_UNPLUGED = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
            val state = intent?.getIntExtra("state", -1)
            when (state) {
                HEADSET_UNPLUGED -> player.stopMusic()
            }
        }
    }

}