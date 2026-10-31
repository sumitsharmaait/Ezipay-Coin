package com.app.ezipaycoin.config

import com.app.ezipaycoin.BuildConfig
import com.app.ezipaycoin.utils.WalletManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChainConfigTest {

    private val testnet = ChainValues(97L, "BSC Testnet", "https://testnet.rpc", "0xezpt97", "0xusdt97", "https://testnet.explorer/tx/")
    private val mainnet = ChainValues(56L, "BSC Mainnet", "https://mainnet.rpc", "0xezpt56", "0xusdt56", "https://explorer/tx/")
    private val known = listOf(testnet, mainnet)

    @Test
    fun `without a stored choice the build chain is used`() {
        assertEquals(testnet, ChainConfig.resolve(testnet, known, switchable = true, override = null))
        assertEquals(mainnet, ChainConfig.resolve(mainnet, known, switchable = true, override = null))
    }

    @Test
    fun `a switchable build uses the stored choice`() {
        assertEquals(mainnet, ChainConfig.resolve(testnet, known, switchable = true, override = 56L))
        assertEquals(testnet, ChainConfig.resolve(mainnet, known, switchable = true, override = 97L))
    }

    @Test
    fun `a build that cannot switch ignores a stored choice`() {
        // Release: CHAIN_SWITCHABLE is false, so a value left in storage never moves it off mainnet.
        assertEquals(mainnet, ChainConfig.resolve(mainnet, known, switchable = false, override = 97L))
    }

    @Test
    fun `an unknown stored chain falls back to the build chain`() {
        assertEquals(testnet, ChainConfig.resolve(testnet, known, switchable = true, override = 1L))
    }

    @Test
    fun `choosing the build chain stores nothing`() {
        assertNull(ChainConfig.overrideToStore(97L, testnet, known))
        assertEquals(56L, ChainConfig.overrideToStore(56L, testnet, known))
        assertNull(ChainConfig.overrideToStore(31337L, testnet, known))
    }

    @Test
    fun `debug build defaults to testnet and can switch to mainnet`() {
        // Unit tests run against the debug BuildConfig.
        assertTrue(BuildConfig.CHAIN_SWITCHABLE)
        assertEquals(97L, ChainConfig.buildChain.chainId)
        assertEquals(97L, ChainConfig.testnet.chainId)
        assertEquals(56L, ChainConfig.mainnet.chainId)
        assertEquals(listOf(ChainConfig.testnet, ChainConfig.mainnet), ChainConfig.choices)
        assertNotEquals(ChainConfig.testnet.ezptTokenContract, ChainConfig.mainnet.ezptTokenContract)
        assertTrue(ChainConfig.mainnet.explorerTxUrl.startsWith("https://"))
    }

    @Test
    fun `before init the process uses the build chain`() {
        assertEquals(ChainConfig.buildChain, ChainConfig.current)
        assertEquals(ChainConfig.buildChain.chainId, WalletManager.CHAIN_ID)
        assertEquals(ChainConfig.buildChain.ezptTokenContract, WalletManager.EZPT_TOKEN_CONTRACT)
    }

    @Test
    fun `label names the chain and its id`() {
        assertEquals("BSC Mainnet (56)", mainnet.label)
    }
}
