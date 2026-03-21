package com.hanadulset.pro_poseapp.presentation

import androidx.lifecycle.ViewModel
import com.hanadulset.pro_poseapp.domain.usecase.AnalyticsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val analyticsUseCases: AnalyticsUseCases
) : ViewModel() {

    fun onAppOpen() {
        analyticsUseCases.saveAppOpenEvent()
    }

    fun onAppClosed() {
        analyticsUseCases.saveAppClosedEvent()
    }
}
