package com.hanadulset.pro_poseapp.data.datasource.impls

import android.graphics.Bitmap
import android.util.Log
import android.util.SizeF
import androidx.core.graphics.createBitmap
import com.hanadulset.pro_poseapp.data.datasource.interfaces.VisionProcessDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import javax.inject.Inject

class VisionProcessDataSourceImpl @Inject constructor() : VisionProcessDataSource {

    init {
        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV loaded successfully")
        }
    }

    private object TrackingConfig {
        const val MAX_FEATURES = 500
        const val QUALITY_LEVEL = 0.01
        const val MIN_DISTANCE = 10.0
        const val RANSAC_THRESHOLD = 3.0
        const val EMA_ALPHA = 0.8f
        const val REFILL_THRESHOLD = 150
        const val MIN_POINTS_FOR_TRACKING = 10
        const val CANNY_THRESHOLD1 = 50.0
        const val CANNY_THRESHOLD2 = 150.0
    }

    private var prevFrame: Mat? = null
    private var prevCornerPoint: MatOfPoint2f? = null
    private var isAnchorSet = false
    private var lastValidOffset: SizeF? = null

    override fun getFixedImage(bitmap: Bitmap): Bitmap {
        val input = Mat()
        val hierarchy = Mat()
        val output = Mat()
        
        Utils.bitmapToMat(bitmap, input)
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY)
        Imgproc.Canny(input, output, TrackingConfig.CANNY_THRESHOLD1, TrackingConfig.CANNY_THRESHOLD2)
        
        val points = mutableListOf<MatOfPoint>()
        Imgproc.findContours(input, points, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE)
        
        for (i in points.indices) {
            Imgproc.drawContours(output, points, i, Scalar(255.0, 255.0, 255.0))
        }

        val result = bitmap.copy(bitmap.config!!, true).apply {
            Utils.matToBitmap(output, this)
        }
        
        releaseMats(input, hierarchy, output)
        points.releaseAll()
        return result
    }

    override fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: Size): Bitmap {
        // 1. Mat 생성 시 빈 객체로 생성 (bitmapToMat이 크기를 자동 설정함)
        val inputImageMat = Mat()
        // 2. 출력 비트맵 설정 (모델 입력 규격에 맞춰 ARGB_8888 권장될 수 있음)
        val outputResizeBitmap = createBitmap(size.width.toInt(), size.height.toInt())
        try {
            Utils.bitmapToMat(bitmap, inputImageMat)
            // 3. RGBA -> RGB 변환 (PyTorch 입력용)
            Imgproc.cvtColor(inputImageMat, inputImageMat, Imgproc.COLOR_RGBA2RGB)
            // 4. 리사이징 (INTER_AREA가 축소 시 화질이 가장 좋음)
            Imgproc.resize(inputImageMat, inputImageMat, size, 0.0, 0.0, Imgproc.INTER_AREA)
            Utils.matToBitmap(inputImageMat, outputResizeBitmap)
        } finally {
            // 5. 반드시 네이티브 자원 해제
            inputImageMat.releaseSafe()
        }
        return outputResizeBitmap
    }




    override suspend fun useOpticalFlow(bitmap: Bitmap, targetOffset: SizeF): SizeF? =
        withContext(Dispatchers.Default) { // CPU 집중 작업이므로 Default 사용
            val currentFrame = Mat()
            // 로직 내부에서 생성되는 모든 임시 Mat들을 추적하기 위한 리스트
            val tempMats = mutableListOf(currentFrame)

            runCatching {
                Utils.bitmapToMat(bitmap, currentFrame)
                Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2GRAY)

                val prev = prevFrame
                if (prev == null || !isAnchorSet) {
                    initializeTracking(currentFrame, targetOffset)
                } else {
                    processTracking(prev, currentFrame, targetOffset, tempMats)
                }
            }.onFailure { e ->
                Log.e("OpticalFlow", "Tracking Error: ${e.message}", e)
                // 실패 시 현재 프레임은 해제 (성공 시엔 prevFrame으로 교체됨)
                currentFrame.releaseSafe()
            }.also {
                // [핵심] 성공/실패 여부와 상관없이 임시 Mat들을 모두 해제
                tempMats.forEach { it.releaseSafe() }
            }.getOrNull()
        }

    // 1. 초기 추적 포인트 설정 (앵커 설정)
    private fun initializeTracking(currentFrame: Mat, targetOffset: SizeF): SizeF {
        val corners = MatOfPoint().also { Imgproc.goodFeaturesToTrack(currentFrame, it, TrackingConfig.MAX_FEATURES, TrackingConfig.QUALITY_LEVEL, TrackingConfig.MIN_DISTANCE) }

        prevCornerPoint?.releaseSafe()
        prevCornerPoint = MatOfPoint2f().apply { fromList(corners.toList()) }

        prevFrame?.releaseSafe()
        prevFrame = currentFrame.clone() // 다음 프레임을 위해 복사본 보관

        isAnchorSet = true
        lastValidOffset = targetOffset
        corners.releaseSafe()

        return targetOffset
    }

    // 2. 실제 추적 로직 (LK Optical Flow + RANSAC)
    private fun processTracking(prev: Mat, current: Mat, targetOffset: SizeF, tempMats: MutableList<Mat>): SizeF {
        // prevCornerPoint가 null이면 더 이상 진행할 수 없으므로 초기화로 유도하거나 현재 오프셋 반환
        val currentPrevCornerPoint = prevCornerPoint ?: return initializeTracking(current, targetOffset)

        val status = MatOfByte().also { tempMats.add(it) }
        val err = MatOfFloat().also { tempMats.add(it) }
        val nextCornerPoint = MatOfPoint2f().also { tempMats.add(it) }

        // 안전하게 가져온 currentPrevCornerPoint 사용
        Video.calcOpticalFlowPyrLK(prev, current, currentPrevCornerPoint, nextCornerPoint, status, err)

        val statusList = status.toList()
        val goodPrev = mutableListOf<Point>()
        val goodNext = mutableListOf<Point>()

        // [수정 포인트] !!를 제거하고 지역 변수를 사용하여 안전하게 접근
        val prevList = currentPrevCornerPoint.toList()
        val nextList = nextCornerPoint.toList()

        for (i in statusList.indices) {
            // statusList[i]가 리스트 범위를 벗어나지 않는지 확인하는 로직이 있으면 더 좋음
            if (i < prevList.size && i < nextList.size && statusList[i].toInt() == 1) {
                goodPrev.add(prevList[i])
                goodNext.add(nextList[i])
            }
        }

        var updatedOffset: SizeF? = null
        if (goodNext.size > TrackingConfig.MIN_POINTS_FOR_TRACKING) {
            val srcPoints = MatOfPoint2f().apply { fromList(goodPrev) }.also { tempMats.add(it) }
            val dstPoints = MatOfPoint2f().apply { fromList(goodNext) }.also { tempMats.add(it) }
            val inliers = Mat().also { tempMats.add(it) }
            val affine = Calib3d.estimateAffinePartial2D(srcPoints, dstPoints, inliers, Calib3d.RANSAC, TrackingConfig.RANSAC_THRESHOLD).also { tempMats.add(it) }

            if (!affine.empty()) {
                updatedOffset = calculateSmoothedOffset(affine, targetOffset)
                updateTrackingState(current, dstPoints, goodNext)
            }
        }

        return updatedOffset ?: lastValidOffset ?: targetOffset
    }

    // 3. 아핀 변환 행렬을 이용한 위치 계산 및 EMA 필터링
    private fun calculateSmoothedOffset(affine: Mat, targetOffset: SizeF): SizeF {
        val a00 = affine.get(0, 0)[0]; val a01 = affine.get(0, 1)[0]; val a02 = affine.get(0, 2)[0]
        val a10 = affine.get(1, 0)[0]; val a11 = affine.get(1, 1)[0]; val a12 = affine.get(1, 2)[0]

        // 현재 기준점 (없으면 타겟 위치)
        val currentX = lastValidOffset?.width?.toDouble() ?: targetOffset.width.toDouble()
        val currentY = lastValidOffset?.height?.toDouble() ?: targetOffset.height.toDouble()

        // 행렬 곱 연산 (변환 적용)
        val transformedX = a00 * currentX + a01 * currentY + a02
        val transformedY = a10 * currentX + a11 * currentY + a12

        // 지수 이동 평균 (EMA) 필터 적용하여 떨림 방지
        val alpha = TrackingConfig.EMA_ALPHA
        val prevX = lastValidOffset?.width?.toDouble() ?: currentX
        val prevY = lastValidOffset?.height?.toDouble() ?: currentY

        val smoothedX = (alpha * transformedX + (1.0 - alpha) * prevX).toFloat()
        val smoothedY = (alpha * transformedY + (1.0 - alpha) * prevY).toFloat()

        return SizeF(smoothedX, smoothedY).also { lastValidOffset = it }
    }

    // 4. 다음 프레임을 위한 상태(Frame, Corner Points) 업데이트
    private fun updateTrackingState(current: Mat, dstPoints: MatOfPoint2f, goodNext: List<Point>) {
        // 이전 프레임 해제 및 현재 프레임 복사본 저장
        prevFrame?.releaseSafe()
        prevFrame = current.clone()

        // 특징점(Corner Points) 보충 로직
        if (goodNext.size < TrackingConfig.REFILL_THRESHOLD) {
            val newCorners = MatOfPoint()
            Imgproc.goodFeaturesToTrack(
                current, newCorners,
                TrackingConfig.MAX_FEATURES,
                TrackingConfig.QUALITY_LEVEL,
                TrackingConfig.MIN_DISTANCE
            )

            // 기존점 + 새 점 합치기
            val combinedPoints = goodNext.toMutableList().apply { addAll(newCorners.toList()) }
            val limitedPoints = combinedPoints.take(TrackingConfig.MAX_FEATURES)

            prevCornerPoint?.releaseSafe()
            prevCornerPoint = MatOfPoint2f().apply { fromList(limitedPoints) }
            newCorners.releaseSafe()
        } else {
            // 기존 특징점이 충분하면 그대로 교체
            prevCornerPoint?.releaseSafe()
            // dstPoints는 tempMats에 의해 해제될 수 있으므로 clone하여 상태 유지
            prevCornerPoint = MatOfPoint2f().apply { fromList(dstPoints.toList()) }
        }
    }


    override fun stopToUseOpticalFlow() {
        // 1. 상태 변수들을 먼저 업데이트하여 추가적인 processTracking 진입을 막음
        isAnchorSet = false
        lastValidOffset = null

        // 2. 지역 변수에 담아두고 원본 참조를 null로 만들어 스레드 간섭 차단
        val frameToRelease = prevFrame
        val pointsToRelease = prevCornerPoint

        prevFrame = null
        prevCornerPoint = null

        // 3. 네이티브 자원 해제
        frameToRelease?.releaseSafe()
        pointsToRelease?.releaseSafe()

        Log.d("OpticalFlow", "Optical Flow Stopped and Resources Released")
    }

    private fun Mat?.releaseSafe() = this?.release()
    private fun Iterable<Mat?>.releaseAll() = forEach { it?.releaseSafe() }
    private fun releaseMats(vararg mats: Mat?) = mats.forEach { it?.releaseSafe() }
}
