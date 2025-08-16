package com.app.ezipaycoin.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.ezipaycoin.data.remote.api.ApiClient
import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.repository.HomeRepoImpl
import com.app.ezipaycoin.navigation.Navigation
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.presentation.App
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.ui.composables.AppDrawerContent
import com.app.ezipaycoin.ui.composables.BottomNavigationBar
import com.app.ezipaycoin.ui.composables.DashboardTopBar
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.utils.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavHostController


    override fun onCreate(savedInstanceState: Bundle?) {
       // val splashScreen = installSplashScreen()
       // splashScreen.setKeepOnScreenCondition { viewModel.isLoading.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiService = ApiClient.retrofit.create(ApiService::class.java)
        val walletSharedViewModel = ViewModelProvider(
            this,
            ViewModelFactory {
                val repository = HomeRepoImpl(apiService)
                WalletSharedViewModel(repository)
            }
        )[WalletSharedViewModel::class.java]


        val bottomNavItems = App.getInstance().items
        val navigationDrawerItems = bottomNavItems + App.getInstance().navigationItems

        setContent {
            EzipayCoinTheme {
                val state by viewModel.uiState.collectAsState()

                navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route?.substringAfterLast('.')
                val showBottomBar =
                    if (currentRoute == null) true else currentRoute in state.bottomBarRoutes
                val showTopBar =
                    if (currentRoute == null) true else currentRoute in state.topBarRoutes

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var selectedItem by remember { mutableStateOf("Home") }
                if (currentRoute != null) {
                    selectedItem = currentRoute
                }
                if (!state.dataLoaded) {
                    // Show splash or loading screen instead
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
//                        Image(
//                            painter = painterResource(id = R.drawable.splash_background_image),
//                            contentDescription = "Ezipay Splash Screen Graphic", // Descriptive text for accessibility
//                            modifier = Modifier.fillMaxSize(), // Make the image fill the Box
//                            contentScale = ContentScale.Fit
//                        )
                    }
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            if (state.isLoggedIn) {
                                AppDrawerContent(
                                    selectedItem = selectedItem,
                                    onItemSelected = {
                                        selectedItem = it
                                        scope.launch { drawerState.close() }
                                        val route: Any = when (it) {
                                            "Home" -> Screen.BottomNavScreens.Home
                                            "Wallet" -> Screen.BottomNavScreens.Wallet
                                            "Earn" -> Screen.BottomNavScreens.Earn
                                            "Learn" -> Screen.BottomNavScreens.Learn
                                            "Pay" -> Screen.AppNavScreens.Pay
                                            "Profile" -> Screen.AppNavScreens.MyProfile
                                            else -> {
                                                Screen.BottomNavScreens.Home
                                            }
                                        }
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        viewModel.onEvent(MainEvent.BottomBarClicked(it))
                                    },
                                    onSignOut = {
                                        // Handle sign out
                                        scope.launch { drawerState.close() }
                                    },
                                    navigationDrawerItems
                                )
                            }

                        }
                    ) {
                        Scaffold(
                            containerColor = AppBackgroundColor,
                            contentWindowInsets = WindowInsets.safeDrawing,
                            topBar = {
                                if (showTopBar) {
                                    DashboardTopBar(
                                        showBottomBar,
                                        currentRoute,
                                        onAccountClicked = {
                                            navController.navigate(Screen.AppNavScreens.MyProfile)
                                        },
                                        onMenuClicked = {
                                            scope.launch { drawerState.open() }
                                        },
                                        onBackClicked = {
                                            onBackPressedDispatcher.onBackPressed()
                                        })
                                }
                            },
                            bottomBar = {
                                if (showBottomBar) {
                                    BottomNavigationBar(
                                        selectedItem,
                                        onItemSelected = {
                                            selectedItem = it
                                            val route: Any = when (it) {
                                                "Home" -> Screen.BottomNavScreens.Home
                                                "Wallet" -> Screen.BottomNavScreens.Wallet
                                                "Earn" -> Screen.BottomNavScreens.Earn
                                                "Learn" -> Screen.BottomNavScreens.Learn
                                                "Pay" -> Screen.AppNavScreens.Pay
                                                else -> {
                                                    Screen.BottomNavScreens.Home
                                                }
                                            }
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }

                                        }, bottomNavItems
                                    )
                                }
                            },
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .padding(paddingValues)
                            ) {
                                // Root NavHost

                                Navigation(state.isLoggedIn, navHostController = navController, apiService, walletSharedViewModel)

                            }
                        }
                    }
                }


            }
        }
    }
}


