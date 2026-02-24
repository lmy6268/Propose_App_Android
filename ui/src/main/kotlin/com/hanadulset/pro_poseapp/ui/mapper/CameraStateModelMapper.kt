package com.hanadulset.pro_poseapp.ui.mapper

import android.util.Size
import com.hanadulset.pro_poseapp.domain.model.camera.CameraResolutionModel

fun Size.toDomain(): CameraResolutionModel =
        CameraResolutionModel(width = this.width, height = this.height)

fun CameraResolutionModel.toUI(): Size = Size(this.width, this.height)
