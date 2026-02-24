package com.hanadulset.pro_poseapp.domain.model.pose

// 포즈 데이터 클래스 - Android 의존성(.net.ProposeUri, .util.ProposeSize) 제거 버젼
data class PoseDataModel(
        val poseId: Int = 0,
        val poseCat: Int = 0,
        val bottomCenterRate: PointFModel = PointFModel(0f, 0f), // 중심점 비율
        val sizeRate: PointFModel = PointFModel(0f, 0f),
        val imageProposeUriString: String? = null, // ProposeUri 대신 String 경로 사용
        val imageScale: Float = 1f,
)
