package com.app.ezipaycoin.presentation.dashboard.pay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.presentation.shared.SharedEvent
import com.app.ezipaycoin.presentation.shared.SharedState
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.receivedAmountBackgroundColor
import com.app.ezipaycoin.ui.theme.receivedAmountTextColor
import com.app.ezipaycoin.ui.theme.sendAmountBackgroundColor
import com.app.ezipaycoin.ui.theme.sendAmountTextColor
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.pasteFromClipboard
import com.app.ezipaycoin.utils.shortenAddress
import kotlin.math.roundToInt

enum class PayScreenTab { DirectPay, Utilities }

@Composable
fun PayScreen(
    navController: NavController,
    viewModel: PayViewModel,
    sharedViewModel: WalletSharedViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val sharedState by sharedViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
        // .verticalScroll(rememberScrollState()) // Scroll handled by LazyColumn in DirectPay
    ) {
        PaySegmentedControl(
            selectedTab = state.selectedTab,
            onTabSelected = { viewModel.onEvent(PayEvent.TabChanged(it)) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        Box(modifier = Modifier.weight(8.7f)) {
            when (state.selectedTab) {
                PayScreenTab.DirectPay -> DirectPayContent(
                    viewModel,
                    state,
                    sharedState,
                    sharedViewModel,
                    modifier = Modifier.fillMaxSize()
                )

                PayScreenTab.Utilities -> UtilitiesContent(
                    viewModel,
                    state,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }


        SlideToPayButtonSection(
            viewModel,
            state,
            sharedState,
            sharedViewModel,
            modifier = Modifier
                .weight(1.3f)
                .padding(all = 16.dp)
        ) // Common slide to pay

    }

}


@Composable
private fun PaySegmentedControl(
    selectedTab: PayScreenTab,
    onTabSelected: (PayScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(35.dp)
            .fillMaxWidth()
            .background(Color.Transparent, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        PayScreenTab.entries.forEach { tab ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(
                        if (selectedTab == tab) greyButtonBackground else Color.Transparent,
                        MaterialTheme.shapes.extraSmall
                    )
                    .border(
                        width = 1.dp,
                        color = greyButtonBackground,
                        MaterialTheme.shapes.extraSmall
                    )
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tab == PayScreenTab.DirectPay) "Direct Pay" else "Utilities",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DirectPayContent(
    vm: PayViewModel,
    state: PayState,
    sharedState: SharedState,
    sharedViewModel: WalletSharedViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    LazyColumn(
        // Use LazyColumn for scrollable content with fixed bottom
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { ScanQrSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            AppTextField(
                value = state.toAddress,
                onValueChange = { vm.onEvent(PayEvent.ToAddressChanged(it)) },
                label = "Paste your address",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                            }
                    )
                }
            )
            state.toAddressError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall.copy(color = TextHintColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { RecentPayeesSection(vm, state) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            PaymentDetailsSection(
                vm,
                state,
                sharedState,
                sharedViewModel
            )
        } // Common payment section
        item { Spacer(modifier = Modifier.height(16.dp)) }
//        item { SlideToPayButtonSection(vm, state) } // Common slide to pay
//        item { Spacer(modifier = Modifier.height(16.dp)) } // Padding at the bottom
    }
}

@Composable
private fun ScanQrSection() {
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
        Text("Scan QR", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
    }
}

data class Payee(
    val initials: String,
    val name: String,
    val date: String,
    val amount: String,
    val isPositive: Boolean
)

@Composable
private fun RecentPayeesSection(vm: PayViewModel, state: PayState) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Recent Payees",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* TODO */ }) {
                Text(
                    "View All",
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Filled.PersonOutline,
                    "Filter Payees",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 120.dp)
                .background(greyButtonBackground, MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            if (state.responseState is ResponseState.Loading) {
                CircularProgressIndicator(color = OnboardingGold1)
            } else if (state.responseState is ResponseState.Success) {
                val data =
                    (state.responseState as ResponseState.Success<BaseResponse<TransactionsResponse>>).data.apiData
                if (data.items.isEmpty()) {
                    Text(
                        "No Transactions Found!!",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                    )
                } else {
                    data.items.forEachIndexed { index, payee ->
                        RecentPayeeItem(item = payee)
                        if (index < data.items.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 0.dp)
                            ) // Indent divider
                        }
                    }
                }
            } else if (state.responseState is ResponseState.Error) {

            }


        }
    }
}

@Composable
fun RecentPayeeItem(item: TransactionsResponse.TransactionsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Select payee */ }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(
                id = when (item.symbol) {
                    "EZPT" -> R.drawable.ic_ezipay_coin_small
                    "BNB" -> R.drawable.ic_currency_top_bar
                    "USDT" -> R.drawable.ic_usdt_icon
                    else -> R.drawable.ic_ezipay_coin_small
                }
            ), "Token", Modifier.size(24.dp)
        )
