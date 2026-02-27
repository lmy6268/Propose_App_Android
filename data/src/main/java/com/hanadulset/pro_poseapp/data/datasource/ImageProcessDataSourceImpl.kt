package com.hanadulset.pro_poseapp.data.datasource

import android.graphics.Bitmap
import android.util.Log
import android.util.SizeF
import androidx.core.graphics.createBitmap
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ImageProcessDataSource
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
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video

class ImageProcessDataSourceImpl : ImageProcessDataSource {

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
        val input = Mat()
        Utils.bitmapToMat(bitmap, input)
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY)
        val hierarchy = Mat.zeros(input.size(), input.type())
        val output = Mat.zeros(input.size(), input.type())

        Imgproc.Canny(input, output, 50.0, 150.0)
        val points = mutableListOf<MatOfPoint>()
        Imgproc.findContours(input, points, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE)
        for (i in points.indices) {
            Imgproc.drawContours(output, points, i, Scalar(255.0, 255.0, 255.0))
        }

        return bitmap.copy(bitmap.config!!, true).apply {
            Utils.matToBitmap(output, this)
        }
    }

    private fun bitmapToMatWithOpenCV(bitmap: Bitmap): Mat {
        val resMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        Utils.bitmapToMat(bitmap, resMat)
        Imgproc.cvtColor(resMat, resMat, Imgproc.COLOR_RGB2GRAY)
        return resMat
    }

    override fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: Size): Bitmap {
        val inputImageMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        val outputResizeBitmap = createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.RGB_565)
        Utils.bitmapToMat(bitmap, inputImageMat)
        Imgproc.cvtColor(inputImageMat, inputImageMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.resize(inputImageMat, inputImageMat, size)
        Utils.matToBitmap(inputImageMat, outputResizeBitmap)
        return outputResizeBitmap
    }

    /**
     * 호모그래피(Homography) 기반 고성능 트래킹
     * 카메라의 모든 변화(이동, 회전, 줌)를 계산하여 Anchor를 고정합니다.
     */
