package com.hanadulset.pro_poseapp.data.datasource.interfaces

import kotlinx.coroutines.flow.StateFlow
import org.opencv.core.Mat

interface TrackingDataSource {
    // 실시간 추적 결과 스트림
    val trackingFlow: StateFlow<Pair<Float, Float>?>
    
    // 추적 시작
    fun startTracking(initialMat: Mat, initialOffset: Pair<Float, Float>)
    
    // 추적 중지
    fun stopTracking()
    
    // 매 프레임 업데이트
    fun processFrame(currentMat: Mat)
}