//        Box(
//            modifier = Modifier
//                .size(36.dp)
//                .background(Color.White, CircleShape),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                item.symbol.take(2),
//                style = MaterialTheme.typography.titleSmall.copy(color = TextSecondaryColor)
//            )
//        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.to.shortenAddress(),
                maxLines = 1,
                modifier = Modifier.padding(end = 8.dp),
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor)
            )
            Text(
                item.formattedDate,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor.copy(alpha = 0.5f))
            )
        }
        Text(
            text = if (item.type.equals(
                    "send",
                    true
                )
            ) "-" + item.formattedAmount else "+" + item.formattedAmount,
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (item.type.equals(
                        "send",
                        true
                    )
                ) sendAmountTextColor else receivedAmountTextColor
            ),
            modifier = Modifier
                .background(
                    if (item.type.equals(
                            "send",
                            true
                        )
                    ) sendAmountBackgroundColor else receivedAmountBackgroundColor,
                    MaterialTheme.shapes.extraSmall
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}


data class UtilityItemData(val label: String, val icon: ImageVector, val id: String)

@Composable
private fun UtilitiesContent(viewModel: PayViewModel, state: PayState, modifier: Modifier) {
    var selectedUtilityId by remember { mutableStateOf<String?>("ATC") }
    // Define utility items
    val utilities = listOf(
        UtilityItemData("Mobile Recharge", Icons.Outlined.Smartphone, "mobile"),
        UtilityItemData("Electricity Bill", Icons.Outlined.Lightbulb, "electricity"),
        UtilityItemData("Water Bill", Icons.Outlined.WaterDrop, "water"),
        UtilityItemData("ATC", Icons.Outlined.Business, "atc"), // Placeholder
        UtilityItemData("Flight", Icons.Outlined.Flight, "flight"),
        UtilityItemData("Hotel", Icons.Outlined.Hotel, "hotel"),
        UtilityItemData("QR", Icons.Outlined.QrCodeScanner, "qr"),
        UtilityItemData(
            "Mobile Recharge",
            Icons.Outlined.Smartphone,
            "mobile2"
        ), // Example duplicate for grid
        UtilityItemData("Electricity Bill", Icons.Outlined.Lightbulb, "electricity2"),
        UtilityItemData("Water Bill", Icons.Outlined.WaterDrop, "water2")
    ).take(9) // Show 9 items in a 3x3 grid as per image

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp), // Adjust height as needed for 3 rows
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(utilities) {
                    UtilityGridItem(
                        item = it,
                        isSelected = state.selectedUtility == it.label,
                        onClick = { viewModel.onEvent(PayEvent.UtilityItemChanged(it.label)) }
                    )
                }
            }
        }

        // Dynamic input fields based on selectedUtilityId
        item { UtilityInputFieldsMobile() }

