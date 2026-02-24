package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import org.pytorch.Module

/**모델을 실행하는 데이터소스*/
interface ModelRunnerDataSource {
    //파일명을 이용하여, AI 모델을 로드한다.
    fun loadModel(moduleAssetName: String): Module

    //미리 실행해둠
    suspend fun preRun(): Boolean

    // 인터페이스도 suspend로 변경하고 반환 타입을 명확히 함
    suspend fun runVapNet(bitmap: Bitmap): Pair<Float, Float>
}
