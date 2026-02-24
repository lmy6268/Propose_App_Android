package com.hanadulset.pro_poseapp.ui.mapper

import android.net.Uri
import com.hanadulset.pro_poseapp.domain.model.camera.ImageResultModel
import androidx.core.net.toUri

fun Uri.toDomainImageResult(date: String? = null): ImageResultModel =
        ImageResultModel(dataProposeUriString = this.toString(), takenDate = date)

fun ImageResultModel.toUIUri(): Uri? = this.dataProposeUriString?.toUri()
