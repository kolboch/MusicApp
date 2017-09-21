package com.kb.example.day4app

import android.app.Service
import android.content.ContentUris
import android.content.Intent
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

class MusicService : Service(), MyMediaPlayerListener {

    private val binder = MusicServiceBinder()

    private val mediaUtils = MediaPlayerUtils()
    private var songs: MutableList<Song> = mutableListOf()
    private var currentSongPosition = 0

    private val player by lazy {
        mediaUtils.initMediaPlayer(applicationContext, this)
    }

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        Log.v("Log", "on create")
        retrieveDeviceSongList()
        startForeground(
                NOTIFICATION_ID,
                MyNotification.createMusicNotification(
                        this,
                        getString(R.string.app_name),
                        getString(R.string.default_notification_text)
                )
        )
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("Log", "on start command")
        return super.onStartCommand(intent, flags, startId)
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

    fun stopMusic() {
        player.stop()
    }

    fun playMusic() {
        prepareMediaPlayer()
        updateNotification()
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
                        composeArtistWithTitle(title, artist)
                )
        )

    }

    private fun composeArtistWithTitle(title: String, artist: String): String {
        return "\"$title\" $artist"
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) = Unit

    override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int) = false

    override fun onPrepared(mediaPlayer: MediaPlayer) = player.start()

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}