//    override suspend fun useOpticalFlow(bitmap: Bitmap, targetOffset: SizeF): SizeF? =
//        kotlin.runCatching {
//            val currentFrame = bitmapToMatWithOpenCV(bitmap)
//
//            if (prevFrame == null || !isAnchorSet) {
//                prevFrame = currentFrame
//                isAnchorSet = true
//                lastValidOffset = targetOffset
//
//                val corners = MatOfPoint()
//                Imgproc.goodFeaturesToTrack(currentFrame, corners, 500, 0.01, 10.0)
//                prevCornerPoint = MatOfPoint2f().apply { fromList(corners.toList()) }
//
//                return targetOffset
//            } else {
//                val status = MatOfByte()
//                val err = MatOfFloat()
//                val nextCornerPoint = MatOfPoint2f()
//
//                Video.calcOpticalFlowPyrLK(prevFrame, currentFrame, prevCornerPoint, nextCornerPoint, status, err)
//
//                val statusList = status.toList()
//                val prevList = prevCornerPoint!!.toList()
//                val nextList = nextCornerPoint.toList()
//
//                val goodPrev = mutableListOf<Point>()
//                val goodNext = mutableListOf<Point>()
//
//                for (i in statusList.indices) {
//                    if (statusList[i].toInt() == 1) {
//                        goodPrev.add(prevList[i])
//                        goodNext.add(nextList[i])
//                    }
//                }
//
//                if (goodNext.size > 10) { // 최소 10개 이상의 포인트가 매칭되어야 함
//                    val srcPoints = MatOfPoint2f().apply { fromList(goodPrev) }
//                    val dstPoints = MatOfPoint2f().apply { fromList(goodNext) }
//
//                    // 1. 호모그래피 행렬 계산 (RANSAC 알고리즘으로 노이즈 제거)
//                    val homography = Calib3d.findHomography(srcPoints, dstPoints, Calib3d.RANSAC, 5.0)
//
//                    if (!homography.empty()) {
//                        // 2. 현재 Anchor 좌표에 호모그래피 변환 적용
//                        val pointsToTransform = MatOfPoint2f(Point(targetOffset.width.toDouble(), targetOffset.height.toDouble()))
//                        val transformedPoints = MatOfPoint2f()
//                        org.opencv.core.Core.perspectiveTransform(pointsToTransform, transformedPoints, homography)
//
//                        val resultPoint = transformedPoints.toList()[0]
//                        val updatedOffset = SizeF(resultPoint.x.toFloat(), resultPoint.y.toFloat())
//
//                        // 3. 상태 업데이트 및 포인트 보충
//                        prevFrame = currentFrame
//                        lastValidOffset = updatedOffset
//
//                        if (goodNext.size < 150) {
//                            val newCorners = MatOfPoint()
//                            Imgproc.goodFeaturesToTrack(currentFrame, newCorners, 500, 0.01, 10.0)
//                            prevCornerPoint = MatOfPoint2f().apply { fromList(newCorners.toList()) }
//                        } else {
//                            prevCornerPoint = dstPoints
//                        }
//
//                        return updatedOffset
//                    }
//                }
//
//                // 트래킹 일시적 실패 시 이전 위치 유지
//                prevFrame = currentFrame
//                return lastValidOffset ?: targetOffset
//            }
//        }.getOrNull()

    override suspend fun useOpticalFlow(bitmap: Bitmap, targetOffset: SizeF): SizeF? =
        kotlin.runCatching {
            val currentFrame = bitmapToMatWithOpenCV(bitmap)

            if (prevFrame == null || !isAnchorSet) {
                prevFrame = currentFrame
                isAnchorSet = true
                lastValidOffset = targetOffset

                val corners = MatOfPoint()
                Imgproc.goodFeaturesToTrack(currentFrame, corners, 500, 0.01, 10.0)
                prevCornerPoint = MatOfPoint2f().apply { fromList(corners.toList()) }

                corners.release() // 메모리 해제

                return targetOffset
            } else {
                val status = MatOfByte()
                val err = MatOfFloat()
                val nextCornerPoint = MatOfPoint2f()

                Video.calcOpticalFlowPyrLK(prevFrame, currentFrame, prevCornerPoint, nextCornerPoint, status, err)

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

                if (goodNext.size > 10) {
                    val srcPoints = MatOfPoint2f().apply { fromList(goodPrev) }
                    val dstPoints = MatOfPoint2f().apply { fromList(goodNext) }
                    val inliers = Mat() // 오버로딩 시그니처 매칭용 객체 생성

                    // 1. Partial Affine Transform 행렬 계산
                    val affine = Calib3d.estimateAffinePartial2D(srcPoints, dstPoints, inliers, Calib3d.RANSAC, 3.0)

                    if (!affine.empty()) {
                        // 2. 현재 Anchor 좌표에 아핀 변환 적용
                        val a00 = affine.get(0, 0)[0]
                        val a01 = affine.get(0, 1)[0]
                        val a02 = affine.get(0, 2)[0]
                        val a10 = affine.get(1, 0)[0]
                        val a11 = affine.get(1, 1)[0]
                        val a12 = affine.get(1, 2)[0]

                        // 1. 이전 프레임의 최종 좌표를 기준으로 아핀 변환 적용
                        val currentX = lastValidOffset?.width?.toDouble() ?: targetOffset.width.toDouble()
                        val currentY = lastValidOffset?.height?.toDouble() ?: targetOffset.height.toDouble()

                        val transformedX = a00 * currentX + a01 * currentY + a02
                        val transformedY = a10 * currentX + a11 * currentY + a12

// 2. 반응성을 극대화한 EMA 필터 재적용 (Alpha = 0.8)
                        val alpha = 0.8f
                        val prevX = lastValidOffset?.width?.toDouble() ?: currentX
                        val prevY = lastValidOffset?.height?.toDouble() ?: currentY

                        val smoothedX = (alpha * transformedX + (1.0 - alpha) * prevX).toFloat()
                        val smoothedY = (alpha * transformedY + (1.0 - alpha) * prevY).toFloat()

                        val updatedOffset = SizeF(smoothedX, smoothedY)

                        // 4. 상태 업데이트
                        prevFrame?.release() // 기존 프레임 메모리 해제
                        prevFrame = currentFrame
                        lastValidOffset = updatedOffset

                        // 5. 특징점 보충 로직
                        if (goodNext.size < 150) {
                            val newCorners = MatOfPoint()
                            Imgproc.goodFeaturesToTrack(currentFrame, newCorners, 500, 0.01, 10.0)

                            val combinedPoints = goodNext.toMutableList()
                            combinedPoints.addAll(newCorners.toList())

                            val limitedPoints = if (combinedPoints.size > 500) combinedPoints.take(500) else combinedPoints

                            prevCornerPoint?.release()
                            prevCornerPoint = MatOfPoint2f().apply { fromList(limitedPoints) }
                            newCorners.release() // 메모리 해제
                        } else {
                            prevCornerPoint?.release()
                            prevCornerPoint = dstPoints
                        }

                        // 사용 완료된 네이티브 객체 메모리 반환
                        srcPoints.release()
                        inliers.release()
                        affine.release()
                        status.release()
                        err.release()
                        nextCornerPoint.release()

                        return updatedOffset
                    }

                    // 변환 실패 시 생성된 객체 정리
                    srcPoints.release()
                    dstPoints.release()
                    inliers.release()
                    affine.release()
                }

                // 트래킹 실패 시 생성된 객체 정리 및 이전 위치 유지
                status.release()
                err.release()
                nextCornerPoint.release()

                prevFrame?.release()
                prevFrame = currentFrame
                return lastValidOffset ?: targetOffset
            }
        }.getOrNull()

    override fun stopToUseOpticalFlow() {
        prevFrame = null
        prevCornerPoint = null
        isAnchorSet = false
        lastValidOffset = null
    }
}
