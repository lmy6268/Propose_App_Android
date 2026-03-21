package com.hanadulset.pro_poseapp.domain.model


data class PoseEntity(
    val poseId: Int = 0,
    val poseCat: Int = 0,
    val bottomCenterRate: SizeFloat = SizeFloat(0F, 0F),
    val sizeRate: SizeFloat = SizeFloat(0F, 0F),
    val imageUri: String? = null,
    val imageScale: Float = 1F,
)

data class PoseResultEntity(
    val poseDataList: List<PoseEntity>,
    val backgroundAngleList: List<Double>,
    val backgroundId: Int,
)
