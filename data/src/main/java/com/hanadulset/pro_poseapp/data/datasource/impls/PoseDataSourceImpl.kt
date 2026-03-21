package com.hanadulset.pro_poseapp.data.datasource.impls

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.SizeF
import com.hanadulset.pro_poseapp.data.datasource.interfaces.PoseDataSource
import com.hanadulset.pro_poseapp.data.model.PoseDto
import com.hanadulset.pro_poseapp.data.model.PoseResultDto
import com.opencsv.CSVReaderBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.cos

class PoseDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PoseDataSource {

    companion object {
        private const val CENTROID_CSV = "centroids.csv"
        private const val POSE_RANKS_CSV = "pose_ranks.csv"
        private const val IMAGE_DATA_CSV = "image_datas.csv"
        private const val SILHOUETTE_ZIP = "silhouette_image.zip"
        private const val SILHOUETTE_DIR = "silhouettes"
    }

    private var centroid: MutableList<List<Double>> = mutableListOf()
    private var poseRanks: List<List<PoseDto>> = emptyList()

    // 데이터 초기화: 센트로이드 및 포즈 랭크 로드
    override suspend fun initPoseData() = withContext(Dispatchers.IO) {
        centroid = loadCentroids()
        poseRanks = loadPoseRanks()
    }

    // 포즈 추천 수행
    override suspend fun recommendPose(mat: Mat): PoseResultDto = withContext(Dispatchers.Default) {
        val histogramMap = generateHog(mat) // HOG 특징량 추출
        val angle = getAngleFromHog(histogramMap) // 특징량으로부터 앵글 추출
        // 센트로이드와 비교하여 가장 가까운 배경(클러스터) 탐색
        val backgroundId = (0 until centroid.size - 2).minByOrNull { getDistance(angle, it) } ?: -1
        android.util.Log.d("PoseDataSource", "Calculated backgroundId: $backgroundId")
        histogramMap.release()

        val res = PoseResultDto(
            poseDataList = poseRanks.getOrElse(backgroundId) { emptyList() }.toMutableList(),
            backgroundId = backgroundId,
            backgroundAngleList = angle
        )
        android.util.Log.d("PoseDataSource", "Recommendation result: ${res.poseDataList.size} poses found")
        res
    }

