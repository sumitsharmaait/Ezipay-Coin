package com.app.ezipaycoin.presentation.main

sealed class MainEvent {
    data class BottomBarClicked(val bottomBar : String) : MainEvent()
}