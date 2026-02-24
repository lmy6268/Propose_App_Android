package com.hanadulset.pro_poseapp.data.mapper

import android.util.SizeF
import com.hanadulset.pro_poseapp.domain.model.pose.PointFModel

fun SizeF.toDomain(): PointFModel = PointFModel(x = this.width, y = this.height)

fun PointFModel.toData(): SizeF = SizeF(this.x, this.y)
