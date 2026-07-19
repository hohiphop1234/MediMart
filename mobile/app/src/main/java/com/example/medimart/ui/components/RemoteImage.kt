package com.example.medimart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

/**
 * Keeps every remote image slot visually useful while an image is loading or unavailable.
 * Seed data and slow networks should never leave large blank cards in the UI.
 */
@Composable
fun RemoteImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackColor: Color,
    fallbackIcon: ImageVector = Icons.Outlined.Image,
    fallbackLabel: String? = null
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = {
            ImageFallback(
                color = fallbackColor,
                icon = fallbackIcon,
                showProgress = true,
                label = fallbackLabel
            )
        },
        error = {
            ImageFallback(color = fallbackColor, icon = fallbackIcon, label = fallbackLabel)
        }
    )
}

@Composable
private fun ImageFallback(
    color: Color,
    icon: ImageVector,
    showProgress: Boolean = false,
    label: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (label == null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f)
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = label,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Mua sắm tiện lợi mỗi ngày",
                    modifier = Modifier.padding(top = 2.dp),
                    color = Color.White.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(0.24f),
                color = Color.White,
                strokeWidth = 2.dp
            )
        }
    }
}
