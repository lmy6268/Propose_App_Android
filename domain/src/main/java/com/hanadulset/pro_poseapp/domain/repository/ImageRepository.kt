package com.hanadulset.pro_poseapp.domain.repository

import com.hanadulset.pro_poseapp.domain.model.camera.ImageResultModel
import com.hanadulset.pro_poseapp.domain.model.pose.PoseDataResultModel
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeImage
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeSize
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeUri

//이미지 저장 및 분석을 담당하는 레포지토리
interface ImageRepository {
    suspend fun getRecommendCompInfo(backgroundImage: ProposeImage): Pair<Float, Float>
    suspend fun getRecommendPose(
        backgroundImage: ProposeImage
    ): PoseDataResultModel //추천된 포즈데이터 반환하기

    suspend fun getFixedScreen(backgroundImage: ProposeImage): ProposeImage // 고정 화면을 보여줌
    suspend fun getLatestProposeImage(): ProposeUri?
    suspend fun preRunModel(): Boolean
    suspend fun getPoseFromProposeImage(uri: ProposeUri?): ProposeImage?
    suspend fun loadAllCapturedProposeImages(): List<ImageResultModel>
    suspend fun deleteCapturedProposeImage(uri: ProposeUri): Boolean

    suspend fun updateOffsetPoint(backgroundImage: ProposeImage, targetOffset: ProposeSize): ProposeSize?
    fun stopPointOffset()

}