package com.app.ezipaycoin.utils

import wallet.core.jni.HDWallet

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

    const val EZPT_TOKEN_CONTRACT: String = "0xCe135658a2E4db9F503B7922E2429c37d9d21DFc"
    const val USDT_TOKEN_CONTRACT: String = "0x59691E19c4f159BdB1c1eDee5EB2616f8805F27B"

}