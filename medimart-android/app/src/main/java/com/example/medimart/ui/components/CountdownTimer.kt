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

@Composable
fun CountdownTimer(endTimeString: String) {
    var timeLeft by remember { mutableIntStateOf(7200) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

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
