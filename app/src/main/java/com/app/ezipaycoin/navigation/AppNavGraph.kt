package com.app.ezipaycoin.navigation

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.app.ezipaycoin.data.remote.api.ApiClient
import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.liveness.LivenessSocketClient
import com.app.ezipaycoin.data.repository.AuthRepoImpl
import com.app.ezipaycoin.data.repository.DepositWithdrawalRepoImpl
import com.app.ezipaycoin.data.repository.TransactionRepoImpl
import com.app.ezipaycoin.presentation.dashboard.pay.PayScreen
import com.app.ezipaycoin.presentation.dashboard.pay.PayViewModel
import com.app.ezipaycoin.presentation.deposit.Deposit
import com.app.ezipaycoin.presentation.deposit.DepositViewModel
import com.app.ezipaycoin.presentation.profile.MyProfile
import com.app.ezipaycoin.presentation.profile.MyProfileViewModel
import com.app.ezipaycoin.presentation.receive.ReceiveScreen
import com.app.ezipaycoin.presentation.receive.ReceiveViewModel
import com.app.ezipaycoin.presentation.shared.SharedEvent
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.presentation.transactiondetails.TransactionDetailsScreen
import com.app.ezipaycoin.presentation.transactions.AllTransactionsViewModel
import com.app.ezipaycoin.presentation.transactions.TransactionScreen
import com.app.ezipaycoin.presentation.viewwalletdetails.ViewWalletDetails
import com.app.ezipaycoin.presentation.viewwalletdetails.ViewWalletDetailsVM
import com.app.ezipaycoin.presentation.webview.WebViewScreen
import com.app.ezipaycoin.presentation.withdraw.Withdraw
import com.app.ezipaycoin.presentation.withdraw.WithdrawViewModel
import com.app.ezipaycoin.utils.ViewModelFactory
import kotlinx.serialization.json.Json

import com.app.ezipaycoin.data.repository.KycRepoImpl
import com.app.ezipaycoin.data.repository.KycVideoRepoImpl
import com.app.ezipaycoin.data.repository.TokenRepoImpl
import com.app.ezipaycoin.presentation.kyc.KycScreen
import com.app.ezipaycoin.presentation.kyc.KycStatusScreen
import com.app.ezipaycoin.presentation.kyc.KycViewModel
import com.app.ezipaycoin.presentation.addtoken.AddCustomTokenScreen
import com.app.ezipaycoin.presentation.addtoken.AddTokenViewModel

import com.app.ezipaycoin.data.repository.NftRepoImpl
import com.app.ezipaycoin.presentation.nft.NftDetailScreen
import com.app.ezipaycoin.presentation.nft.mint.NftMintScreen
import com.app.ezipaycoin.presentation.nft.mint.NftMintViewModel
import com.app.ezipaycoin.presentation.nft.NftDetailViewModel

