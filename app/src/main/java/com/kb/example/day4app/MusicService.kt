package com.kb.example.day4app

import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import java.util.*

/**
 * Created by Karol on 2017-09-21.
 */

const val NOTIFICATION_ID = 77
const val PENDING_INTENT_PLAY = 88
const val PENDING_INTENT_STOP = 99
const val ACTION_PLAY = "com.kb.example.day4app.play_music"
const val ACTION_STOP = "com.kb.example.day4app.stop_music"
const val LOG_TAG_SERVICE = "MusicService"

class MusicService : Service(), MyMediaPlayerListener, MusicPlayer {

    private val binder = MusicServiceBinder()
    private val mediaUtils = MediaPlayerUtils()

    private val playIntent by lazy {
        PendingIntent.getService(
                applicationContext,
                PENDING_INTENT_PLAY,
                Intent(ACTION_PLAY),
                0)
    }

    private val stopIntent by lazy {
        PendingIntent.getService(
                applicationContext,
                PENDING_INTENT_STOP,
                Intent(ACTION_STOP),
                0)
    }

    private val player by lazy { mediaUtils.initMediaPlayer(applicationContext, this) }
    private val headPhonesReceiver = HeadPhonesUnpluggedReceiver(this)

    private var songs: MutableList<Song> = mutableListOf()
    private var currentSongPosition = 0

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun stopMusic() {
        player.stop()
    }

    fun playMusic() {
        prepareMediaPlayer()
        updateNotification()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        Log.v(LOG_TAG_SERVICE, "onCreate")
        startForeground(
                NOTIFICATION_ID,
                MyNotification.createMusicNotification(
                        this,
                        getString(R.string.app_name),
                        getString(R.string.default_notification_text),
                        playIntent,
                        stopIntent
                )
        )
        retrieveDeviceSongList()
        registerHeadphonesUnpluggedReceiver()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(LOG_TAG_SERVICE, "onStartCommand")
        handleIntentAction(intent?.action)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun registerHeadphonesUnpluggedReceiver() {
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG)
        registerReceiver(headPhonesReceiver, intentFilter)
    }

    private fun retrieveDeviceSongList() {
        songs = mutableListOf()
        val musicResolver = contentResolver
        val musicCursor = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val titleColumn = musicCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)
            val idColumn = musicCursor.getColumnIndex(BaseColumns._ID)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)
            val albumCoverColumn = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val albumNameColumn = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)
            val durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)
            do {
                val thisId = musicCursor.getLong(idColumn)
                val songTitle = musicCursor.getString(titleColumn)
                val songArtist = musicCursor.getString(artistColumn)
                val songAlbum = musicCursor.getString(albumNameColumn)
                val albumCoverId = musicCursor.getLong(albumCoverColumn)
                val albumCoverUriPath = Uri.parse("content://media/external/audio/albumart")
                val albumArtUri = ContentUris.withAppendedId(albumCoverUriPath, albumCoverId)
                val songDuration = musicCursor.getLong(durationColumn)
                songs.add(Song(thisId, songTitle, songArtist, songAlbum, albumArtUri, songDuration.toInt()))
            } while (musicCursor.moveToNext())
        }
        musicCursor?.close()
        Collections.sort<Song>(songs) { lhs, rhs -> lhs.title.compareTo(rhs.title) }
    }

    private fun prepareMediaPlayer() {
        Log.v("LOG", "Prepare media player call")
        player.reset()
        val currentlyPlayedSong = songs[currentSongPosition]
        val currentSongId = currentlyPlayedSong.id
        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongId)

        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        player.prepareAsync()
    }

    private fun updateNotification() {
        val title = songs[currentSongPosition].title
        val artist = songs[currentSongPosition].artist
        startForeground(
                NOTIFICATION_ID,
                MyNotification.createMusicNotification(
                        this,
                        getString(R.string.app_name),
                        composeArtistWithTitle(title, artist),
                        playIntent,
                        stopIntent
                )
        )
    }

    private fun handleIntentAction(action: String?) {
        when (action) {
            ACTION_PLAY -> playMusic()
            ACTION_STOP -> stopMusic()
        }
    }

    private fun composeArtistWithTitle(title: String, artist: String): String {
        return "\"$title\" $artist"
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) = Unit

    override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int) = false

    override fun onPrepared(mediaPlayer: MediaPlayer) = player.start()

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        player.release()
        stopForeground(true)
        unregisterReceiver(headPhonesReceiver)
        super.onDestroy()
    }
}