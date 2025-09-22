package com.app.ezipaycoin.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.app.ezipaycoin.data.remote.api.ApiClient
import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.repository.AuthRepoImpl
import com.app.ezipaycoin.presentation.confirmseedphrase.ConfirmSeedPhraseScreen
import com.app.ezipaycoin.presentation.confirmseedphrase.ConfirmSeedPhraseViewModel
import com.app.ezipaycoin.presentation.importfromseed.ImportFromSeedScreen
import com.app.ezipaycoin.presentation.importfromseed.ImportFromSeedViewModel
import com.app.ezipaycoin.presentation.onboarding.OnboardingScreen
import com.app.ezipaycoin.presentation.securewallet.SecureWallet
import com.app.ezipaycoin.presentation.seedphraseview.ViewSeedPhraseViewModel
import com.app.ezipaycoin.presentation.seedphraseview.WriteDownSeedPhraseScreen
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.presentation.success.WalletSuccess
import com.app.ezipaycoin.presentation.walletsetup.CreateNewWallet
import com.app.ezipaycoin.presentation.walletsetup.CreatePassword
import com.app.ezipaycoin.presentation.walletsetup.WalletSetUp
import com.app.ezipaycoin.utils.ViewModelFactory


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    walletSharedViewModel: WalletSharedViewModel
) {
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    val repository = AuthRepoImpl(apiService)

    navigation<Screen.Auth>(
        startDestination = Screen.Auth.WalkThrough
    ) {
        composable<Screen.Auth.WalkThrough> {
            OnboardingScreen(navController = navController)
        }

        composable<Screen.Auth.WalletSetup> {
            WalletSetUp(navController = navController)
        }

        composable<Screen.Auth.ImportFromSeed> {
            val importFromSeedViewModel: ImportFromSeedViewModel = viewModel(
                factory = ViewModelFactory {
                    ImportFromSeedViewModel(repository)
                }
            )
            ImportFromSeedScreen(navController = navController, importFromSeedViewModel)
        }

        composable<Screen.Auth.CreatePassword> {
            CreatePassword(navController = navController)
        }

        composable<Screen.Auth.CreateNewWallet> {
            CreateNewWallet(navController = navController)
        }

        composable<Screen.Auth.SecureWallet> {
            SecureWallet(navController = navController)
        }

        composable<Screen.Auth.SeedPhrase> {
            val viewSeedPhraseViewModel: ViewSeedPhraseViewModel = viewModel(
                factory = ViewModelFactory {
                    ViewSeedPhraseViewModel(repository)
                }
            )
            WriteDownSeedPhraseScreen(navController = navController, viewSeedPhraseViewModel)
        }
        composable<Screen.Auth.SeedPhraseVerify> { backStackEntry ->
            val seeds = backStackEntry.toRoute<Screen.Auth.SeedPhraseVerify>()
            val confirmSeedPhraseViewModel: ConfirmSeedPhraseViewModel = viewModel(
                factory = ViewModelFactory {
                    ConfirmSeedPhraseViewModel(repository)
                }
            )
            ConfirmSeedPhraseScreen(
                navController = navController,
                originalSeeds = seeds.phrases,
                confirmSeedPhraseViewModel
            )
        }

        composable<Screen.Auth.WalletSuccess> {
            WalletSuccess(
                navController = navController,
                walletSharedViewModel = walletSharedViewModel
            )
        }


    }

}