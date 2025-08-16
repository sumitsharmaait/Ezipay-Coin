package com.app.ezipaycoin.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.repository.HomeRepoImpl
import com.app.ezipaycoin.presentation.dashboard.earn.EarnScreen
import com.app.ezipaycoin.presentation.dashboard.home.HomeScreen
import com.app.ezipaycoin.presentation.dashboard.home.HomeViewModel
import com.app.ezipaycoin.presentation.dashboard.learn.LearnScreen
import com.app.ezipaycoin.presentation.dashboard.learn.LearnViewModel
import com.app.ezipaycoin.presentation.dashboard.wallet.WalletScreen
import com.app.ezipaycoin.presentation.dashboard.wallet.WalletViewModel
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.utils.ViewModelFactory

fun NavGraphBuilder.bottomNavGraph(
    navController: NavHostController,
    apiService: ApiService,
    walletSharedViewModel: WalletSharedViewModel
) {

    navigation<Screen.BottomNavScreens>(
        startDestination = Screen.BottomNavScreens.Home,
    ) {
        composable<Screen.BottomNavScreens.Home> {
            val homeViewModel: HomeViewModel = viewModel(
                factory = ViewModelFactory {
                    val repository = HomeRepoImpl(apiService)
                    HomeViewModel(repository)
                }
            )
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                walletSharedViewModel
            )
        }

        composable<Screen.BottomNavScreens.Wallet> {
            val walletViewModel: WalletViewModel = viewModel(
                factory = ViewModelFactory {
                    val repository = HomeRepoImpl(apiService)
                    WalletViewModel(repository)
                }
            )
            WalletScreen(
                navController = navController,
                viewModel = walletViewModel,
                walletSharedViewModel
            )
        }


        composable<Screen.BottomNavScreens.Learn> {
            LearnScreen(navController = navController, viewModel = viewModel<LearnViewModel>())
        }

        composable<Screen.BottomNavScreens.Earn> {
            EarnScreen()
        }


    }

}
