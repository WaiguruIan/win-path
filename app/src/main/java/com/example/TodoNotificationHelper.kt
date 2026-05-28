package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.TodoItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ParsedTodoTime(
    val parsedTime24h: String, // e.g. "09:00" or "21:30"
    val timeStartIndex: Int,
    val timeEndIndex: Int
)

object TodoNotificationHelper {

    fun parseTimePlaceholder(input: String): ParsedTodoTime? {
        // Tolerantly matches patterns like "at 9:30 pm", "at 9.15 AM", "at 3 AM", "at 15:45", "at 12pm", "at 9am", "at 3 pm"
        val pattern = Regex("(?i)\\bat\\s+(\\d{1,2})(?:[:.](\\d{1,2}))?\\s*(am|pm)?\\b")
        val match = pattern.find(input) ?: return null
        
        val hourStr = match.groupValues[1]
        val minStr = if (match.groupValues[2].isEmpty()) "00" else match.groupValues[2]
        val amPm = match.groupValues[3].uppercase(Locale.getDefault())

        var hour = hourStr.toIntOrNull() ?: return null
        val minute = minStr.toIntOrNull() ?: return null

        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null

        if (amPm.isNotEmpty()) {
            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0
        }

        val time24h = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        return ParsedTodoTime(
            parsedTime24h = time24h,
            timeStartIndex = match.range.first,
            timeEndIndex = match.range.last + 1
        )
    }

    fun scheduleTodoAlarm(context: Context, item: TodoItem) {
        if (!item.isAlertEnabled || item.alertTime.isNullOrEmpty() || item.isCompleted) {
            cancelTodoAlarm(context, item)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateStr = "${item.dateString} ${item.alertTime}"
            val date = sdf.parse(dateStr) ?: return

            val calendar = Calendar.getInstance()
            calendar.time = date

            // Subtract offset minutes
            calendar.add(Calendar.MINUTE, -item.alertOffsetMinutes)

            // If time is in the past, don't schedule it
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                Log.d("TodoNotificationHelper", "Calculated alert time is in the past, skipping. alertTime=${calendar.time}")
                return
            }

            val intent = Intent(context, TodoAlarmReceiver::class.java).apply {
                putExtra("todo_id", item.id)
                putExtra("todo_title", item.title)
                putExtra("todo_time", formatAlertTime12h(item.alertTime))
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                item.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val showIntent = Intent(context, Class.forName("com.example.MainActivity"))
                    val showPendingIntent = PendingIntent.getActivity(
                        context,
                        item.id.toInt() + 100000,
                        showIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val clockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, showPendingIntent)
                    alarmManager.setAlarmClock(clockInfo, pendingIntent)
                } catch (clockEx: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        } catch (securityEx: SecurityException) {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } catch (securityEx: SecurityException) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
            Log.d("TodoNotificationHelper", "Scheduled alarm for todo item ${item.id} at ${calendar.time}")
        } catch (e: Exception) {
            Log.e("TodoNotificationHelper", "Error scheduling alarm", e)
        }
    }

    fun cancelTodoAlarm(context: Context, item: TodoItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TodoAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun formatAlertTime12h(time24h: String?): String {
        if (time24h.isNullOrEmpty()) return ""
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdf12 = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = sdf24.parse(time24h)
            if (date != null) sdf12.format(date) else time24h
        } catch (e: Exception) {
            time24h
        }
    }
}
