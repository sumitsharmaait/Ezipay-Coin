package com.app.ezipaycoin.presentation.dashboard.learn

sealed class LearnEvent {
    data object PayAmount : LearnEvent()
}