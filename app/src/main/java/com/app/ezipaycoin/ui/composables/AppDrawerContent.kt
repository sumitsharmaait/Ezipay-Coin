package com.app.ezipaycoin.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground


data class SocialItem(val name: String, val icon: ImageVector)

@Composable
fun AppDrawerContent(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    onSignOut: () -> Unit,
    navItems: List<BottomNavItem>
) {

    // IMPORTANT: Replace these with your custom social media icons
    val socialItems = listOf(
        SocialItem("X", Icons.Filled.Close), // Placeholder
        SocialItem("Telegram", Icons.Filled.Send), // Placeholder
        SocialItem("Medium", Icons.Filled.Article), // Placeholder
        SocialItem("Discord", Icons.Filled.Message), // Placeholder
        SocialItem("Facebook", Icons.Filled.Facebook),
        SocialItem("LinkedIn", Icons.Filled.Group), // Placeholder
        SocialItem("Instagram", Icons.Filled.PhotoCamera), // Placeholder
        SocialItem("YouTube", Icons.Filled.PlayCircle) // Placeholder
    )

    val scrollState = rememberScrollState()

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val drawerWidth = (screenWidth * 0.65).dp

    ModalDrawerSheet(
        drawerContainerColor = greyButtonBackground.copy(alpha = 0.8f), // Container is transparent to allow background
        modifier = Modifier
            .width(drawerWidth)
            .background(color = greyButtonBackground.copy(alpha = 0.8f)),
        drawerTonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ezipay_coin_small), // Replace with your logo
                    contentDescription = "Ezipay Logo",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Ezipay",
                    style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                GradientText(
                    text = "Coin",
                    modifier = Modifier,
                    style = MaterialTheme.typography.headlineSmall,
                    align = TextAlign.Start
                )

            }
            Spacer(modifier = Modifier.height(24.dp))
            // Main Navigation
            navItems.forEach { item ->
                NavigationDrawerItem(
                    label = {
                        Text(
                            item.label,
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    selected = item.label == selectedItem,
                    modifier = if (item.label == selectedItem) {
                        Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(vertical = 4.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    },
                    onClick = { onItemSelected(item.label) },
                    icon = {
                        Icon(
                            item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Transparent,
                        unselectedContainerColor = Color.Transparent,
                        selectedIconColor = TextPrimaryColor,
                        unselectedIconColor = TextPrimaryColor,
                        selectedTextColor = TextPrimaryColor,
                        unselectedTextColor = TextPrimaryColor
                    ),
                )
            }

            // Sections
            SectionHeader("Resources")
            DrawerListItem(
                "Whitepaper & Tokenomics",
                Icons.AutoMirrored.Filled.LibraryBooks,
                selectedItem
            ) { label -> onItemSelected(label) }
            DrawerListItem("FAQ & Help Center", Icons.AutoMirrored.Filled.HelpOutline, selectedItem) { label -> onItemSelected(label) }

            SectionHeader("Settings")
            DrawerListItem("Language", Icons.Filled.Translate, selectedItem) { label -> onItemSelected(label) }
            DrawerListItem("Currency", Icons.Filled.AttachMoney, selectedItem) { label -> onItemSelected(label) }
            DrawerListItem("Notifications", Icons.Filled.Notifications, selectedItem) { label -> onItemSelected(label) }
            DrawerListItem("2FA & Biometrics", Icons.Filled.Fingerprint, selectedItem) { label -> onItemSelected(label) }
            DrawerListItem("KYC Status", Icons.Filled.CheckCircle, selectedItem) { label -> onItemSelected(label) }

            SectionHeader("Legal")
            DrawerListItem("Terms & Conditions", Icons.Filled.Description, selectedItem) { label -> onItemSelected(label) }
            DrawerListItem("Privacy Policy", Icons.Filled.Shield, selectedItem) { label -> onItemSelected(label) }

            SectionHeader("Social")
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(120.dp), // Fixed height for 2 rows
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(socialItems) { social ->
                    IconButton(onClick = { /*TODO: Open social link*/ }) {
                        Icon(
                            social.icon,
                            contentDescription = social.name,
                            tint = TextPrimaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            SectionHeader("Team")
            DrawerListItem("Core Team", Icons.Filled.Groups, selectedItem) { label -> onItemSelected(label) }

            Spacer(modifier = Modifier.weight(1f)) // Push Sign Out to bottom

            OutlinedButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(0.5.dp, TextPrimaryColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryColor)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    "Sign Out",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign Out",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor))
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = TextPrimaryColor)
    }
}

@Composable
fun DrawerListItem(
    label: String,
    icon: ImageVector,
    selectedItem: String,
    onClick: (label: String) -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall
            )
        },
        selected = label == selectedItem,
        modifier = if (label == selectedItem) {
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(vertical = 4.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        } else {
            Modifier
                .fillMaxWidth()
                .height(40.dp)
        },
        onClick = { onClick(label) },
        icon = {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
        },
        shape = MaterialTheme.shapes.medium,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color.Transparent,
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = TextPrimaryColor,
            unselectedIconColor = TextPrimaryColor,
            selectedTextColor = TextPrimaryColor,
            unselectedTextColor = TextPrimaryColor
        ),
    )
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .padding(vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            icon,
//            contentDescription = label,
//            tint = TextPrimaryColor,
//            modifier = Modifier.size(18.dp)
//        )
//        Spacer(modifier = Modifier.width(16.dp))
//        Text(label, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor))
//    }
}



