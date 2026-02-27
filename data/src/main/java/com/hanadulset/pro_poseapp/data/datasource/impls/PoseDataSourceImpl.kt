package com.hanadulset.pro_poseapp.data.datasource.impls

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.SizeF
import androidx.core.net.toUri
import com.hanadulset.pro_poseapp.data.datasource.interfaces.PoseDataSource
import com.hanadulset.pro_poseapp.utils.ImageUtils
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult
import com.opencsv.CSVReaderBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.math.*

class PoseDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PoseDataSource {

    private var centroid: MutableList<List<Double>> = mutableListOf()
    private var poseRanks: List<List<PoseData>> = emptyList()

    override suspend fun preparePoseData() = withContext(Dispatchers.IO) {
        poseRanks = initPoseRankList()
        centroid = initCentroidValue()
    }

    override suspend fun recommendPose(backgroundBitmap: Bitmap): PoseDataResult = withContext(Dispatchers.Default) {
        val histogramMap = getHistogramMap(backgroundBitmap)
        val angle = getAngleFromHog(histogramMap)
        val backgroundId = (0 until centroid.size - 2).minByOrNull { getDistance(angle, it) } ?: -1

        histogramMap.release()

        PoseDataResult(
            poseDataList = poseRanks.getOrElse(backgroundId) { emptyList() }.toMutableList(),
            backgroundId = backgroundId,
            backgroundAngleList = angle
        )
    }

