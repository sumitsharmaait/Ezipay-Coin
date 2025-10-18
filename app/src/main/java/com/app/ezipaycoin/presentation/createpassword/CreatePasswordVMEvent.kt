package com.app.ezipaycoin.presentation.createpassword

sealed class CreatePasswordVMEvent {
    data object MoveToNext : CreatePasswordVMEvent()
    data object MoveToSuccess : CreatePasswordVMEvent()
}