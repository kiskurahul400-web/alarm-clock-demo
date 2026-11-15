package com.example.alarmclockdemo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)

    fun scheduleAlarm(hour: Int, minute: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val triggerAtMillis = target.timeInMillis
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.alarmclockdemo.ALARM_TRIGGERED"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        prefs.edit().putLong("alarm_time", triggerAtMillis).apply()
        return true
    }

    fun cancelAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.alarmclockdemo.ALARM_TRIGGERED"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()
        prefs.edit().remove("alarm_time").apply()
    }

    fun getNextAlarmDisplay(): String {
        val alarmEpoch = prefs.getLong("alarm_time", -1L)
        if (alarmEpoch <= 0L) return "No alarm set"

        val date = java.util.Date(alarmEpoch)
        val formatter = java.text.SimpleDateFormat("EEE, dd MMM yyyy - h:mm a", java.util.Locale.getDefault())
        return "Next alarm: ${formatter.format(date)}"
    }
}
