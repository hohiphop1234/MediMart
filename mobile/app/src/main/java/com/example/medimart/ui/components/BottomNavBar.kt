package com.example.medimart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.medimart.R
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartTextSecondary

data class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    cartBadgeCount: Int = 0
) {
    val items = listOf(
        NavItem("home", stringResource(R.string.nav_home), Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("category", stringResource(R.string.nav_category), Icons.AutoMirrored.Filled.List,
            Icons.AutoMirrored.Outlined.List
        ),
        NavItem("cart", stringResource(R.string.nav_cart), Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
        NavItem("profile", stringResource(R.string.nav_profile), Icons.Filled.Person, Icons.Outlined.Person)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                spotColor = Color.Black.copy(alpha = 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (index == 2) {
                    Spacer(modifier = Modifier.width(48.dp)) // Space for FAB
                }
                val isSelected = currentRoute == item.route
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
                val color = if (isSelected) MediMartOrange else MediMartTextSecondary
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.title,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                            if (item.route == "cart" && cartBadgeCount > 0) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-4).dp),
                                    containerColor = Color.Red
                                ) {
                                    Text("$cartBadgeCount", color = Color.White)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = color
                        )
                    }
                }
            }
        }
    }
}
