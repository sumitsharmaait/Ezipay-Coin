package com.app.ezipaycoin.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.app.ezipaycoin.data.remote.api.ApiClient
import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.repository.TransactionRepoImpl
import com.app.ezipaycoin.presentation.dashboard.pay.PayScreen
import com.app.ezipaycoin.presentation.dashboard.pay.PayViewModel
import com.app.ezipaycoin.presentation.profile.MyProfile
import com.app.ezipaycoin.presentation.profile.MyProfileViewModel
import com.app.ezipaycoin.presentation.receive.ReceiveScreen
import com.app.ezipaycoin.presentation.receive.ReceiveViewModel
import com.app.ezipaycoin.presentation.shared.SharedEvent
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.presentation.transactiondetails.TransactionDetailsScreen
import com.app.ezipaycoin.presentation.transactions.AllTransactionsViewModel
import com.app.ezipaycoin.presentation.transactions.TransactionScreen
import com.app.ezipaycoin.utils.ViewModelFactory

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
            walletSharedViewModel.uiState.value.selectedTransaction?.let {
                TransactionDetailsScreen(item = it, onExplorerClick = {})
            }
        }

        composable<Screen.AppNavScreens.MyProfile> {
            MyProfile(viewModel = viewModel<MyProfileViewModel>())
        }

        composable<Screen.AppNavScreens.Receive> {
            ReceiveScreen(navController = navController, viewModel = viewModel<ReceiveViewModel>())
        }

    }

}