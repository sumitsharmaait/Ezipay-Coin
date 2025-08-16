package com.app.ezipaycoin.presentation.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.tagsBackgroundColor
import com.app.ezipaycoin.ui.theme.tagsTextColor
import com.app.ezipaycoin.utils.copyToClipboard

@Composable
fun MyProfile(viewModel: MyProfileViewModel) {

    val state by viewModel.uiState.collectAsState()
    val context: Context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box {
            Image(
                // IMPORTANT: Replace with your actual profile image resource
                painter = painterResource(id = R.drawable.ic_ezipay_coin_small),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            val goldGradient = Brush.linearGradient(
                colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
            )
            FloatingActionButton(
                onClick = { /* TODO: Handle Edit Profile Picture */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .align(Alignment.BottomEnd)
                    .offset(x = (0).dp, y = (0).dp)
                    .background(goldGradient),
                shape = CircleShape,
                containerColor = Color.Transparent,
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit Profile Picture",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(state.name, style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Regular",
                style = MaterialTheme.typography.titleSmall.copy(color = tagsTextColor),
                modifier = Modifier
                    .background(tagsBackgroundColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            Text(
                text = "Verified",
                style = MaterialTheme.typography.titleSmall.copy(color = tagsTextColor),
                modifier = Modifier
                    .background(tagsBackgroundColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = greyButtonBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            UserInfoRow(
                "ID",
                state.walletAddress,
                Icons.Filled.ContentCopy,
                "Copy ID"
            ) {
                context.copyToClipboard(state.walletAddress, "WalletAddress")
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline
            )
            UserInfoRow(
                "Email",
                state.email,
                Icons.Filled.VisibilityOff,
                "Toggle Email Visibility"
            ) { /* TODO: Toggle Email */ }
        }


        Spacer(modifier = Modifier.height(24.dp))

        val settings = listOf(
            SettingItem(
                Icons.Filled.WorkspacePremium,
                "VIP Privilege",
                "Regular"
            ) { /* TODO */ },
            SettingItem(
                Icons.Filled.PersonOutline,
                "Verifications",
                "Verified",
            ) { /* TODO */ },
            SettingItem(Icons.Filled.Shield, "Security") { /* TODO */ },
            SettingItem(
                Icons.Filled.Close,
                "Twitter",
                "Linked"
            ) { /* TODO */ } // Assuming X is Twitter
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(greyButtonBackground, MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium)
        ) {
            settings.forEachIndexed { index, item ->
                SettingRow(item)
                if (index < settings.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppGreyButton(
            labelColor = Color.White,
            label = "Log Out",
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 8.dp)
        )

    }

}

@Composable
fun UserInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    iconContentDescription: String,
    onIconClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )
        Row(
            modifier = Modifier.weight(4f, true),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                value,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onIconClick,
                modifier = Modifier.size(20.dp)
            ) { // Constrain IconButton size
                Icon(icon, contentDescription = iconContentDescription, tint = Color.White)
            }
        }
    }
}

@Composable
fun SettingRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            item.label,
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            modifier = Modifier.weight(1f)
        )
        item.status?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium.copy(color = tagsTextColor),
                modifier = Modifier
                    .background(tagsBackgroundColor, RoundedCornerShape(4.dp))
                    .padding(
                        horizontal = 10.dp, vertical = 2.dp
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color.White
        )
    }
}

data class SettingItem(
    val icon: ImageVector,
    val label: String,
    val status: String? = null,
    val onClick: () -> Unit
)

@Preview
@Composable
fun ProfileScreenSmallerPreview() {
    EzipayCoinTheme {
        MyProfile(viewModel<MyProfileViewModel>())
    }
}