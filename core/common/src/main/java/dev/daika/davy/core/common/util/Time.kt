package dev.daika.davy.core.common.util

import kotlin.time.Duration.Companion.milliseconds

fun formatTime(ms: Long): String {
    if (ms < 0) return "00:00"

    return ms.milliseconds.toComponents { hours, minutes, seconds, _ ->
        if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
