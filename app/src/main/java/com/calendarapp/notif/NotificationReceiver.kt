package com.calendarapp.notif

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.calendarapp.CalendarApplication.Companion.CHANNEL_ID

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val notificationID = intent?.getIntExtra("notification_id", 0) ?: 0
        val event = intent?.getStringExtra("notification_event") ?: "Event"
        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(event)
        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(intent?.getStringExtra("Calendar Reminder"))
            .setContentText("An event is coming up!")
            .setStyle(bigTextStyle)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }
}