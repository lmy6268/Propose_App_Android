package com.hanadulset.pro_poseapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSetModel(
        val isCompOn: Boolean = true,
        val isPoseOn: Boolean = true,
        val poseCnt: Int = 10
)
