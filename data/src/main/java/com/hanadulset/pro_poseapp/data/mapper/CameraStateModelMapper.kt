package com.hanadulset.pro_poseapp.data.mapper

import android.util.Size
import com.hanadulset.pro_poseapp.domain.model.camera.CameraResolutionModel

fun Size.toDomain(): CameraResolutionModel = CameraResolutionModel(width = this.width, height = this.height)

fun CameraResolutionModel.toData(): Size = Size(this.width, this.height)

// data 영역에 존재할 CameraStateDTO를 따로 두지 않더라도,
// 필요한 곳에서 매핑하거나, 여기서 확장 함수로 해결할 수 있습니다.
