package com.hanadulset.pro_poseapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CaptureEventDto(
    val poseID: Int, //포즈
    val prevRecommendPoses: List<Int>?, //이전에 선택한 추천 포즈 데이터
    val timestamp: String, //이벤트 발생 시기
    val backgroundId: Int?,//추천 배경
    val backgroundHog: List<Double>? //배경 특징량
)
