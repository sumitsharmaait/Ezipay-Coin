package com.app.ezipaycoin.presentation.dashboard.pay

import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse

/**
 * Payees are the recipients of sent transfers, newest first, one entry per address.
 * Received transfers are ignored (their recipient is the user's own wallet) and so is the own address.
 */
internal fun recentPayees(
    items: List<TransactionsResponse.TransactionsItem>,
    ownAddress: String?,
    limit: Int = 5
): List<TransactionsResponse.TransactionsItem> {
    val seen = HashSet<String>()
    return items.asSequence()
        .filter { it.isSend }
        .filter { tx ->
            val to = tx.to?.trim().orEmpty()
            to.isNotEmpty() &&
                !to.equals(ownAddress?.trim(), ignoreCase = true) &&
                !to.equals(tx.from?.trim(), ignoreCase = true)
        }
        .filter { seen.add(it.to!!.trim().lowercase()) }
        .take(limit)
        .toList()
}
