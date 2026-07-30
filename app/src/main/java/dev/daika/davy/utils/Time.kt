package dev.daika.davy.utils

fun formatTime(ms: Long, needHours: Boolean = false): String {
    if (ms < 0) return "00:00"
    val totalSeconds = ms / 1000
    val totalMinutes = totalSeconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val seconds = totalSeconds % 60
    if (needHours) {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    return String.format("%02d:%02d", minutes, seconds)
}