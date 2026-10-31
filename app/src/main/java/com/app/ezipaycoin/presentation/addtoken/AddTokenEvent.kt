package com.app.ezipaycoin.presentation.addtoken

sealed class AddTokenEvent {
    data class ContractAddressChanged(val value: String) : AddTokenEvent()
    data class ChainIdChanged(val value: Int) : AddTokenEvent()
    data class SymbolChanged(val value: String) : AddTokenEvent()
    data class TokenNameChanged(val value: String) : AddTokenEvent()
    data class DecimalsChanged(val value: String) : AddTokenEvent()
    data object ValidateAddress : AddTokenEvent()
    data object AddToken : AddTokenEvent()
    data class DismissDialog(val which: Int) : AddTokenEvent()
}
