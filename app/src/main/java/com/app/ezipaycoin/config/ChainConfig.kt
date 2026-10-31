package com.app.ezipaycoin.config

import android.content.Context
import android.content.SharedPreferences
import com.app.ezipaycoin.BuildConfig

/** Everything the app needs to sign for and read from one chain. All values come from BuildConfig. */
data class ChainValues(
    val chainId: Long,
    val name: String,
    val rpcUrl: String,
    val ezptTokenContract: String,
    val usdtTokenContract: String,
    val explorerTxUrl: String,
) {
    val label: String get() = "$name ($chainId)"
}

/**
 * The chain this process uses. Release builds always use the build's chain. Debug, staging and UAT
 * builds ([BuildConfig.CHAIN_SWITCHABLE]) can pick testnet or mainnet in Profile; the choice is
 * stored and applies from the next start, so every client built at start-up sees one chain.
 */
object ChainConfig {

    private const val PREFS = "chain_config"
    private const val KEY_CHAIN_ID = "chain_id"

    val buildChain = ChainValues(
        BuildConfig.CHAIN_ID, BuildConfig.CHAIN_NAME, BuildConfig.RPC_URL,
        BuildConfig.EZPT_TOKEN_CONTRACT, BuildConfig.USDT_TOKEN_CONTRACT, BuildConfig.EXPLORER_TX_URL
    )

    val testnet = ChainValues(
        BuildConfig.TESTNET_CHAIN_ID, BuildConfig.TESTNET_CHAIN_NAME, BuildConfig.TESTNET_RPC_URL,
        BuildConfig.TESTNET_EZPT_TOKEN_CONTRACT, BuildConfig.TESTNET_USDT_TOKEN_CONTRACT,
        BuildConfig.TESTNET_EXPLORER_TX_URL
    )

    val mainnet = ChainValues(
        BuildConfig.MAINNET_CHAIN_ID, BuildConfig.MAINNET_CHAIN_NAME, BuildConfig.MAINNET_RPC_URL,
        BuildConfig.MAINNET_EZPT_TOKEN_CONTRACT, BuildConfig.MAINNET_USDT_TOKEN_CONTRACT,
        BuildConfig.MAINNET_EXPLORER_TX_URL
    )

    val switchable: Boolean get() = BuildConfig.CHAIN_SWITCHABLE

    /** What the switch offers; only the build's chain when switching is off. */
    val choices: List<ChainValues> get() = if (switchable) listOf(testnet, mainnet) else listOf(buildChain)

    private var prefs: SharedPreferences? = null

    @Volatile
    private var active: ChainValues = buildChain

    /** The chain of this process. The build's chain until [init] has read a stored choice. */
    val current: ChainValues get() = active

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = runCatching { prefs?.takeIf { it.contains(KEY_CHAIN_ID) }?.getLong(KEY_CHAIN_ID, 0L) }.getOrNull()
        active = resolve(buildChain, listOf(testnet, mainnet), switchable, stored)
    }

    /** The chain the next start will use. */
    fun selected(): ChainValues {
        val stored = runCatching { prefs?.takeIf { it.contains(KEY_CHAIN_ID) }?.getLong(KEY_CHAIN_ID, 0L) }.getOrNull()
        return resolve(buildChain, listOf(testnet, mainnet), switchable, stored)
    }

    /** Stores the choice for the next start. Ignored when the build cannot switch. */
    fun select(chainId: Long) {
        if (!switchable) return
        val toStore = overrideToStore(chainId, buildChain, listOf(testnet, mainnet))
        runCatching {
            prefs?.edit()?.apply {
                if (toStore == null) remove(KEY_CHAIN_ID) else putLong(KEY_CHAIN_ID, toStore)
            }?.commit()
        }
    }

    /** The build's chain unless switching is allowed and [override] names a known chain. */
    internal fun resolve(
        build: ChainValues,
        known: List<ChainValues>,
        switchable: Boolean,
        override: Long?,
    ): ChainValues {
        if (!switchable || override == null) return build
        return known.firstOrNull { it.chainId == override } ?: build
    }

    /** Nothing is stored for the build's own chain or an unknown one, so the build default applies. */
    internal fun overrideToStore(chainId: Long, build: ChainValues, known: List<ChainValues>): Long? =
        chainId.takeIf { it != build.chainId && known.any { chain -> chain.chainId == it } }
}
