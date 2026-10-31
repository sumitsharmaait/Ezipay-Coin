package com.app.ezipaycoin.presentation.nft.mint

import com.app.ezipaycoin.data.remote.dto.response.NftCollectionResponse
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

import android.util.Log

/* enum class SalePhase {
    CLOSED, ALLOWLIST, PUBLIC;

    companion object {
        fun from(raw: String?): SalePhase = when (raw?.lowercase()?.trim()) {
            "public" -> PUBLIC
            "allowlist", "allow_list", "presale" -> ALLOWLIST
            else -> CLOSED
        }
    }
} */

enum class SalePhase(val code: Int) {
    CLOSED(0), ALLOWLIST(1), PUBLIC(2);

    companion object {
        fun from(raw: String?): SalePhase {
            val s = raw?.trim().orEmpty()
            val match = s.toIntOrNull()?.let { n -> entries.firstOrNull { it.code == n } }
                ?: when (s.lowercase()) {
                    "public" -> PUBLIC
                    "allowlist", "allow_list", "presale" -> ALLOWLIST
                    "closed" -> CLOSED
                    else -> null
                }

            if (match == null) Log.w("SalePhase", "Unrecognised salePhase: '$raw'")
            return match ?: CLOSED
        }
    }
}

data class NftMintState(
    val isLoading: Boolean = true,
    val isMinting: Boolean = false,
    val collection: NftCollectionResponse? = null,
    val phase: SalePhase = SalePhase.CLOSED,
    val quantity: Int = 1,
    /**
     * Merkle proof for allowlist minting.
     *
     * GAP: there is no endpoint in the API that hands the client a proof. Until one
     * exists, allowlist mints will fail on-chain. Options: add
     * GET /api/v1/Nft/allowlist/proof, or have the backend look the proof up itself
     * from the JWT wallet address and ignore this field.
     */
    val proof: List<String> = emptyList(),
    val transactionHash: String? = null,
    val error: String? = null
) {
    private val unitPriceWei: BigInteger?
        get() = when (phase) {
            SalePhase.PUBLIC -> collection?.publicPriceWei
            SalePhase.ALLOWLIST -> collection?.allowlistPriceWei
            SalePhase.CLOSED -> null
        }?.toBigIntegerOrNull()

    val maxPerWallet: Int get() = collection?.maxPerWallet?.takeIf { it > 0 } ?: 10

    val totalPriceWei: BigInteger?
        get() = unitPriceWei?.multiply(BigInteger.valueOf(quantity.toLong()))

    val unitPriceLabel: String get() = unitPriceWei.toBnbLabel()

    val totalPriceLabel: String get() = totalPriceWei.toBnbLabel()

    val remainingSupplyLabel: String?
        get() {
            val minted = collection?.totalMinted ?: return null
            val max = collection?.maxSupply ?: return null
            return "$minted of $max minted"
        }

    val canMint: Boolean
        get() = !isLoading &&
            !isMinting &&
            phase != SalePhase.CLOSED &&
            quantity in 1..maxPerWallet

    val canDecrement: Boolean get() = !isMinting && quantity > 1

    val canIncrement: Boolean get() = !isMinting && quantity < maxPerWallet
}

private const val WEI_DECIMALS = 18

private fun String.toBigIntegerOrNull(): BigInteger? =
    runCatching { BigInteger(trim()) }.getOrNull()

/** Formats a wei amount as a BNB string. Never routes through Double. */
internal fun BigInteger?.toBnbLabel(): String {
    if (this == null) return "--"
    val bnb = BigDecimal(this).movePointLeft(WEI_DECIMALS)
    return bnb.setScale(6, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString() + " BNB"
}
