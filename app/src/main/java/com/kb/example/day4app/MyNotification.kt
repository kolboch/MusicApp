package com.kb.example.day4app

import android.app.Notification
import android.content.Context
import android.support.v4.app.NotificationCompat

/**
 * Created by Karol on 2017-09-21.
 */
class MyNotification {

    companion object {
        fun createMusicNotification(appContext: Context, contentTitle: String, contentText: String): Notification {
            return NotificationCompat.Builder(appContext)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .build()
        }
    }
}