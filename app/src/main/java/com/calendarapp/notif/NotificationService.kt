package com.calendarapp.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.calendarapp.data.Event

class NotificationService(
    private val context: Context
): NotificationScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    override suspend fun schedule(event: Event) {
        if(event.reminder > System.currentTimeMillis()) {
            val uniqueId = System.currentTimeMillis().toInt()
            val alarmIntent = Intent(context, NotificationReceiver::class.java).let { intent ->
                intent.putExtra("notification_id",uniqueId)
                intent.putExtra("notification_event", "${event.title} on ${event.date} at ${event.hour}")
                PendingIntent.getBroadcast(context, uniqueId, intent, PendingIntent.FLAG_MUTABLE)
            }
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                event.reminder-3600000,
                alarmIntent
            )
        }
    }

    override suspend fun cancel(event: Event) {
        val alarmIntent = Intent(context, NotificationReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, event._id.hashCode(), intent, PendingIntent.FLAG_MUTABLE)
        }
        alarmManager.cancel(alarmIntent)
    }
}