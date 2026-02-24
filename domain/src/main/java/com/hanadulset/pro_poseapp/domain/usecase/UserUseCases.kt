package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.usecase.user.CheckUserSuccessToUseUseCase
import com.hanadulset.pro_poseapp.domain.usecase.user.LoadUserSetUseCase
import com.hanadulset.pro_poseapp.domain.usecase.user.SaveUserSetUseCase
import com.hanadulset.pro_poseapp.domain.usecase.user.SaveUserSuccessToUseUseCase
import javax.inject.Inject

data class UserUseCases
@Inject
constructor(
        val checkUserSuccessToUse: CheckUserSuccessToUseUseCase,
        val saveUserSuccessToUse: SaveUserSuccessToUseUseCase,
        val loadUserSet: LoadUserSetUseCase,
        val saveUserSet: SaveUserSetUseCase
)
