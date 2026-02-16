package com.hanadulset.pro_poseapp.data.datasource

import android.content.Context
import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ModelRunnerDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.Size
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.math.absoluteValue
import kotlin.math.exp

class ModelRunnerDataSourceDataSourceImpl(private val context: Context) : ModelRunnerDataSource {

    private var vapNetModule: Module? = null

    private val imageProcessDataSource by lazy {
        ImageProcessDataSourceImpl()
    }

    // path를 알면 로드할 수 있음.
    override fun loadModel(moduleAssetName: String): Module {
        val file = File(context.dataDir, moduleAssetName)
        if (!file.exists() || file.length() <= 0) {
            context.assets.open(moduleAssetName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return LiteModuleLoader.load(file.absolutePath)
    }

    // 모델을 예열한다. (IO 스레드에서 수행)
    override suspend fun preRun(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            vapNetModule = loadModel(VAPNET_FILE)
            true
        }.getOrDefault(false)
    }

    override fun runVapNet(bitmap: Bitmap): Pair<Float, Float> {
        val module = vapNetModule ?: return Pair(0F, 0F)

        val resizedBitmap = imageProcessDataSource.resizeBitmapWithOpenCV(bitmap, RESNET_INPUT_SIZE)

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap, MEAN_ARRAY, STD_ARRAY
        )

        val output = module.forward(IValue.from(inputTensor))
        val outputTuple = output.toTuple()

        val suggestion = outputTuple[0].toTensor().dataAsFloatArray
        val adjustment = outputTuple[1].toTensor().dataAsFloatArray
        val magnitude = outputTuple[2].toTensor().dataAsFloatArray

        var res = Pair(0F, 0F)

        if (suggestion[0] > SUGGESTION_THRESHOLD) { // 조정이 필요한 경우
            val magIndex = adjustment.toList().indexOf(adjustment.maxOrNull() ?: 0f)
            val magOutput = magnitude[magIndex]

            if (adjustment.sum() in 0.99F..1.01F) {   // 이전 모델 이용 시
                res = processOldModelOutput(magIndex, magOutput)
            } else { // 최근 모델 이용 시
                res = processNewModelOutput(adjustment, magnitude)
            }
        }

        return res
    }

    private fun processOldModelOutput(magIndex: Int, magOutput: Float): Pair<Float, Float> {
        return when (magIndex) {
            in 0..1 -> {
                val horizontalMoveRate = magOutput.absoluteValue
                Pair(if (magIndex == 0) -horizontalMoveRate else horizontalMoveRate, 0F)
            }
            else -> {
                val verticalMoveRate = magOutput.absoluteValue
                Pair(0F, if (magIndex == 2) -verticalMoveRate else verticalMoveRate)
            }
        }
    }

    private fun processNewModelOutput(adjustment: FloatArray, magnitude: FloatArray): Pair<Float, Float> {
        var horizontal = 0F
        var vertical = 0F

        val modifiedAdjustment = adjustment.map {
            (1 / (1 + exp((-it).toDouble()))).toFloat()
        }

        val horizontalMoveIndex = if (modifiedAdjustment[0] > modifiedAdjustment[1]) 0 else 1
        val verticalMoveIndex = if (modifiedAdjustment[2] > modifiedAdjustment[3]) 2 else 3

        if (modifiedAdjustment[horizontalMoveIndex] >= ADJUSTMENT_THRESHOLD) {
            horizontal = magnitude[horizontalMoveIndex] * (if (horizontalMoveIndex == 0) -1F else 1F)
        }
        if (modifiedAdjustment[verticalMoveIndex] >= ADJUSTMENT_THRESHOLD) {
            vertical = magnitude[verticalMoveIndex] * (if (verticalMoveIndex == 2) -1F else 1F)
        }

        return Pair(horizontal, vertical)
    }

    companion object {
        private val RESNET_INPUT_SIZE = Size(224.0, 224.0)
        private const val VAPNET_FILE = "vapnet.ptl"

        private val MEAN_ARRAY = floatArrayOf(0.485F, 0.456F, 0.406F)
        private val STD_ARRAY = floatArrayOf(0.229F, 0.224F, 0.225F)

        private const val SUGGESTION_THRESHOLD = 0.9
        private const val ADJUSTMENT_THRESHOLD = 0.5
    }
}
