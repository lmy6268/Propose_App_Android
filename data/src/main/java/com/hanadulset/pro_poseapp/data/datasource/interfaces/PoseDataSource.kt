package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.data.model.PoseResultDto
import org.opencv.core.Mat

interface PoseDataSource {
    // 데이터 사전 로드
    suspend fun initPoseData()
    
    // 포즈 추천 요청 (일회성 결과 직접 반환)
    suspend fun recommendPose(mat: Mat): PoseResultDto
}