fun NavGraphBuilder.appNavGraph(
    navController: NavController,
    apiService: ApiService,
    walletSharedViewModel: WalletSharedViewModel
) {

    val bitTransactions = ApiClient.retrofitPay.create(ApiService::class.java)



    navigation<Screen.AppNavScreens>(
        startDestination = Screen.AppNavScreens.Pay
    ) {
        composable<Screen.AppNavScreens.Pay> {
            val payViewModel: PayViewModel = viewModel(
                factory = ViewModelFactory {
                    val repository = TransactionRepoImpl(apiService, bitTransactions)
                    PayViewModel(repository)
                }
            )
            PayScreen(
                navController,
                payViewModel,
                sharedViewModel = walletSharedViewModel,
                onClickViewAllTransactions = { navController.navigate(Screen.AppNavScreens.Transactions) })
        }


        composable<Screen.AppNavScreens.Transactions> {
            val allTransactionsViewModel: AllTransactionsViewModel = viewModel(
                factory = ViewModelFactory {
                    val repository = TransactionRepoImpl(apiService, bitTransactions)
                    AllTransactionsViewModel(repository)
                }
            )
            TransactionScreen(
                navController = navController,
                viewModel = allTransactionsViewModel,
                onTransactionClick = {
                    walletSharedViewModel.onEvent(SharedEvent.TransactionSelected(it))
                    navController.navigate(Screen.AppNavScreens.TransactionDetails)
                })
        }

        composable<Screen.AppNavScreens.TransactionDetails> {
            walletSharedViewModel.uiState.collectAsState().value.selectedTransaction?.let {
                TransactionDetailsScreen(item = it, onExplorerClick = { id ->
                    walletSharedViewModel.onEvent(SharedEvent.OpenUrl("https://bscscan.com/tx/$id"))
                })
            }
        }

        composable<Screen.AppNavScreens.MyProfile> {
            val myProfileViewModel: MyProfileViewModel = viewModel(
                factory = ViewModelFactory {
                    val apiService = ApiClient.retrofit.create(ApiService::class.java)
                    val repository = AuthRepoImpl(apiService)
                    MyProfileViewModel(repository)
                }
            )
            MyProfile(navController = navController, viewModel = myProfileViewModel)
        }

        composable<Screen.AppNavScreens.Receive> {
            ReceiveScreen(navController = navController, viewModel = viewModel<ReceiveViewModel>())
        }

        composable<Screen.AppNavScreens.Deposit> { backStackEntry ->
            val token = backStackEntry.toRoute<Screen.AppNavScreens.Deposit>()
            val depositViewModel: DepositViewModel = viewModel(
                factory = ViewModelFactory {
                    val repository = DepositWithdrawalRepoImpl(apiService)
                    DepositViewModel(repository, Json.decodeFromString(token.token))
                }
            )
            Deposit(
                navController = navController,
                viewModel = depositViewModel,
            )
        }

        composable<Screen.AppNavScreens.WebViewScreen> { backStackEntry ->
            val url = backStackEntry.toRoute<Screen.AppNavScreens.WebViewScreen>()
            WebViewScreen(url = url.url, onBack = { navController.popBackStack() })
        }


        composable<Screen.AppNavScreens.Withdraw> { navBackStackEntry ->
            val tokenId = navBackStackEntry.toRoute<Screen.AppNavScreens.Withdraw>()
            val withdrawViewModel: WithdrawViewModel = viewModel(
                factory = ViewModelFactory {
                    val repository = DepositWithdrawalRepoImpl(apiService)
                    WithdrawViewModel(repository, tokenId = Json.decodeFromString(tokenId.token))
                }
            )
            Withdraw(navController = navController, viewModel = withdrawViewModel)
        }

        composable<Screen.AppNavScreens.WalletDetails> {
            ViewWalletDetails(navController = navController, vm = viewModel<ViewWalletDetailsVM>())
        }

        composable<Screen.AppNavScreens.KycStatus> {
            val kycViewModel: KycViewModel = viewModel(
                factory = ViewModelFactory {
                    val api = ApiClient.retrofit.create(ApiService::class.java)
                    val repository = KycRepoImpl(api)
                    val videoRepository = KycVideoRepoImpl(api)
                    val socketClient = LivenessSocketClient(
                        okhttp3.OkHttpClient.Builder()
                            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS) // keep socket open
                            .build()
                    )
                    KycViewModel(repository, videoRepository, socketClient)
                }
            )
            KycStatusScreen(
                navController = navController,
                viewModel = kycViewModel,
                onStartVerification = { navController.navigate(Screen.AppNavScreens.KycUpload) }
            )
        }

        composable<Screen.AppNavScreens.KycUpload> {
            val kycViewModel: KycViewModel = viewModel(
                factory = ViewModelFactory {
                    val api = ApiClient.retrofit.create(ApiService::class.java)
                    val repository = KycRepoImpl(api)
                    val videoRepository = KycVideoRepoImpl(api)
                    val socketClient = LivenessSocketClient(
                        okhttp3.OkHttpClient.Builder()
                            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS) // keep socket open
                            .build()
                    )
                    KycViewModel(repository, videoRepository, socketClient)
                }
            )
            KycScreen(
                navController = navController,
                viewModel = kycViewModel,
                onSubmitted = {
                    navController.popBackStack(Screen.AppNavScreens.KycStatus, inclusive = false)
                }
            )
        }

        composable<Screen.AppNavScreens.AddToken> {
            val addTokenViewModel: AddTokenViewModel = viewModel(
                factory = ViewModelFactory {
                    val api = ApiClient.retrofit.create(ApiService::class.java)
                    val repository = TokenRepoImpl(api)
                    AddTokenViewModel(repository)
                }
            )
            AddCustomTokenScreen(
                navController = navController,
                viewModel = addTokenViewModel,
                onTokenAdded = { navController.popBackStack() }
            )
        }

        composable<Screen.AppNavScreens.NftDetail> { entry ->
            val args = entry.toRoute<Screen.AppNavScreens.NftDetail>()
            val vm: NftDetailViewModel = viewModel(
                factory = ViewModelFactory { NftDetailViewModel(NftRepoImpl(apiService)) }
            )
            NftDetailScreen(
                tokenId = args.tokenId,
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.AppNavScreens.NftMint> {
            val vm: NftMintViewModel = viewModel(
                factory = ViewModelFactory { NftMintViewModel(NftRepoImpl(apiService)) }
            )
            NftMintScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onMinted = { navController.popBackStack() }
            )
        }

    }

}