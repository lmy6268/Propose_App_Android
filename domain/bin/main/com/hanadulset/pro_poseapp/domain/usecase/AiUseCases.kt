package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.repository.AIRepository
import com.hanadulset.pro_poseapp.domain.usecase.ai.GetPoseFromImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendCompInfoUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendPoseUseCase
import javax.inject.Inject

data class AiUseCases @Inject constructor(
    val recommendPoseUseCase: RecommendPoseUseCase,
    val recommendCompInfoUseCase: RecommendCompInfoUseCase,
    val getPoseFromImageUseCase: GetPoseFromImageUseCase,
    private val aiRepository: AIRepository
) {
    /** 모델 사전 로드 (PrepareServiceViewModel에서 호출) */
    suspend fun preLoadModel(): Boolean {
        return try {
            aiRepository.preRunModel()
            true
        } catch (e: Exception) {
            false
        }
    }
}
