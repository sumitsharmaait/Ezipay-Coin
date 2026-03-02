package com.app.ezipaycoin.presentation.withdraw

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.composables.LaserLineEffect
import com.app.ezipaycoin.ui.composables.QrCodeScanner
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.RedTextColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.pasteFromClipboard
import kotlin.math.roundToInt

@Composable
fun Withdraw(
    navController: NavController,
    viewModel: WithdrawViewModel,
) {
    val state by viewModel.uiState.collectAsState()


    when (state.networkInfoResponse) {
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
            //val data = (state.networkInfoResponse as ResponseState.Success<List<NetworkInfoByTokenResponse.NetworkInfo>>).data
            WithdrawScreenWithNetworkInfo(vm = viewModel, navController)
        }

        is ResponseState.Error -> {
            Dialogue(
                isError = true, msg = (state.networkInfoResponse as ResponseState.Error).message
            ) {
                viewModel.onEvent(WithdrawEvent.DismissDialog(0))
            }
        }

        ResponseState.Idle -> {
            Log.e("Idle", "Idle")
        }
    }


}


@Composable
private fun WithdrawScreenWithNetworkInfo(
    vm: WithdrawViewModel,
    navController: NavController
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        // Use LazyColumn for scrollable content with fixed bottom
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.networkInfo, style = MaterialTheme.typography.headlineSmall.copy(
                brush = Brush.linearGradient(
                    colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                ), fontWeight = FontWeight(800)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        ScanQrSection(state, onSuccess = {
            vm.onEvent(WithdrawEvent.QRSuccess(it))
        }, onRetry = {
            vm.onEvent(WithdrawEvent.QRRetry)
        }, onDispose = {
            vm.onEvent(WithdrawEvent.QRDispose)
        })
        Spacer(modifier = Modifier.height(24.dp))

        AppTextField(
            value = state.toAddress,
            onValueChange = { vm.onEvent(WithdrawEvent.ToAddressChanged(it)) },
            label = "Paste your address",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isPasswordToggleEnabled = false,
            trailingIcon = {
                Icon(
                    Icons.Filled.ContentPaste,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            context.pasteFromClipboard()
                        })
            })
        state.toAddressError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall.copy(color = RedTextColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        //PaymentDetailsSection(vm = viewModel)
        AppTextField(
            value = state.amount,
            onValueChange = { vm.onEvent(WithdrawEvent.AmountChanged(it)) },
            label = "Enter Amount",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isPasswordToggleEnabled = false,
        )
        state.amountError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall.copy(color = RedTextColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        SlideToPayButtonSection(
            vm, state, modifier = Modifier.height(50.dp),
            onPay = {
                navController.popBackStack()
            }
        )


    }

}

@Composable
private fun ScanQrSection(
    state: WithdrawState,
    onSuccess: (String) -> Unit,
    onRetry: () -> Unit,
    onDispose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(180.dp)
            .aspectRatio(1f)
            .border(BorderStroke(2.dp, GoldAccentColor), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(greyButtonBackground.copy(alpha = 0.5f)) // Semi-transparent background
            .clickable { /* TODO: Open QR Scanner */ },
        contentAlignment = Alignment.Center,
    ) {

        if (state.isScanning) {
            QrCodeScanner(
                modifier = Modifier.matchParentSize(),
                isScanning = state.isScanning,
                onResult = {
                    onSuccess(it)
                },
                onDispose = {
                    onDispose()
                })
            LaserLineEffect()
        }
        AnimatedVisibility(
            visible = !state.isScanning,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                tint = Color.White,
                modifier = Modifier
                    .padding(10.dp)
                    .size(28.dp)
                    .clickable { onRetry() })
        }
    }

}

@Composable
private fun SlideToPayButtonSection(
    vm: WithdrawViewModel, state: WithdrawState, modifier: Modifier,
    onPay: () -> Unit = {}
) {
    val density = LocalDensity.current
    val buttonWidthDp = 100.dp // Width of the draggable "Pay >" part
    val buttonWidthPx = with(density) { buttonWidthDp.toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(greyButtonBackground, RoundedCornerShape(80))
            .clip(RoundedCornerShape(80)),
        contentAlignment = Alignment.CenterStart // Align draggable part to start initially
    ) {
        Text(
            "Slide to Deposite",
            style = MaterialTheme.typography.titleSmall.copy(color = TextHintColor),
            modifier = Modifier.align(Alignment.Center) // Center the helper text
        )

        when (state.transferInfoResponse) {
            is ResponseState.Loading -> {
                CircularProgressIndicator(
                    color = OnboardingGold1,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .align(Alignment.CenterEnd)
                )
            }

            is ResponseState.Success -> {
                Dialogue(
                    isError = false,
                    msg = (state.transferInfoResponse as ResponseState.Success).data
                ) {
                    vm.onEvent(WithdrawEvent.DismissDialog(1))
                    onPay()
                }
            }

            is ResponseState.Error -> {
                Dialogue(
                    isError = true,
                    msg = (state.transferInfoResponse as ResponseState.Error).message
                ) {
                    vm.onEvent(WithdrawEvent.DismissDialog(1))
                }
            }

            else -> {
                //do nothing
            }
        }

        Box(modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .width(buttonWidthDp + (offsetX / buttonWidthPx * 10.dp).coerceAtLeast(0.dp)) // Slightly expand on drag
            .fillMaxHeight()
            .padding(8.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Gradient_1, Gradient_2, Gradient_3, Gradient_4
                    )
                ), RoundedCornerShape(80)
            )
            .clip(RoundedCornerShape(80))
            .clickable {
                // This is a simplified click, a real slide would use draggable
                if (offsetX > 150f) { // Arbitrary threshold for "slide completion"
                    /* TODO: Execute Payment */
                    println("Payment Executed (Simplified)")
                    offsetX = 0f // Reset
                }
            }
            // For actual dragging:
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val newOffset = (offsetX + delta).coerceIn(
                        0f, /* trackWidthPx - buttonWidthPx */
                        600f
                    ) // Simplified max offset
                    offsetX = newOffset
                },
                onDragStopped = {
                    if (offsetX > 450f) {
                        vm.onEvent(WithdrawEvent.WithdrawAmount)
                        offsetX = 0f
                    } else {
                        offsetX = 0f
                    } // Snap back
                }), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Pay",
                    style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Slide", tint = TextPrimaryColor)
            }
        }
    }
}