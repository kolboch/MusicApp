package com.kb.example.day4app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var service: MusicService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onListenersSetup()
        bindToMusicService()
    }

    private fun bindToMusicService() {
        val intent = Intent(this, MusicService::class.java)
        val connection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                Log.v("LOG", "service connected")
                service = (binder as MusicService.MusicServiceBinder).getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.v("LOG", "on service disconnected")
            }
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun onListenersSetup() {
        buttonPlay.setOnClickListener {
            service?.playMusic()
        }

        buttonStop.setOnClickListener {
            service?.stopMusic()
        }
    }
}


