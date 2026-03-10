package com.hanadulset.pro_poseapp.domain.repository

import com.hanadulset.pro_poseapp.domain.model.PoseResultEntity
import kotlinx.coroutines.flow.Flow

interface AIRepository {
    // 모델 및 데이터 초기화
    suspend fun preRunModel()

    // 포즈 분석 수행 (내부에서 최신 프레임 획득)
    suspend fun requestPoseAnalysis(): PoseResultEntity

    // 구도 분석 및 추적 수행 (내부에서 초기 프레임 및 지속 프레임 획득)
    suspend fun analyzeComposition(): Flow<Pair<Float, Float>?>
}
