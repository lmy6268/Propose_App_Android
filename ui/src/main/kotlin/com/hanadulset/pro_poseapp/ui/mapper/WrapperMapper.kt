package com.hanadulset.pro_poseapp.ui.mapper

import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeImage
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeSize
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeUri

// Bitmap Wrapper
data class AndroidProposeImage(val bitmap: Bitmap) : ProposeImage

fun Bitmap.toDomainWrapper(): ProposeImage = AndroidProposeImage(this)

fun ProposeImage.toBitmap(): Bitmap = (this as AndroidProposeImage).bitmap

// Uri Wrapper
data class AndroidProposeUri(val uri: Uri) : ProposeUri

fun Uri.toDomainWrapper(): ProposeUri = AndroidProposeUri(this)

fun ProposeUri.toUri(): Uri = (this as AndroidProposeUri).uri

// Size Wrapper
data class AndroidProposeSize(override val width: Float, override val height: Float) : ProposeSize

fun Size.toDomainWrapper(): ProposeSize =
        AndroidProposeSize(this.width.toFloat(), this.height.toFloat())

fun ProposeSize.toSize(): Size = Size(this.width.toInt(), this.height.toInt())