    private suspend fun getHistogramMap(bitmap: Bitmap): Mat = withContext(Dispatchers.Default) {
        val resizedMat = Mat()
        Utils.bitmapToMat(bitmap, resizedMat)
        Imgproc.cvtColor(resizedMat, resizedMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.resize(resizedMat, resizedMat, Size(128.0, 128.0))
        Imgproc.medianBlur(resizedMat, resizedMat, 17)

        val resizedImageMats = arrayListOf(
            Mat.zeros(resizedMat.width(), resizedMat.height(), CvType.CV_8UC1),
            Mat.zeros(resizedMat.width(), resizedMat.height(), CvType.CV_8UC1),
            Mat.zeros(resizedMat.width(), resizedMat.height(), CvType.CV_8UC1)
        )
        Core.split(resizedMat, resizedImageMats)

        val resMagnitude = Mat.zeros(resizedMat.width(), resizedMat.height(), CvType.CV_64FC1)
        val resOrientation = Mat.zeros(resizedMat.width(), resizedMat.height(), CvType.CV_64FC1)
        val cntMat = Mat.zeros(resizedMat.width(), resizedMat.height(), CvType.CV_8UC1)

        for (mat in resizedImageMats) {
            val gX = Mat(); val gY = Mat()
            Imgproc.Sobel(mat, gX, CvType.CV_64F, 1, 0, 3)
            Imgproc.Sobel(mat, gY, CvType.CV_64F, 0, 1, 3)
            val mag = Mat(); val ori = Mat()
            Core.cartToPolar(gX, gY, mag, ori, true)

            for (r in 0 until mag.rows()) {
                for (c in 0 until mag.cols()) {
                    if (mag.get(r, c)[0] != 0.0) {
                        resMagnitude.put(r, c, resMagnitude.get(r, c)[0] + mag.get(r, c)[0])
                        resOrientation.put(r, c, resOrientation.get(r, c)[0] + (ori.get(r, c)[0] % 180.0))
                        cntMat.put(r, c, cntMat.get(r, c)[0] + 1)
                    }
                }
            }
            gX.release(); gY.release(); mag.release(); ori.release()
        }

        val totalSum = Core.sumElems(resMagnitude).`val`[0]
        val ave = totalSum / (resMagnitude.width() * resMagnitude.height())
        for (r in 0 until resMagnitude.rows()) {
            for (c in 0 until resMagnitude.cols()) {
                val currentCnt = cntMat.get(r, c)[0]
                if (currentCnt > 0) {
                    resMagnitude.put(r, c, resMagnitude.get(r, c)[0] / currentCnt)
                    resOrientation.put(r, c, resOrientation.get(r, c)[0] / currentCnt)
                }
                val finalMag = if (ave * 1.5 > resMagnitude.get(r, c)[0]) 0.0 else 1.0
                resMagnitude.put(r, c, finalMag)
            }
        }

        val histogramMap = Mat.zeros(Size(8.0, 8.0), CvType.CV_64FC(12))
        for (x in 0 until 128 step 16) {
            for (y in 0 until 128 step 16) {
                val hist = DoubleArray(12)
                for (cx in 0 until 16) {
                    for (cy in 0 until 16) {
                        val magVal = resMagnitude.get(x + cx, y + cy)[0]
                        if (magVal > 0) {
                            val oriVal = resOrientation.get(x + cx, y + cy)[0]
                            val bin = (oriVal / 15.0).toInt().coerceAtMost(11)
                            hist[bin] += magVal
                        }
                    }
                }
                val maxIdx = hist.indices.maxByOrNull { hist[it] } ?: 0
                val finalHist = DoubleArray(12) { if (it == maxIdx && hist[it] > 0) 1.0 else 0.0 }
                histogramMap.put(x / 16, y / 16, *finalHist)
            }
        }

        resizedMat.release(); resMagnitude.release(); resOrientation.release(); cntMat.release()
        resizedImageMats.forEach { it.release() }
        histogramMap
    }

    private fun getAngleFromHog(histogramMap: Mat): List<Double> {
        val angleMap = MutableList(64) { -1.0 }
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val data = histogramMap.get(r, c)
                for (i in data.indices) {
                    if (data[i] == 1.0) {
                        angleMap[r * 8 + c] = i * 15.0
                        break
                    }
                }
            }
        }
        return angleMap
    }

    private fun getDistance(angle: List<Double>, centroidIdx: Int): Double {
        val targetCentroid = centroid.getOrNull(centroidIdx) ?: return Double.MAX_VALUE
        var distance = 0.0
        for (idx in angle.indices) {
            distance += if ((angle[idx] == -1.0 && targetCentroid[idx] != -1.0) || 
                           (angle[idx] != -1.0 && targetCentroid[idx] == -1.0)) 1.0
            else 1 - abs(cos(Math.toRadians(angle[idx] - targetCentroid[idx])))
        }
        return 50.0 * distance
    }

    private fun initCentroidValue(): MutableList<List<Double>> {
        val result = mutableListOf<List<Double>>()
        try {
            context.assets.open("centroids.csv").use { stream ->
                val reader = CSVReaderBuilder(InputStreamReader(stream)).build()
                reader.forEach { row ->
                    if (row.isNotEmpty() && !row[0].contains("label")) {
                        val data = row.toMutableList().apply { 
                            this[1] = this[1].removePrefix("[")
                            this[lastIndex] = this[lastIndex].removeSuffix("]")
                        }.drop(1).map { it.trim().toDouble() }
                        result.add(data)
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return result
    }

    private fun initPoseRankList(): List<List<PoseData>> {
        val rankList = mutableListOf<List<Double>>()
        try {
            context.assets.open("pose_ranks.csv").use { stream ->
                val reader = CSVReaderBuilder(InputStreamReader(stream)).build()
                reader.forEach { row ->
                    if (row.size > 1 && row[1] != "pose_ids") {
                        val ids = row[1].removePrefix("[").removeSuffix("]").split(",").map { it.trim().toDouble() }
                        rankList.add(ids)
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        val allPoses = loadPoseData()
        return rankList.mapIndexed { idx, ids ->
            ids.map { id -> allPoses.getOrElse(id.toInt()) { PoseData(id.toInt(), -1) }.copy(poseCat = idx) }
        }
    }

    private fun loadPoseData(): List<PoseData> {
        val result = mutableListOf<PoseData>()
        val images = loadPoseImages()
        try {
            context.assets.open("image_datas.csv").use { stream ->
                val reader = CSVReaderBuilder(InputStreamReader(stream)).build()
                val rows = reader.readAll()
                rows.drop(1).forEach { row ->
                    val id = row[0].toInt()
                    val center = row[1].removePrefix("[").removeSuffix("]").split(",").map { it.trim().toFloat() }
                    val size = row[2].removePrefix("[").removeSuffix("]").split(",").map { it.trim().toFloat() }
                    result.add(PoseData(poseId = id, bottomCenterRate = SizeF(center[0], center[1]), sizeRate = SizeF(size[0], size[1])))
                }
            }
            result.sortBy { it.poseId }
            result.forEachIndexed { i, pose -> if (i < images.size) result[i] = pose.copy(imageUri = images[i]) }
        } catch (e: Exception) { e.printStackTrace() }
        return result
    }

    private fun loadPoseImages(): List<Uri> {
        val dir = File(context.dataDir, "silhouettes")
        val zipFile = File(context.dataDir, "silhouette_image.zip")
        try {
            context.assets.open("silhouette_image.zip").use { input ->
                FileOutputStream(zipFile).use { input.copyTo(it) }
            }
            if (!dir.exists()) dir.mkdir()
            ImageUtils.unZip(zipFile, dir.absolutePath)
        } catch (e: Exception) { e.printStackTrace() }

        return dir.listFiles()?.mapNotNull { file ->
            val name = file.name.removeSuffix(".png").toIntOrNull()
            if (name != null) name to file.toUri() else null
        }?.sortedBy { it.first }?.map { it.second } ?: emptyList()
    }
}
