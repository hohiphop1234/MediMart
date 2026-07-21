package com.example.medimart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FaceRetouchingNatural
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartOrangeSoft

private data class CategoryIconStyle(
    val imageVector: ImageVector,
    val tint: Color,
    val background: Color
)

@Composable
fun CategoryIcon(
    categoryName: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = categoryName
) {
    val style = categoryIconStyle(categoryName)

    Box(
        modifier = modifier.background(style.background, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = style.imageVector,
            contentDescription = contentDescription,
            tint = style.tint,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )
    }
}

private fun categoryIconStyle(categoryName: String): CategoryIconStyle {
    val normalizedName = categoryName.lowercase()

    return when {
        "chăm sóc cá nhân" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.SelfImprovement,
            tint = Color(0xFF7C3AED),
            background = Color(0xFFF3E8FF)
        )
        "dược mỹ phẩm" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.FaceRetouchingNatural,
            tint = Color(0xFFDB2777),
            background = Color(0xFFFCE7F3)
        )
        "miễn dịch" in normalizedName || "đề kháng" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.Shield,
            tint = Color(0xFF059669),
            background = Color(0xFFD1FAE5)
        )
        "thần kinh" in normalizedName || "não" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.Psychology,
            tint = Color(0xFF4F46E5),
            background = Color(0xFFE0E7FF)
        )
        "thiết bị y tế" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.MedicalServices,
            tint = Color(0xFF0284C7),
            background = Color(0xFFE0F2FE)
        )
        "thực phẩm chức năng" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.HealthAndSafety,
            tint = Color(0xFF0F766E),
            background = Color(0xFFCCFBF1)
        )
        normalizedName == "thuốc" || "dược phẩm" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.Medication,
            tint = Color(0xFFEA580C),
            background = Color(0xFFFFEDD5)
        )
        "tiêu hóa" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.Restaurant,
            tint = Color(0xFFD97706),
            background = Color(0xFFFEF3C7)
        )
        "tim mạch" in normalizedName || "huyết áp" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.MonitorHeart,
            tint = Color(0xFFDC2626),
            background = Color(0xFFFEE2E2)
        )
        "vitamin" in normalizedName || "khoáng chất" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.Science,
            tint = Color(0xFF65A30D),
            background = Color(0xFFECFCCB)
        )
        "sức khỏe" in normalizedName -> CategoryIconStyle(
            Icons.Rounded.HealthAndSafety,
            tint = Color(0xFF0891B2),
            background = Color(0xFFCFFAFE)
        )
        else -> CategoryIconStyle(
            Icons.Rounded.LocalPharmacy,
            tint = MediMartOrange,
            background = MediMartOrangeSoft
        )
    }
}
