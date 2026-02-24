package com.hanadulset.pro_poseapp.ui.mapper

import android.graphics.PointF as AndroidPointF
import com.hanadulset.pro_poseapp.domain.model.pose.PointFModel

fun AndroidPointF.toDomain(): PointFModel = PointFModel(x = this.x, y = this.y)

fun PointFModel.toUI(): AndroidPointF = AndroidPointF(this.x, this.y)
