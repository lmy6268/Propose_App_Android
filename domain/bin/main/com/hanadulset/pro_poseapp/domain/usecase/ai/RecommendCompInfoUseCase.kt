package com.hanadulset.pro_poseapp.domain.usecase.ai

import com.hanadulset.pro_poseapp.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecommendCompInfoUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    /**
     * 비즈니스 로직: 이미지 변환 후 AI 엔진에 구도 분석을 요청하고, 분석 결과를 포함한 실시간 스트림(Flow)을 반환합니다.
     */
    suspend operator fun invoke(): Flow<Pair<Float, Float>?> {
        // AI 엔진에 구도 분석 및 트래킹 요청
        return aiRepository.analyzeComposition()
    }
}
