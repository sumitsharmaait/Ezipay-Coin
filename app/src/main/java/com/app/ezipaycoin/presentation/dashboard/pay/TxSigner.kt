package com.app.ezipaycoin.presentation.dashboard.pay

import com.app.ezipaycoin.utils.WalletManager.CHAIN_ID
import com.google.protobuf.ByteString
import wallet.core.java.AnySigner
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Ethereum
import java.math.BigInteger

/** Result of signing: the raw transaction bytes, or an error message from the signer. */
class SignedTx(val encoded: ByteArray, val errorMessage: String?)

/** Signs transfers. The wallet-core implementation needs the native library, so tests use a fake. */
interface TxSigner {
    fun signToken(
        nonce: String,
        walletPrivateKey: String,
        contractAddress: String,
        payload: ByteArray,
        gasLimit: String,
        gasPrice: String
    ): SignedTx

    fun signNative(
        nonce: String,
        walletPrivateKey: String,
        gasPrice: String,
        gasLimit: String,
        to: String,
        wei: BigInteger
    ): SignedTx
}

class WalletCoreTxSigner : TxSigner {

    @OptIn(ExperimentalStdlibApi::class)
    override fun signToken(
        nonce: String,
        walletPrivateKey: String,
        contractAddress: String,
        payload: ByteArray,
        gasLimit: String,
        gasPrice: String
    ): SignedTx {

        val contract = AnyAddress(contractAddress, CoinType.SMARTCHAIN)

        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = ByteString.copyFrom(BigInteger.valueOf(CHAIN_ID).toByteArray())
            this.nonce =
                ByteString.copyFrom(BigInteger(nonce.removePrefix("0x"), 16).toByteArray())
            this.gasPrice = BigInteger(gasPrice.removePrefix("0x"), 16).toByteString()
            this.gasLimit = BigInteger(gasLimit.removePrefix("0x"), 16).toByteString()
            this.toAddress = contract.description()
            this.privateKey = ByteString.copyFrom(walletPrivateKey.hexToByteArray())
            transaction = Ethereum.Transaction.newBuilder().apply {
                contractGeneric = Ethereum.Transaction.ContractGeneric.newBuilder().apply {
                    this.data = ByteString.copyFrom(payload)
                    this.amount = ByteString.copyFrom(BigInteger.ZERO.toByteArray())
                }.build()
            }.build()
        }.build()

        val output = AnySigner.sign(input, CoinType.SMARTCHAIN, Ethereum.SigningOutput.parser())
        return output.toSignedTx()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun signNative(
        nonce: String,
        walletPrivateKey: String,
        gasPrice: String,
        gasLimit: String,
        to: String,
        wei: BigInteger
    ): SignedTx {

        val amountBytes = wei.toByteArray()
        val chainId = BigInteger.valueOf(CHAIN_ID).toByteArray()
        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = ByteString.copyFrom(chainId)
            this.nonce =
                ByteString.copyFrom(BigInteger(nonce.removePrefix("0x"), 16).toByteArray())
            this.gasPrice = BigInteger(gasPrice.removePrefix("0x"), 16).toByteString()
            this.gasLimit = BigInteger(gasLimit.removePrefix("0x"), 16).toByteString()
            this.toAddress = to
            this.transaction = Ethereum.Transaction.newBuilder().apply {
                this.transfer = Ethereum.Transaction.Transfer.newBuilder()
                    .setAmount(ByteString.copyFrom(amountBytes))
                    .build()
            }.build()
            this.privateKey = ByteString.copyFrom(walletPrivateKey.hexToByteArray())

        }.build()

        val output = AnySigner.sign(input, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        return output.toSignedTx()
    }

    private fun Ethereum.SigningOutput.toSignedTx() = SignedTx(encoded.toByteArray(), errorMessage)

    private fun BigInteger.toByteString(): ByteString {
        return ByteString.copyFrom(this.toByteArray())
    }
}
