package com.hanadulset.pro_poseapp.data.mapper

import com.hanadulset.pro_poseapp.data.model.ImageResultDto
import com.hanadulset.pro_poseapp.domain.model.ImageResultEntity
import androidx.core.net.toUri

fun ImageResultDto.toDomain() = ImageResultEntity(
    dataUri = this.dataUri?.toString(),
    takenDate = this.takenDate
)

fun ImageResultEntity.toDto() = ImageResultDto(
    dataUri = this.dataUri?.toUri(),
    takenDate = this.takenDate
)
