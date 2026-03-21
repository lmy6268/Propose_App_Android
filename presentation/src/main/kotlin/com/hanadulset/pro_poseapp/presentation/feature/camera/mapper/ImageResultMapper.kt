package com.hanadulset.pro_poseapp.presentation.mapper

import android.net.Uri
import com.hanadulset.pro_poseapp.domain.model.ImageResultEntity
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.ImageResultUIItem

fun ImageResultEntity.toUI() = ImageResultUIItem(
    dataUri = this.dataUri?.let { Uri.parse(it) },
    takenDate = this.takenDate
)

fun ImageResultUIItem.toDomain() = ImageResultEntity(
    dataUri = this.dataUri?.toString(),
    takenDate = this.takenDate
)
