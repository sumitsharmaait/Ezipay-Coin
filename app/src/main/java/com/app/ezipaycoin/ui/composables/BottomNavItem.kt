package com.app.ezipaycoin.ui.composables

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,   // Can be ImageVector or Painter
    val isCentral: Boolean = false
)
