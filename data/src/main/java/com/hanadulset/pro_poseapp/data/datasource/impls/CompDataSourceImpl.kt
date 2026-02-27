package com.hanadulset.pro_poseapp.data.datasource.impls

import android.content.Context
import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.data.datasource.interfaces.CompDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.VisionProcessDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.Size
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.exp

class CompDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val visionDS: VisionProcessDataSource
) : CompDataSource {

    private var module: Module? = null

    override suspend fun prepareModel() = withContext(Dispatchers.IO) {
        val file = File(context.dataDir, "vapnet.ptl")
        if (!file.exists()) {
            context.assets.open("vapnet.ptl").use { input ->
                FileOutputStream(file).use { input.copyTo(it) }
            }
        }
        module = LiteModuleLoader.load(file.absolutePath)
    }

    override suspend fun recommendCompData(backgroundBitmap: Bitmap): Pair<Float, Float> = withContext(Dispatchers.IO) {
        val model = module ?: return@withContext Pair(0f, 0f)
        val resized = visionDS.resizeBitmapWithOpenCV(backgroundBitmap, Size(224.0, 224.0))
        val tensor = TensorImageUtils.bitmapToFloat32Tensor(
            resized, 
            floatArrayOf(0.485f, 0.456f, 0.406f), 
            floatArrayOf(0.229f, 0.224f, 0.225f)
        )
        
        val output = model.forward(IValue.from(tensor)).toTuple()
        val adjustment = output[1].toTensor().dataAsFloatArray
        val magnitude = output[2].toTensor().dataAsFloatArray

        val hIdx = if (adjustment[0] > adjustment[1]) 0 else 1
        val vIdx = if (adjustment[2] > adjustment[3]) 2 else 3
        
        Pair(
            if (exp(adjustment[hIdx].toDouble()) > 0.5) magnitude[hIdx] * (if (hIdx == 0) -1f else 1f) else 0f,
            if (exp(adjustment[vIdx].toDouble()) > 0.5) magnitude[vIdx] * (if (vIdx == 2) -1f else 1f) else 0f
        )
    }
}
