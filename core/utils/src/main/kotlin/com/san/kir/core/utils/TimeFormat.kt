package com.san.kir.core.utils

import android.content.Context

fun Long.formatTime(context: Context): String {
    return TimeFormat(this).toString(context)
}

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TimeFormat(seconds: Long) {
    var days: Long = 0
    var hours: Long = 0
    var minutes: Long = 0
    var seconds: Long = 0

    init {
        this.seconds = seconds % 60

        val minutes = seconds / 60

        this.minutes = minutes % 60

        val hours = minutes / 60

        this.hours = hours % 24

        val days = hours / 24

        this.days = days
    }

    fun isSeconds() = minutes == 0L && isMinutes()
    fun isMinutes() = hours == 0L && isHours()
    fun isHours() = days == 0L

    fun toString(context: Context): String {
        if (days == 0L && hours == 0L && minutes == 0L && seconds == 0L)
            return context.getString(R.string.time_format_seconds, 0)

        val builder = StringBuilder()

        if (days != 0L) {
            builder.append(context.getString(R.string.time_format_days, days))
            builder.append(" ")
        }
        if (hours != 0L) {
            builder.append(context.getString(R.string.time_format_hours, hours))
            builder.append(" ")
        }
        if (minutes != 0L) {
            builder.append(context.getString(R.string.time_format_minutes, minutes))
            builder.append(" ")
        }
        if (seconds != 0L)
            builder.append(context.getString(R.string.time_format_seconds, seconds))

        return builder.toString()
    }
}
