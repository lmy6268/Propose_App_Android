package com.hanadulset.pro_poseapp.data.mapper

import android.net.Uri
import com.hanadulset.pro_poseapp.domain.model.camera.ImageResultModel
import androidx.core.net.toUri

// 도메인의 ImageResult는 Uri 대신 String을 사용합니다.
// Data 레이어에서 Uri를 반환받았을 때 이를 도메인 모델로 변환해주는 매퍼

fun Uri.toDomainImageResult(date: String? = null): ImageResultModel =
        ImageResultModel(dataProposeUriString = this.toString(), takenDate = date)

fun ImageResultModel.toDataUri(): Uri? = this.dataProposeUriString?.toUri()
