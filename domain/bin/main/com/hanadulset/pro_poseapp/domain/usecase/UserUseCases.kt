package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.usecase.user.CheckUserSuccessToUseUseCase
import com.hanadulset.pro_poseapp.domain.usecase.user.LoadUserSetUseCase
import com.hanadulset.pro_poseapp.domain.usecase.user.SaveUserSetUseCase
import com.hanadulset.pro_poseapp.domain.usecase.user.SaveUserSuccessToUseUseCase
import javax.inject.Inject

data class UserUseCases @Inject constructor(
    val loadUserSetUseCase: LoadUserSetUseCase,
    val saveUserSetUseCase: SaveUserSetUseCase,
    val saveUserSuccessToUseUseCase: SaveUserSuccessToUseUseCase,
    val checkUserSuccessToUseUseCase: CheckUserSuccessToUseUseCase
)
