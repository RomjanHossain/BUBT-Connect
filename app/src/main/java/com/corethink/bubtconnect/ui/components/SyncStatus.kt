package com.corethink.bubtconnect.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/** "Synced 5 min ago", refreshed every 30 seconds while on screen. */
@Composable
fun rememberSyncedLabel(lastSyncedAt: Long?): String {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(lastSyncedAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }
    return syncedLabel(lastSyncedAt, now)
}

fun syncedLabel(lastSyncedAt: Long?, now: Long): String {
    if (lastSyncedAt == null) return "Not synced yet"
    val minutes = ((now - lastSyncedAt) / 60_000).coerceAtLeast(0)
    return when {
        minutes < 1 -> "Synced just now"
        minutes < 60 -> "Synced $minutes min ago"
        minutes < 24 * 60 -> "Synced ${minutes / 60} h ago"
        else -> "Synced ${minutes / (24 * 60)} d ago"
    }
}
