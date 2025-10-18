package com.app.ezipaycoin.utils

import wallet.core.jni.Curve
import wallet.core.jni.HDWallet
import wallet.core.jni.Hash
import wallet.core.jni.PrivateKey

object WalletManager {

    private var _wallet: HDWallet? = null

    val wallet: HDWallet
        get() = _wallet ?: throw IllegalStateException("Wallet not initialized.")

    fun isInitialized(): Boolean = _wallet != null

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
        maxLength: Int = 20
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
    const val EZPT_TOKEN_CONTRACT: String = "0x50387a24253150e808875f3cac8111546623ba64" // main net

    //const val USDT_TOKEN_CONTRACT: String = "0x59691E19c4f159BdB1c1eDee5EB2616f8805F27B" //test net
    const val USDT_TOKEN_CONTRACT: String = "0x55d398326f99059fF775485246999027B3197955" //main net

    //const val CHAIN_ID = 97L //test net
    const val CHAIN_ID = 56L //main net

}