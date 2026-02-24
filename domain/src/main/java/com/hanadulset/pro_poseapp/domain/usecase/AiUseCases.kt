package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.usecase.ai.*
import javax.inject.Inject

data class AiUseCases
@Inject
constructor(
        val recommendCompInfo: RecommendCompInfoUseCase,
        val recommendPose: RecommendPoseUseCase,
        val getPoseFromProposeImage: GetPoseFromImageUseCase,
        val preLoadModel: PreLoadModelUseCase
)
