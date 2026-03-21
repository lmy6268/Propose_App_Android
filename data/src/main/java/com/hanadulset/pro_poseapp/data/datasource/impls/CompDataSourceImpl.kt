package com.hanadulset.pro_poseapp.data.datasource.impls

import android.content.Context
import android.util.Log
import com.hanadulset.pro_poseapp.data.datasource.interfaces.CompDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.exp
import androidx.core.graphics.createBitmap

class CompDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CompDataSource {

    companion object {
        private const val COMP_MODEL_NAME = "vapnet.ptl"
        private const val TAG = "CompDataSource"
    }

    private var compModule: Module? = null

    override suspend fun loadModel() = withContext(Dispatchers.IO) {
        val file = File(context.dataDir, COMP_MODEL_NAME)
        if (!file.exists()) {
            context.assets.open(COMP_MODEL_NAME).use { input ->
                FileOutputStream(file).use { input.copyTo(it) }
            }
        }
        Log.d(TAG, "Loading model: $COMP_MODEL_NAME")
        compModule = LiteModuleLoader.load(file.absolutePath)
    }.also { Log.d(TAG, "Model loaded successfully") }

    override suspend fun recommendCompData(mat: Mat): Pair<Float, Float> =
        withContext(Dispatchers.Default) {
            val model = compModule
            if (model == null) {
                Log.e("CompDataSource", "Model is not loaded!")
                return@withContext Pair(0f, 0f)
            }

            // 1. Mat -> Resize (224x224) -> Bitmap (Tensor용)
            val resizedMat = Mat()
            Imgproc.resize(mat, resizedMat, Size(224.0, 224.0), 0.0, 0.0, Imgproc.INTER_AREA)
            val bitmap = createBitmap(224, 224)
            Utils.matToBitmap(resizedMat, bitmap)
            resizedMat.release()

            // 2. Tensor 변환 및 추론
            val tensor = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap, floatArrayOf(0.485f, 0.456f, 0.406f), floatArrayOf(0.229f, 0.224f, 0.225f)
            )

            val output = model.forward(IValue.from(tensor)).toTuple()
            val adjustment = output[1].toTensor().dataAsFloatArray
            val magnitude = output[2].toTensor().dataAsFloatArray

            val hIdx = if (adjustment[0] > adjustment[1]) 0 else 1
            val vIdx = if (adjustment[2] > adjustment[3]) 2 else 3

            val res = Pair(
                if (exp(adjustment[hIdx].toDouble()) > 0.5) magnitude[hIdx] * (if (hIdx == 0) -1f else 1f) else 0f,
                if (exp(adjustment[vIdx].toDouble()) > 0.5) magnitude[vIdx] * (1f) else 0f
            )
            Log.d("CompDataSource", "Inference result: $res")
            res
        }
}
