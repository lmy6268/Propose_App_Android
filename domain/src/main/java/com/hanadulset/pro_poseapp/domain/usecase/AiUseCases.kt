package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.usecase.ai.GetPoseFromImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.PreLoadModelUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendCompInfoUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendPoseUseCase
import javax.inject.Inject

data class AiUseCases @Inject constructor(
    val recommendPoseUseCase: RecommendPoseUseCase,
    val recommendCompInfoUseCase: RecommendCompInfoUseCase,
    val preLoadModelUseCase: PreLoadModelUseCase,
    val getPoseFromImageUseCase: GetPoseFromImageUseCase
)
