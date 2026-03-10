package com.hanadulset.pro_poseapp.data.mapper

import com.hanadulset.pro_poseapp.data.model.UserDto
import com.hanadulset.pro_poseapp.domain.model.UserEntity

fun UserDto.toDomain() = UserEntity(
    isCompOn = isCompOn,
    isPoseOn = isPoseOn,
    poseCnt = poseCnt
)

fun UserEntity.toDto() = UserDto(
    isCompOn = isCompOn,
    isPoseOn = isPoseOn,
    poseCnt = poseCnt
)
