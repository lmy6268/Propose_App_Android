package com.hanadulset.pro_poseapp.domain.usecase.ai

import com.hanadulset.pro_poseapp.domain.repository.AIRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.domain.repository.useMat
import com.hanadulset.pro_poseapp.domain.model.PoseResultEntity
import javax.inject.Inject

class RecommendPoseUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val aiRepository: AIRepository
) {
    /**
     * 비즈니스 로직: 이미지를 Mat으로 변환한 후 AI 엔진에 포즈 추천을 요청하고 결과를 반환합니다.
     */
    suspend operator fun invoke(): PoseResultEntity {
        // AI 엔진에 포즈 분석 요청
        return aiRepository.requestPoseAnalysis()
    }
}