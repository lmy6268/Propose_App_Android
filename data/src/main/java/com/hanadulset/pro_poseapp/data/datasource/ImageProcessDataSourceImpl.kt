package com.hanadulset.pro_poseapp.data.datasource

import android.graphics.Bitmap
import android.util.Log
import android.util.SizeF
import androidx.core.graphics.createBitmap
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ImageProcessDataSource
import javax.inject.Inject
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video

class ImageProcessDataSourceImpl @Inject constructor() : ImageProcessDataSource {

    init {
        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV loaded successfully")
        }
    }

    private var prevFrame: Mat? = null
    private var prevCornerPoint: MatOfPoint2f? = null
    private var isAnchorSet = false
    private var lastValidOffset: SizeF? = null

    override fun getFixedImage(bitmap: Bitmap): Bitmap {
        /*
        // [기존의 노이즈가 많은 외곽선 추출 방식 (참고용으로 주석 처리)]
        val input = Mat()
        Utils.bitmapToMat(bitmap, input)
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY)
        val hierarchy = Mat.zeros(input.size(), input.type())
        val output = Mat.zeros(input.size(), input.type())

        Imgproc.Canny(input, output, 50.0, 150.0)
        val points = mutableListOf<MatOfPoint>()
        Imgproc.findContours(
                input,
                points,
                hierarchy,
                Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_NONE
        )
        for (i in points.indices) {
            Imgproc.drawContours(output, points, i, Scalar(255.0, 255.0, 255.0))
        }

        return bitmap.copy(bitmap.config!!, true).apply { Utils.matToBitmap(output, this) }
        */

        // [개선된 스케치 느낌의 부드러운 외곽선 추출 방식]
        val input = Mat()
        Utils.bitmapToMat(bitmap, input)

        // 1. Convert to Grayscale
        val grayMat = Mat()
        Imgproc.cvtColor(input, grayMat, Imgproc.COLOR_RGB2GRAY)

        // 2. Bilateral Filter: Smooths textures (noise, wrinkles) while keeping edges sharp.
        val smoothedMat = Mat()
        Imgproc.bilateralFilter(grayMat, smoothedMat, 5, 75.0, 75.0)

        // 3. Canny Edge Detection: Tweaked thresholds for cleaner main lines after smoothing.
        val edges = Mat()
        Imgproc.Canny(smoothedMat, edges, 40.0, 120.0)

        // 4. Morphological Closing: Connects broken lines and removes tiny noise dots.
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        Imgproc.morphologyEx(edges, edges, Imgproc.MORPH_CLOSE, kernel)

        return bitmap.copy(bitmap.config!!, true).apply { Utils.matToBitmap(edges, this) }
    }

    private fun bitmapToMatWithOpenCV(bitmap: Bitmap): Mat {
        val resMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        Utils.bitmapToMat(bitmap, resMat)
        Imgproc.cvtColor(resMat, resMat, Imgproc.COLOR_RGB2GRAY)
        return resMat
    }

    override fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: Size): Bitmap {
        val inputImageMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        val outputResizeBitmap =
                createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.RGB_565)
        Utils.bitmapToMat(bitmap, inputImageMat)
        Imgproc.cvtColor(inputImageMat, inputImageMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.resize(inputImageMat, inputImageMat, size)
        Utils.matToBitmap(inputImageMat, outputResizeBitmap)
        return outputResizeBitmap
    }

    /** 호모그래피(Homography) 기반 고성능 트래킹 카메라의 모든 변화(이동, 회전, 줌)를 계산하여 Anchor를 고정합니다. */
    override suspend fun useOpticalFlow(bitmap: Bitmap, targetOffset: SizeF): SizeF? =
            kotlin
                    .runCatching {
                        val currentFrame = bitmapToMatWithOpenCV(bitmap)

                        if (prevFrame == null || !isAnchorSet) {
                            prevFrame = currentFrame
                            isAnchorSet = true
                            lastValidOffset = targetOffset

                            val corners = MatOfPoint()
                            Imgproc.goodFeaturesToTrack(currentFrame, corners, 500, 0.01, 10.0)
                            prevCornerPoint = MatOfPoint2f().apply { fromList(corners.toList()) }

                            return targetOffset
                        } else {
                            val status = MatOfByte()
                            val err = MatOfFloat()
                            val nextCornerPoint = MatOfPoint2f()

                            Video.calcOpticalFlowPyrLK(
                                    prevFrame,
                                    currentFrame,
                                    prevCornerPoint,
                                    nextCornerPoint,
                                    status,
                                    err
                            )

                            val statusList = status.toList()
                            val prevList = prevCornerPoint!!.toList()
                            val nextList = nextCornerPoint.toList()

                            val goodPrev = mutableListOf<Point>()
                            val goodNext = mutableListOf<Point>()

                            for (i in statusList.indices) {
                                if (statusList[i].toInt() == 1) {
                                    goodPrev.add(prevList[i])
                                    goodNext.add(nextList[i])
                                }
                            }

                            if (goodNext.size > 10) { // 최소 10개 이상의 포인트가 매칭되어야 함
                                val srcPoints = MatOfPoint2f().apply { fromList(goodPrev) }
                                val dstPoints = MatOfPoint2f().apply { fromList(goodNext) }

                                // 1. 호모그래피 행렬 계산 (RANSAC 알고리즘으로 노이즈 제거)
                                val homography =
                                        Calib3d.findHomography(
                                                srcPoints,
                                                dstPoints,
                                                Calib3d.RANSAC,
                                                5.0
                                        )

                                if (!homography.empty()) {
                                    // 2. 현재 Anchor 좌표에 호모그래피 변환 적용
                                    val pointsToTransform =
                                            MatOfPoint2f(
                                                    Point(
                                                            targetOffset.width.toDouble(),
                                                            targetOffset.height.toDouble()
                                                    )
                                            )
                                    val transformedPoints = MatOfPoint2f()
                                    org.opencv.core.Core.perspectiveTransform(
                                            pointsToTransform,
                                            transformedPoints,
                                            homography
                                    )

                                    val resultPoint = transformedPoints.toList()[0]
                                    val updatedOffset =
                                            SizeF(resultPoint.x.toFloat(), resultPoint.y.toFloat())

                                    // 3. 상태 업데이트 및 포인트 보충
                                    prevFrame = currentFrame
                                    lastValidOffset = updatedOffset

                                    if (goodNext.size < 150) {
                                        val newCorners = MatOfPoint()
                                        Imgproc.goodFeaturesToTrack(
                                                currentFrame,
                                                newCorners,
                                                500,
                                                0.01,
                                                10.0
                                        )
                                        prevCornerPoint =
                                                MatOfPoint2f().apply {
                                                    fromList(newCorners.toList())
                                                }
                                    } else {
                                        prevCornerPoint = dstPoints
                                    }

                                    return updatedOffset
                                }
                            }

                            // 트래킹 일시적 실패 시 이전 위치 유지
                            prevFrame = currentFrame
                            return lastValidOffset ?: targetOffset
                        }
                    }
                    .getOrNull()

    override fun stopToUseOpticalFlow() {
        prevFrame = null
        prevCornerPoint = null
        isAnchorSet = false
        lastValidOffset = null
    }
}
