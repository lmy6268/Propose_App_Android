package com.hanadulset.pro_poseapp.presentation.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.AiUseCases
import com.hanadulset.pro_poseapp.domain.usecase.UserUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrepareServiceViewModel @Inject constructor(
    private val aiUseCases: AiUseCases,
    private val userUseCases: UserUseCases
) : ViewModel() {

    private val _totalLoadedState = MutableStateFlow(false)
    private val _modelLoadedState = MutableStateFlow(false)
    val totalLoadedState = _totalLoadedState.asStateFlow()


    private val _checkUserSuccess = MutableStateFlow<Boolean?>(null)
    val checkUserSuccess = _checkUserSuccess.asStateFlow()


    fun preLoadModel() {
        _modelLoadedState.value = false
        viewModelScope.launch {
            _modelLoadedState.value = aiUseCases.preLoadModelUseCase()
            checkLoadAllPreRunMethod()
        }

    }

    fun successToUse() {
        viewModelScope.launch {
            userUseCases.saveUserSuccessToUseUseCase()
        }
    }

    fun checkToUse() {
        viewModelScope.launch {
            _checkUserSuccess.value = userUseCases.checkUserSuccessToUseUseCase()
        }
    }


    private fun checkLoadAllPreRunMethod() {
        if (_modelLoadedState.value) _totalLoadedState.value = true
    }


}