//        // Add more else if blocks for other utility types
//
//        item { Spacer(modifier = Modifier.height(24.dp)) }
//        item { PaymentDetailsSection() }
//        item { Spacer(modifier = Modifier.height(16.dp)) }
//        item { SlideToPayButtonSection() }
//        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun UtilityGridItem(item: UtilityItemData, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundModifier = if (isSelected)
        Modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Gradient_4,
                    Gradient_2,
                    Gradient_3,
                    Gradient_1
                ),
            ), RoundedCornerShape(12.dp)
        ) else Modifier.background(
        color = Color.Transparent
    )

    Column(
        modifier = Modifier
            .aspectRatio(1.5f) // Make items square
            .then(
                if (!isSelected)
                    Modifier.border(
                        width = 1.dp,
                        color = greyButtonBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
                else Modifier
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .then(backgroundModifier)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            item.label,
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PaymentDetailsSection(
    vm: PayViewModel,
    state: PayState,
    sharedState: SharedState,
    sharedViewModel: WalletSharedViewModel
) {

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = greyButtonBackground, RoundedCornerShape(12.dp))
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(
                    id = when (sharedState.selectedCrypto?.symbol) {
                        "EZPT" -> R.drawable.ic_ezipay_coin_small
                        "BNB" -> R.drawable.ic_currency_top_bar
                        "USDT" -> R.drawable.ic_usdt_icon
                        else -> R.drawable.ic_ezipay_coin_small
                    }
                ), "Token", Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                sharedState.selectedCrypto?.symbol ?: "0.00",
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor)
            )

            DropdownMenu(
                expanded = state.isCurrencyOptionsExpanded,
                onDismissRequest = { vm.onEvent(PayEvent.ExpandCurrencyOptions(false)) }
            ) {
                sharedState.dashboardResponse?.crypto?.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.symbol) },
                        onClick = {
                            sharedViewModel.onEvent(SharedEvent.CryptoOptionChanged(item))
                            vm.onEvent(PayEvent.CurrentOptionChanged(item))
                        }
                    )
                }
            }

            Icon(
                Icons.Filled.ArrowDropDown,
                "Select Token",
                tint = Color.White,
                modifier = Modifier.clickable { vm.onEvent(PayEvent.ExpandCurrencyOptions(true)) })


            // Amount Input - Custom implementation needed for "$" prefix fixed
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "$",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimaryColor
                    )
                )
                Spacer(Modifier.width(4.dp))
                TextField(
                    value = state.amount,
                    label = {
                        Text(
                            text = "Enter Amount",
                            color = TextHintColor,
                            textAlign = TextAlign.End
                        )
                    },
                    onValueChange = { vm.onEvent(PayEvent.AmountChanged(it)) },
                    modifier = Modifier.widthIn(1.dp, Dp.Infinity), // Adjust width as needed
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimaryColor,
                        textAlign = TextAlign.End
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        cursorColor = GoldAccentColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                )
            }

//            TextButton(
//                onClick = { /* TODO: Set Max Amount */ },
//                modifier = Modifier.padding(start = 8.dp)
//            ) {
//                Text(
//                    "Max",
//                    style = MaterialTheme.typography.bodyMedium.copy(color = GoldAccentColor)
//                )
//            }
        }

        state.amountError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall.copy(color = TextHintColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "Balance: ",
                style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor),
            )
            if (sharedState.responseState == ResponseState.Loading) {
                CircularProgressIndicator(
                    color = OnboardingGold1,
                    strokeWidth = 1.dp,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
            } else {
                Text(
                    "" + sharedState.selectedCrypto?.formattedBalance,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor),
                )
            }

        }
    }


}


@Composable
private fun SlideToPayButtonSection(
    vm: PayViewModel,
    state: PayState,
    sharedState: SharedState,
    sharedViewModel: WalletSharedViewModel,
    modifier: Modifier
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
            "Slide to Pay",
            style = MaterialTheme.typography.titleSmall.copy(color = TextHintColor),
            modifier = Modifier.align(Alignment.Center) // Center the helper text
        )

        when (state.payMoneyResponse) {
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
                    msg = (state.payMoneyResponse as ResponseState.Success).data
                ) {
                    sharedViewModel.onEvent(SharedEvent.FetchBalance)
                    vm.onEvent(PayEvent.DismissDialog)
                }
            }

            is ResponseState.Error -> {
                Dialogue(
                    isError = true,
                    msg = (state.payMoneyResponse as ResponseState.Error).message
                ) {
                    vm.onEvent(PayEvent.DismissDialog)
                }
            }

            else -> {
                //do nothing
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .width(buttonWidthDp + (offsetX / buttonWidthPx * 10.dp).coerceAtLeast(0.dp)) // Slightly expand on drag
                .fillMaxHeight()
                .padding(8.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Gradient_1,
                            Gradient_2,
                            Gradient_3,
                            Gradient_4
                        )
                    ),
                    RoundedCornerShape(80)
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
                            if (state.selectedTab == PayScreenTab.DirectPay) {
                                sharedState.selectedCrypto?.symbol?.let {
                                    vm.onEvent(PayEvent.PayAmount(it))
                                }
                            }
                            offsetX = 0f
                        } else {
                            offsetX = 0f
                        } // Snap back
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
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

@Composable
fun UtilityInputFieldsMobile() {
    Column {
        var mobileNumber by remember { mutableStateOf("") }
        var selectedOperator by remember { mutableStateOf("") } // Could be a dropdown
        var amount by remember { mutableStateOf("") }

        Spacer(modifier = Modifier.height(16.dp))
        AppTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = "Enter mobile number"
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(
            value = selectedOperator,
            onValueChange = { selectedOperator = it },
            label = "Select Operator",
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    "Dropdown",
                    tint = TextHintColor
                )
            })
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(
            value = amount,
            onValueChange = { amount = it },
            label = "Enter amount",
            keyboardOptions = KeyboardOptions.Default
        )
    }
}










