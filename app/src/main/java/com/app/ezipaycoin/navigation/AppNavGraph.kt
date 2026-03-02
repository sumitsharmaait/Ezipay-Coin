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
import com.app.ezipaycoin.data.repository.AuthRepoImpl
import com.app.ezipaycoin.data.repository.DepositWithdrawalRepoImpl
import com.app.ezipaycoin.data.repository.TransactionRepoImpl
import com.app.ezipaycoin.presentation.dashboard.pay.PayScreen
import com.app.ezipaycoin.presentation.dashboard.pay.PayViewModel
import com.app.ezipaycoin.presentation.deposit.Deposit
import com.app.ezipaycoin.presentation.deposit.DepositViewModel
import com.app.ezipaycoin.presentation.depositviacard.DepositViaCard
import com.app.ezipaycoin.presentation.depositviacard.DepositViaCardViewModel
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

fun NavGraphBuilder.appNavGraph(
    navController: NavController,
    apiService: ApiService,
    walletSharedViewModel: WalletSharedViewModel
) {

    val bitTransactions = ApiClient.retrofitPay.create(ApiService::class.java)
    val depositWithdrawalApiClient = ApiClient.depositWithdrawalPay.create(ApiService::class.java)



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
                    val repository = DepositWithdrawalRepoImpl(depositWithdrawalApiClient)
                    DepositViewModel(repository, Json.decodeFromString(token.token))
                }
            )
            Deposit(
                navController = navController,
                viewModel = depositViewModel,
            )
        }

        composable<Screen.AppNavScreens.DepositViaCard> { backStackEntry ->
            val token = backStackEntry.toRoute<Screen.AppNavScreens.DepositViaCard>()
            val depositViaCardViewModel: DepositViaCardViewModel = viewModel(
                factory = ViewModelFactory {
                    val repository = DepositWithdrawalRepoImpl(depositWithdrawalApiClient)
                    DepositViaCardViewModel(repository, Json.decodeFromString(token.token))
                }
            )
            DepositViaCard(
                navController = navController,
                viewModel = depositViaCardViewModel,
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
                    val repository = DepositWithdrawalRepoImpl(depositWithdrawalApiClient)
                    WithdrawViewModel(repository, tokenId = Json.decodeFromString(tokenId.token))
                }
            )
            Withdraw(navController = navController, viewModel = withdrawViewModel)
        }

        composable<Screen.AppNavScreens.WalletDetails> {
            ViewWalletDetails(navController = navController, vm = viewModel<ViewWalletDetailsVM>())
        }

    }

}