package com.example.medimart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun CountdownTimer(endTimeString: String) {
    val endTimeMillis = remember(endTimeString) { parseEndTime(endTimeString) }
    var nowMillis by remember(endTimeMillis) { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(endTimeMillis) {
        while (endTimeMillis > System.currentTimeMillis()) {
            delay(1000)
            nowMillis = System.currentTimeMillis()
        }
    }

    val timeLeft = ((endTimeMillis - nowMillis) / 1000).coerceAtLeast(0).toInt()

    val hours = timeLeft / 3600
    val minutes = (timeLeft % 3600) / 60
    val seconds = timeLeft % 60

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeBox(String.format("%02d", hours))
        Text(":", color = Color.White, style = MaterialTheme.typography.titleMedium)
        TimeBox(String.format("%02d", minutes))
        Text(":", color = Color.White, style = MaterialTheme.typography.titleMedium)
        TimeBox(String.format("%02d", seconds))
    }
}

private fun parseEndTime(value: String): Long {
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(value)?.time ?: 0L
    }.getOrDefault(0L)
}

@Composable
fun TimeBox(time: String) {
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = time, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}
