package com.app.ezipaycoin.presentation.importfromseed

sealed class ImportFromSeedVMEvent {
    data object MoveToSuccess: ImportFromSeedVMEvent()
}