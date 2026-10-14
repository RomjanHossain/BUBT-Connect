package com.corethink.bubtconnect.ui.components

import android.content.Context
import android.widget.Toast
import java.util.Locale

/** "৳ 14,100" */
fun formatTaka(amount: Int): String = "৳ " + String.format(Locale.US, "%,d", amount)

/** Minute of day → "8:30" (12-hour, no period). */
fun formatClock(minuteOfDay: Int): String {
    val hour24 = minuteOfDay / 60
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    return String.format(Locale.US, "%d:%02d", hour12, minuteOfDay % 60)
}

fun periodOf(minuteOfDay: Int): String = if (minuteOfDay < 12 * 60) "AM" else "PM"

fun formatGpa(value: Double): String = String.format(Locale.US, "%.2f", value)

/** 3.0 → "3", 1.5 → "1.5" */
fun formatCredits(credits: Double): String =
    if (credits % 1.0 == 0.0) credits.toInt().toString() else credits.toString()

/** 860 → "860", 1200 → "1.2k", 2000 → "2k" */
fun formatCompactCount(count: Int): String {
    if (count < 1000) return count.toString()
    val thousands = String.format(Locale.US, "%.1f", count / 1000.0).removeSuffix(".0")
    return "${thousands}k"
}

/** 0 → "today", 2 → "2d ago", 7 → "1w ago", 45 → "1mo ago" */
fun formatDaysAgo(days: Int): String = when {
    days <= 0 -> "today"
    days < 7 -> "${days}d ago"
    days < 30 -> "${days / 7}w ago"
    else -> "${days / 30}mo ago"
}

/** Placeholder feedback for actions whose backend isn't built yet (PDFs, payment links). */
fun showNotAvailableYet(context: Context, what: String) {
    Toast.makeText(context, "$what isn't available yet", Toast.LENGTH_SHORT).show()
}
