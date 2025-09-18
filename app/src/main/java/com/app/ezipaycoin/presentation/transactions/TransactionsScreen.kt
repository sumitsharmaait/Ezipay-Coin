package com.app.ezipaycoin.presentation.transactions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.receivedAmountBackgroundColor
import com.app.ezipaycoin.ui.theme.receivedAmountTextColor
import com.app.ezipaycoin.ui.theme.sendAmountBackgroundColor
import com.app.ezipaycoin.ui.theme.sendAmountTextColor
import com.app.ezipaycoin.utils.shortenAddress

@Composable
fun TransactionScreen(
    navController: NavController,
    viewModel: AllTransactionsViewModel,
    onTransactionClick: (TransactionsResponse.TransactionsItem) -> Unit
) {

    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        TransactionFilters(viewModel, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(20.dp))
        TransactionListWithPagination(
            transactions = state.transactionsList,
            viewModel,
            isLoading = state.isLoading,
            onLoadMore = { viewModel.loadMoreTransactions() },
            onItemClick = { onTransactionClick(it) }
        )
    }

}

@Composable
private fun TransactionFilters(viewModel: AllTransactionsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.filters.forEach { filter ->
            val isSelected = state.selectedFilter == filter
            if (isSelected) {
                GoldGradientButton(
                    label = filter,
                    onClick = { viewModel.onEvent(TransactionsEvent.FilterChanged(filter)) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                AppGreyButton(
                    labelColor = TextPrimaryColor,
                    label = filter,
                    onClick = { viewModel.onEvent(TransactionsEvent.FilterChanged(filter)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TransactionListWithPagination(
    transactions: List<TransactionsResponse.TransactionsItem>,
    viewModel: AllTransactionsViewModel,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    onItemClick: (item: TransactionsResponse.TransactionsItem) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { state.scrollbarSize = it },
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(transactions, key = { it.id }) { transaction ->
                TransactionItemRow(item = transaction, onItemClick = {
                    onItemClick(transaction)
                })
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OnboardingGold1)
                    }
                }
            }
        }

        //CustomScrollbar(listState = listState, containerSize = scrollbarSize)

        // Pagination trigger
        val shouldLoadMore = remember {
            derivedStateOf {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5 // Trigger when 5 items from end
            }
        }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value && !isLoading && state.canLoadMore) {
                onLoadMore()
            }
        }
    }
}

@Composable
private fun TransactionItemRow(
    item: TransactionsResponse.TransactionsItem,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(greyButtonBackground, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onItemClick() }
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