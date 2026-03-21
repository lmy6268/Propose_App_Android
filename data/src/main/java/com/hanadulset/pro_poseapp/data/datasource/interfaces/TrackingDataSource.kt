package com.hanadulset.pro_poseapp.data.datasource.interfaces

import kotlinx.coroutines.flow.Flow
import org.opencv.core.Mat

interface TrackingDataSource {
    /**
     * 트래킹을 시작하고 결과를 Flow로 반환합니다.
     * Flow가 수집되는 동안 [frameProvider]를 통해 매 프레임을 Pull 하여 트래킹을 수행하고,
     * Flow 종료 시 내부 리소스를 자동 해제합니다.
     *
     * @param initialMat 초기 이미지 (내부에서 필요한 데이터를 복사함, 호출 측에서 해제 가능)
     * @param initialOffset 구도 분석으로 얻은 초기 정규화 오프셋
     * @param frameProvider 매 프레임을 제공하는 suspend 함수 (반환된 Mat은 이 클래스가 해제)
     */
    fun setTracker(
        initialMat: Mat,
        initialOffset: Pair<Float, Float>,
        frameProvider: suspend () -> Mat?
    ): Flow<Pair<Float, Float>?>
}
