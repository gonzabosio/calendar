package com.calendarapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class CalendarApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val notificationChannel= NotificationChannel(
            CHANNEL_ID,
            "Calendar",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }
    companion object {
        const val CHANNEL_ID = "calendar_notification_id"
    }
}