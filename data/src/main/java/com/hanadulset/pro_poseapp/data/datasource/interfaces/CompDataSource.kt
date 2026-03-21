package com.hanadulset.pro_poseapp.data.datasource.interfaces

import org.opencv.core.Mat

interface CompDataSource {
    // 모델 사전 로드
    suspend fun loadModel()
    
    // 구도 분석 요청
    suspend fun recommendCompData(mat: Mat): Pair<Float, Float>
}
