package com.app.ezipaycoin.utils

import com.app.ezipaycoin.BuildConfig
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

    //const val EZPT_TOKEN_CONTRACT: String = "0xCe135658a2E4db9F503B7922E2429c37d9d21DFc" //test net
    // Mainnet for debug and release, the local chain for the sandbox build type.
    const val EZPT_TOKEN_CONTRACT: String = BuildConfig.EZPT_TOKEN_CONTRACT

    //const val USDT_TOKEN_CONTRACT: String = "0x59691E19c4f159BdB1c1eDee5EB2616f8805F27B" //test net
    const val USDT_TOKEN_CONTRACT: String = BuildConfig.USDT_TOKEN_CONTRACT

    //const val CHAIN_ID = 97L //test net
    const val CHAIN_ID = BuildConfig.CHAIN_ID

}