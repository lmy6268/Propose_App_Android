package com.hanadulset.pro_poseapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val isCompOn: Boolean = true,
    val isPoseOn: Boolean = true,
    val poseCnt: Int = 10
)