    // HOG 특징량 생성 (이미지 전처리 및 그라디언트 계산)
    private fun generateHog(mat: Mat): Mat {
        val resized = Mat()
        Imgproc.resize(mat, resized, Size(128.0, 128.0), 0.0, 0.0, Imgproc.INTER_AREA)
        Imgproc.medianBlur(resized, resized, 17)
        
        val channels = mutableListOf<Mat>()
        Core.split(resized, channels)
        val mag = Mat.zeros(128, 128, CvType.CV_64FC1)
        val ori = Mat.zeros(128, 128, CvType.CV_64FC1)
        
        for (c in channels) {
            val gx = Mat(); val gy = Mat()
            Imgproc.Sobel(c, gx, CvType.CV_64F, 1, 0, 3)
            Imgproc.Sobel(c, gy, CvType.CV_64F, 0, 1, 3)
            val m = Mat(); val o = Mat()
            Core.cartToPolar(gx, gy, m, o, true)
            Core.add(mag, m, mag); Core.add(ori, o, ori)
            gx.release(); gy.release(); m.release(); o.release(); c.release()
        }

        val histogramMap = Mat.zeros(8, 8, CvType.CV_64FC(12))
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                val hist = DoubleArray(12)
                for (x in 0 until 16) {
                    for (y in 0 until 16) {
                        val v = mag.get(i * 16 + x, j * 16 + y)[0]
                        if (v > 0) {
                            val ang = ori.get(i * 16 + x, j * 16 + y)[0] % 180
                            hist[(ang / 15).toInt().coerceAtMost(11)] += v
                        }
                    }
                }
                val maxIdx = hist.indices.maxByOrNull { hist[it] } ?: 0
                val resultHist = DoubleArray(12) { if (it == maxIdx) 1.0 else 0.0 }
                histogramMap.put(i, j, *resultHist)
            }
        }
        mag.release(); ori.release(); resized.release()
        return histogramMap
    }

    private fun getAngleFromHog(histogramMap: Mat): List<Double> {
        val result = mutableListOf<Double>()
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                val data = histogramMap.get(i, j)
                val idx = data.indices.firstOrNull { data[it] == 1.0 } ?: -1
                result.add(if (idx == -1) -1.0 else idx * 15.0)
            }
        }
        return result
    }

    private fun getDistance(angle: List<Double>, centroidIdx: Int): Double {
        val target = centroid.getOrNull(centroidIdx) ?: return Double.MAX_VALUE
        var dist = 0.0
        for (i in angle.indices) {
            if (angle[i] == -1.0 || target[i] == -1.0) dist += 1.0
            else dist += 1.0 - abs(cos(Math.toRadians(angle[i] - target[i])))
        }
        return dist * 50.0
    }

    private fun loadCentroids(): MutableList<List<Double>> {
        val result = mutableListOf<List<Double>>()
        try {
            context.assets.open(CENTROID_CSV).use { stream ->
                val reader = CSVReaderBuilder(InputStreamReader(stream)).build()
                reader.iterator().forEach { row ->
                    if (row.isNotEmpty() && !row[0].contains("label")) {
                        val data = row.drop(1).map { it.trim().removePrefix("[").removeSuffix("]").toDouble() }
                        result.add(data)
                    }
                }
            }
        } catch (e: Exception) { Log.e("PoseDataSource", "센트로이드 로드 에러", e) }
        return result
    }

    private fun loadPoseRanks(): List<List<PoseDto>> {
        val rankList = mutableListOf<List<Double>>()
        try {
            context.assets.open(POSE_RANKS_CSV).use { stream ->
                val reader = CSVReaderBuilder(InputStreamReader(stream)).build()
                reader.iterator().forEach { row ->
                    if (row.size > 1 && row[1] != "pose_ids") {
                        val ids = row[1].removePrefix("[").removeSuffix("]").split(",").map { it.trim().toDouble() }
                        rankList.add(ids)
                    }
                }
            }
        } catch (e: Exception) { Log.e("PoseDataSource", "포즈 랭크 로드 에러", e) }
        
        val allPoses = loadAllPoseData()
        return rankList.mapIndexed { idx, ids ->
            ids.map { id -> allPoses.getOrElse(id.toInt()) { PoseDto(id.toInt(), -1) }.copy(poseCat = idx) }
        }
    }

    private fun loadAllPoseData(): List<PoseDto> {
        val result = mutableListOf<PoseDto>()
        val images = loadPoseImages()
        try {
            context.assets.open(IMAGE_DATA_CSV).use { stream ->
                val reader = CSVReaderBuilder(InputStreamReader(stream)).build()
                val rows = reader.readAll()
                rows.drop(1).forEach { row ->
                    val id = row[0].toInt()
                    val center = row[1].removePrefix("[").removeSuffix("]").split(",").map { it.trim().toFloat() }
                    val size = row[2].removePrefix("[").removeSuffix("]").split(",").map { it.trim().toFloat() }
                    result.add(PoseDto(poseId = id, bottomCenterRate = SizeF(center[0], center[1]), sizeRate = SizeF(size[0], size[1])))
                }
            }
            result.sortBy { it.poseId }
            result.forEachIndexed { i, pose -> if (i < images.size) result[i] = pose.copy(imageUri = images[i]) }
        } catch (e: Exception) { Log.e("PoseDataSource", "포즈 데이터 로드 에러", e) }
        return result
    }

    private fun loadPoseImages(): List<String> {
        val dir = File(context.dataDir, SILHOUETTE_DIR)
        val zipFile = File(context.dataDir, SILHOUETTE_ZIP)
        try {
            context.assets.open(SILHOUETTE_ZIP).use { input ->
                FileOutputStream(zipFile).use { output -> input.copyTo(output) }
            }
            if (!dir.exists()) dir.mkdir()
            unZip(zipFile, dir.absolutePath)
        } catch (e: Exception) { Log.e("PoseDataSource", "포즈 이미지 로드 에러", e) }
        return dir.listFiles()?.mapNotNull { file ->
            val name = file.name.removeSuffix(".png").toIntOrNull()
            if (name != null) name to file.absolutePath else null
        }?.sortedBy { it.first }?.map { it.second } ?: emptyList()
    }

    private fun unZip(srcZip: File, dstPath: String) {
        ZipFile(srcZip).use { zipFile ->
            zipFile.extractAll(dstPath)
        }
    }
}
