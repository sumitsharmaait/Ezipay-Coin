package com.app.ezipaycoin.presentation.dashboard.learn

import com.app.ezipaycoin.data.remote.dto.response.LearnItemResponse
import com.app.ezipaycoin.presentation.dashboard.earn.Load

data class LearnState(
    val lessons: Load<List<LearnItemResponse>> = Load.Loading,
)
