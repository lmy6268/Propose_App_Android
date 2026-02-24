package com.hanadulset.pro_poseapp.data.datasource

import android.content.Context
import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ImageProcessDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ModelRunnerDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.Size
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils

class ModelRunnerDataSourceDataSourceImpl
@Inject
constructor(
        @param:ApplicationContext private val context: Context,
        private val imageProcessDataSource: ImageProcessDataSource
) : ModelRunnerDataSource {

    private var vapNetModule: Module? = null

    // path를 알면 로드할 수 있음.
    override fun loadModel(moduleAssetName: String): Module {
        val file = File(context.dataDir, moduleAssetName)
        if (!file.exists() || file.length() <= 0) {
            context.assets.open(moduleAssetName).use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
        }
        return LiteModuleLoader.load(file.absolutePath)
    }

    // 모델을 예열한다. (IO 스레드에서 수행)
    override suspend fun preRun(): Boolean =
            withContext(Dispatchers.IO) {
                runCatching {
                            vapNetModule = loadModel(Config.VAPNET_FILE)
                            true
                        }
                        .getOrDefault(false)
            }

    override suspend fun runVapNet(bitmap: Bitmap): Pair<Float, Float> =
            withContext(Dispatchers.IO) {
                val module = vapNetModule ?: return@withContext Pair(0F, 0F)

                val resizedBitmap =
                        imageProcessDataSource.resizeBitmapWithOpenCV(
                                bitmap,
                                Config.RESNET_INPUT_SIZE
                        )

                val inputTensor =
                        TensorImageUtils.bitmapToFloat32Tensor(
                                resizedBitmap,
                                Config.MEAN_ARRAY,
                                Config.STD_ARRAY
                        )

                val output = module.forward(IValue.from(inputTensor))
                val outputTuple = output.toTuple()

                // Clean up native resources
                resizedBitmap.recycle()

                val suggestion = outputTuple[0].toTensor().dataAsFloatArray
                val adjustment = outputTuple[1].toTensor().dataAsFloatArray
                val magnitude = outputTuple[2].toTensor().dataAsFloatArray

                var res = Pair(0F, 0F)

                if (suggestion[0] > Config.SUGGESTION_THRESHOLD) { // 조정이 필요한 경우
                    val magIndex = adjustment.indices.maxByOrNull { adjustment[it] } ?: 0
                    val magOutput = magnitude[magIndex]

                    if (adjustment.sum() in 0.99F..1.01F) { // 이전 모델 이용 시
                        res = processOldModelOutput(magIndex, magOutput)
                    } else { // 최근 모델 이용 시
                        res = processNewModelOutput(adjustment, magnitude)
                    }
                }

                res
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

    private fun processNewModelOutput(
            adjustment: FloatArray,
            magnitude: FloatArray
    ): Pair<Float, Float> {
        var horizontal = 0F
        var vertical = 0F

        val horizontalMoveIndex = if (adjustment[0] > adjustment[1]) 0 else 1
        val verticalMoveIndex = if (adjustment[2] > adjustment[3]) 2 else 3

        if (adjustment[horizontalMoveIndex] >= Config.LOGIT_THRESHOLD) {
            horizontal =
                    magnitude[horizontalMoveIndex] * (if (horizontalMoveIndex == 0) -1F else 1F)
        }
        if (adjustment[verticalMoveIndex] >= Config.LOGIT_THRESHOLD) {
            vertical = magnitude[verticalMoveIndex] * (if (verticalMoveIndex == 2) -1F else 1F)
        }

        return Pair(horizontal, vertical)
    }

    companion object {
        private object Config {
            val RESNET_INPUT_SIZE = Size(224.0, 224.0)
            const val VAPNET_FILE = "vapnet.ptl"

            val MEAN_ARRAY = floatArrayOf(0.485F, 0.456F, 0.406F)
            val STD_ARRAY = floatArrayOf(0.229F, 0.224F, 0.225F)

            const val SUGGESTION_THRESHOLD = 0.9
            const val ADJUSTMENT_THRESHOLD = 0.5

            // 시그모이드 임계값 계산 최적화:
            // 모델의 출력값(logit)을 매번 sigmoid 함수를 통과시켜 확률로 변환(1/(1+exp(-x)))하는 것은 CPU 연산량이 많습니다.
            // 대신 임계값을 역으로 계산하여 출력값(logit)과 직접 비교할 수 있는 'Logit 임계값'을 미리 계산해 둡니다.
            val LOGIT_THRESHOLD =
                    if (ADJUSTMENT_THRESHOLD == 0.5) 0F
                    else -kotlin.math.ln(1 / ADJUSTMENT_THRESHOLD.toDouble() - 1).toFloat()
        }
    }
}
