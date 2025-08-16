package com.app.ezipaycoin.presentation.receive

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.iconColorOnGreyBackground
import com.app.ezipaycoin.utils.copyToClipboard
import com.app.ezipaycoin.utils.generateQrCodeBitmap


@Composable
fun ReceiveScreen(
    navController: NavController,
    viewModel: ReceiveViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val context: Context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = "Ezipay Coin Wallet",
            style = MaterialTheme.typography.headlineMedium.copy(
                brush = Brush.linearGradient(
                    colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                ), fontWeight = FontWeight(800)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (state.walletAddress.isNotBlank()) {
            val qrCodeBitmap = state.walletAddress.generateQrCodeBitmap()
            Box(
                modifier = Modifier
                    .size(240.dp) // Adjust size as needed
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(1.dp), // Padding inside the white box to create a border
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrCodeBitmap.asImageBitmap(),
                    contentDescription = "Wallet Address QR Code",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

//            Box(
//                modifier = Modifier
//                    .size(40.dp)
//                    .background(Color(0xFF26A17B), CircleShape)
//                    .border(2.dp, Color.White, CircleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                // Placeholder 'T' icon for Tether (USDT)
//                Icon(
//                    Icons.Filled.CurrencyYen,
//                    "Tether",
//                    tint = Color.White,
//                    modifier = Modifier.size(24.dp)
//                ) // Using Yen as a 'T' like placeholder
//            }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Scan this QR code to receive\nto payments",
            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(greyButtonBackground, MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.walletAddress,
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
                modifier = Modifier.weight(1f),
                maxLines = 2,
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = { context.copyToClipboard(state.walletAddress, "WalletAddress") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, "Copy Address", tint = iconColorOnGreyBackground)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppGreyButton(
                labelColor = Color.White,
                label = "Save",
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            GoldGradientButton(
                label = "Share",
                onClick = {},
                modifier = Modifier
                    .weight(1f)
            )
        }

    }

}













