package com.app.ezipaycoin.presentation.deposit

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
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
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.iconColorOnGreyBackground
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.copyToClipboard
import com.app.ezipaycoin.utils.generateQrCodeBitmap

@Composable
fun Deposit(
    navController: NavController,
    viewModel: DepositViewModel,
) {
    val state by viewModel.uiState.collectAsState()


    when (state.walletInfoResponse) {
        is ResponseState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OnboardingGold1)
            }
        }

        is ResponseState.Success -> {
            val data = (state.walletInfoResponse as ResponseState.Success<String>).data
            DepositeScreenSuccess(viewModel = viewModel, data)
        }

        is ResponseState.Error -> {
            Dialogue(
                isError = true,
                msg = (state.walletInfoResponse as ResponseState.Error).message
            ) {
                viewModel.onEvent(DepositeEvent.DismissDialog(0))
            }
        }

        ResponseState.Idle -> {
            Log.e("Idle", "Idle")
        }
    }

    when (state.statusDepositeResponse) {
        is ResponseState.Loading -> {}
        is ResponseState.Success -> {
            Dialogue(
                isError = false,
                msg = "Transaction Success"
            ) {
                viewModel.onEvent(DepositeEvent.DismissDialog(1))
            }
        }

        is ResponseState.Error -> {
            Dialogue(
                isError = true,
                msg = "Transaction Failed"
            ) {
                viewModel.onEvent(DepositeEvent.DismissDialog(1))
            }
        }

        ResponseState.Idle -> {}
    }


}

@Composable
private fun DepositeScreenSuccess(viewModel: DepositViewModel, walletAddress: String) {
    val state by viewModel.uiState.collectAsState()
    val context: Context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = state.networkInfo,
            style = MaterialTheme.typography.headlineSmall.copy(
                brush = Brush.linearGradient(
                    colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                ), fontWeight = FontWeight(800)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (walletAddress.isNotBlank()) {
            val qrCodeBitmap = walletAddress.generateQrCodeBitmap()
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
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Scan this QR code to deposite payment",
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
                text = walletAddress,
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
                modifier = Modifier.weight(1f),
                maxLines = 2,
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = { context.copyToClipboard(walletAddress, "WalletAddress") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, "Copy Address", tint = iconColorOnGreyBackground)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

//        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//            AppGreyButton(
//                labelColor = Color.White,
//                label = "Save",
//                onClick = {},
//                modifier = Modifier.weight(1f)
//            )
//            GoldGradientButton(
//                label = "Share",
//                onClick = {},
//                modifier = Modifier
//                    .weight(1f)
//            )
//        }

    }
}