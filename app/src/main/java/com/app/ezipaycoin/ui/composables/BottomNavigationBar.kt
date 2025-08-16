package com.app.ezipaycoin.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground

@Composable
fun BottomNavigationBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    bottomItems: List<BottomNavItem>
) {
    Box(contentAlignment = Alignment.BottomCenter) {
        NavigationBar(
            containerColor = greyButtonBackground,
            modifier = Modifier.height(70.dp)
        ) {
            bottomItems.forEachIndexed { index, item ->
                if (index == 2) {
                    // Add spacer for center FAB
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        enabled = false
                    )
                } else {
                    NavigationBarItem(
                        label = {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        selected = selectedItem == item.label,
                        onClick = {
                            onItemSelected(item.label)
                        },
                        icon = {
                            Icon(
                                if (selectedItem == item.label) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAccentColor,
                            selectedTextColor = GoldAccentColor,
                            unselectedIconColor = TextPrimaryColor,
                            unselectedTextColor = TextPrimaryColor,
                            indicatorColor = Color.Transparent // No indicator circle
                        ),
                        //modifier = Modifier.weight(1f)
                    )
                }
            }
        }


        FloatingActionButton(
            onClick = { onItemSelected("Pay") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp), // Adjust height as needed
            shape = CircleShape
        ) {
            Image(
                painterResource(id = R.drawable.btn_pay),
                contentDescription = "Pay",
                modifier = Modifier.size(72.dp)
            )
        }


    }

}