package com.calendarapp.notif

import com.calendarapp.data.Event

interface NotificationScheduler {
    suspend fun schedule(event: Event)
    suspend fun cancel(event: Event)
}