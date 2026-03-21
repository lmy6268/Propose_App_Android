package com.hanadulset.pro_poseapp.data.mapper

import com.hanadulset.pro_poseapp.data.model.PoseDto
import com.hanadulset.pro_poseapp.data.model.PoseResultDto
import com.hanadulset.pro_poseapp.domain.model.PoseEntity
import com.hanadulset.pro_poseapp.domain.model.PoseResultEntity
import com.hanadulset.pro_poseapp.domain.model.SizeFloat

fun PoseDto.toDomain() = PoseEntity(
    poseId = poseId,
    poseCat = poseCat,
    bottomCenterRate = SizeFloat(bottomCenterRate.width, bottomCenterRate.height),
    sizeRate = SizeFloat(sizeRate.width, sizeRate.height),
    imageUri = imageUri,
    imageScale = imageScale
)

fun PoseResultDto.toDomain() = PoseResultEntity(
    poseDataList = poseDataList.map { it.toDomain() },
    backgroundAngleList = backgroundAngleList,
    backgroundId = backgroundId
)
