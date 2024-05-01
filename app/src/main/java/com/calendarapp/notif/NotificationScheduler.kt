package com.calendarapp.notif

import android.content.Context
import androidx.core.app.NotificationCompat
import com.calendarapp.data.Event

interface NotificationScheduler {
    suspend fun schedule(event: Event)
    suspend fun cancel(event: Event)
}