package com.hanadulset.pro_poseapp.domain.model

data class UserEntity(
    val isCompOn: Boolean = true,
    val isPoseOn: Boolean = true,
    val poseCnt: Int = 10
)
