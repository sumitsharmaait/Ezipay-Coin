package com.app.ezipaycoin.utils

import com.app.ezipaycoin.config.ChainConfig
import wallet.core.jni.Curve
import wallet.core.jni.HDWallet
import wallet.core.jni.Hash
import wallet.core.jni.PrivateKey
import wallet.core.jni.CoinType

object WalletManager {

    private var _wallet: HDWallet? = null

    val wallet: HDWallet
        get() = _wallet ?: throw IllegalStateException("Wallet not initialized.")

    fun isInitialized(): Boolean = _wallet != null

    /** Photo URL from a social sign-up, held until the wallet is persisted after the seed backup. */
    var pendingProfilePic: String? = null

    /** BSC receive address, or null when the wallet isn't unlocked yet. */
    val address: String?
        get() = _wallet?.getAddressForCoin(CoinType.SMARTCHAIN)

    /** Drops the in-memory wallet and anything held for a sign-up in progress. */
    fun clear() {
        _wallet = null
        pendingProfilePic = null
    }

    fun createNewWallet(): HDWallet {
        val wallet = HDWallet(128, "")
        _wallet = wallet
        return wallet
    }

    fun importWallet(mnemonic: String): HDWallet {
        val wallet = HDWallet(mnemonic, "")
        _wallet = wallet
        return wallet
    }

//    fun loadWalletIfExists(context: Context): Boolean {
//        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        val mnemonic = prefs.getString(MNEMONIC_KEY, null)
//        return if (mnemonic != null) {
//            _wallet = HDWallet(mnemonic, "")
//            true
//        } else {
//            false
//        }
//    }

    @OptIn(ExperimentalStdlibApi::class)
    fun signNonceWithTrustWalletCore(nonce: String, privateKey: PrivateKey): String {

        // Ethereum-style signed message prefix
        val prefix = "\u0019Ethereum Signed Message:\n" + nonce.length
        val prefixedMessage = prefix + nonce

        // Hash the message using Keccak-256
        val messageHash = Hash.keccak256(prefixedMessage.toByteArray())

        // Sign the message hash
        val signature = privateKey.sign(messageHash, Curve.SECP256K1)

        // Return hex representation of the signature
        return signature.toHexString()
    }

    fun generateInvoiceNo(
        minLength: Int = 10,
        maxLength: Int = 15
    ): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val suffix = System.currentTimeMillis()
            .toString()
            .takeLast(6)
        val maxRandomLength = (minLength..maxLength).random() - suffix.length
            .coerceAtLeast(1)

        val randomPart = (1..maxRandomLength)
            .map { chars.random() }
            .joinToString("")

        return "$randomPart$suffix"
    }

    // The chain of this process (config/ChainConfig.kt): the build type's chain, or the one picked
    // in Profile on debug and staging builds.
    val EZPT_TOKEN_CONTRACT: String get() = ChainConfig.current.ezptTokenContract

    val USDT_TOKEN_CONTRACT: String get() = ChainConfig.current.usdtTokenContract

    val CHAIN_ID: Long get() = ChainConfig.current.chainId

}