package com.kb.example.day4app

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat

/**
 * Created by Karol on 2017-09-21.
 */
class MyNotification {

    companion object {
        fun createMusicNotification(appContext: Context,
                                    contentTitle: String,
                                    contentText: String,
                                    actionPlay: PendingIntent,
                                    actionStop: PendingIntent): Notification {
            return NotificationCompat.Builder(appContext)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .addAction(R.drawable.ic_play_arrow_black_24dp, appContext.getString(R.string.play), actionPlay)
                    .addAction(R.drawable.ic_stop_black_24dp, appContext.getString(R.string.stop), actionStop)
                    .build()
        }
    }
}