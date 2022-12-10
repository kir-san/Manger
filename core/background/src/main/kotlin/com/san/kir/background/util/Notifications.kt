package com.san.kir.background.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.san.kir.background.R
import java.util.UUID

internal fun Context.cancelAction(id: UUID): NotificationCompat.Action {
    return NotificationCompat
        .Action
        .Builder(
            R.drawable.ic_notification_cancel,
            getString(R.string.catalog_fos_service_action_cancel_all),
            WorkManager
                .getInstance(this)
                .createCancelPendingIntent(id)
        )
        .build()
}

internal fun Context.tryCreateNotificationChannel(channelId: String, tag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = NotificationManagerCompat.from(this)
        val chan = NotificationChannel(channelId, tag, NotificationManager.IMPORTANCE_LOW)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(chan)
    }
}
