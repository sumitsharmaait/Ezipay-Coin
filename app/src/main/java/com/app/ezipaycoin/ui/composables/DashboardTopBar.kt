package com.app.ezipaycoin.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    isBackIcon: Boolean,
    currentRoute: String?,
    onAccountClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    onBackClicked: () -> Unit
) {

    CenterAlignedTopAppBar(
        title = {
            if (isBackIcon) {
                Row(
                    modifier = Modifier
                        .background(color = greyButtonBackground, shape = RoundedCornerShape(80)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_currency_top_bar),
                        contentDescription = "Network Logo",
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 10.dp, start = 20.dp)
                            .size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "BSC",
                        color = Color.White,
                        fontWeight = FontWeight(500),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 20.dp)
                    )
//                    Icon(
//                        Icons.Filled.ArrowDropDown,
//                        contentDescription = "Select Network",
//                        tint = Color.White,
//                        modifier = Modifier.padding(end = 20.dp)
//                    )
                }
            } else {
                currentRoute?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor)
                    )
                }

            }

        },
        navigationIcon = {
            if (isBackIcon) {
                IconButton(onClick = { onMenuClicked() }) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            } else {
                IconButton(onClick = { onBackClicked() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

        },
        actions = {
            if (isBackIcon) {
                IconButton(onClick = { /* TODO: Handle Notification Click */ }) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { onAccountClicked() }) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackgroundColor)
    )

}
