package com.hanadulset.pro_poseapp.presentation.feature.camera.model

import com.hanadulset.pro_poseapp.domain.model.UserEntity

data class UserUIItem(
    val isCompOn: Boolean = true,
    val isPoseOn: Boolean = true,
    val poseCnt: Int = 10
)

fun UserEntity.toUIItem() = UserUIItem(
    isCompOn = isCompOn,
    isPoseOn = isPoseOn,
    poseCnt = poseCnt
)

fun UserUIItem.toDomain() = UserEntity(
    isCompOn = isCompOn,
    isPoseOn = isPoseOn,
    poseCnt = poseCnt
)
