package com.kb.example.day4app


import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Karol on 2017-09-21.
 */
@Parcelize
class Song(
        val id: Long,
        val title: String,
        val artist: String,
        val albumName: String,
        val albumCoverUri: Uri,
        val durationMillis: Int
) : Parcelable