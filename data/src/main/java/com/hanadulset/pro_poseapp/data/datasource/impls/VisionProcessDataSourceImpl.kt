package com.hanadulset.pro_poseapp.data.datasource.impls

import android.graphics.Bitmap
import android.util.Log
import android.util.SizeF
import androidx.core.graphics.createBitmap
import com.hanadulset.pro_poseapp.data.datasource.interfaces.VisionProcessDataSource
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
        val inputImageMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        val outputResizeBitmap = createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.RGB_565)
        
        Utils.bitmapToMat(bitmap, inputImageMat)
        Imgproc.cvtColor(inputImageMat, inputImageMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.resize(inputImageMat, inputImageMat, size)
        Utils.matToBitmap(inputImageMat, outputResizeBitmap)
        
        inputImageMat.releaseSafe()
        return outputResizeBitmap
    }

    override suspend fun useOpticalFlow(bitmap: Bitmap, targetOffset: SizeF): SizeF? =
        runCatching {
            val currentFrame = Mat()
            Utils.bitmapToMat(bitmap, currentFrame)
            Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2GRAY)

            if (prevFrame == null || !isAnchorSet) {
                prevFrame = currentFrame
                isAnchorSet = true
                lastValidOffset = targetOffset

                val corners = MatOfPoint()
                Imgproc.goodFeaturesToTrack(currentFrame, corners, TrackingConfig.MAX_FEATURES, TrackingConfig.QUALITY_LEVEL, TrackingConfig.MIN_DISTANCE)
                prevCornerPoint = MatOfPoint2f().apply { fromList(corners.toList()) }
                corners.releaseSafe()
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

                var updatedOffset: SizeF? = null
                if (goodNext.size > TrackingConfig.MIN_POINTS_FOR_TRACKING) {
                    val srcPoints = MatOfPoint2f().apply { fromList(goodPrev) }
                    val dstPoints = MatOfPoint2f().apply { fromList(goodNext) }
                    val inliers = Mat()
                    val affine = Calib3d.estimateAffinePartial2D(srcPoints, dstPoints, inliers, Calib3d.RANSAC, TrackingConfig.RANSAC_THRESHOLD)

                    if (!affine.empty()) {
                        val a00 = affine.get(0, 0)[0]; val a01 = affine.get(0, 1)[0]; val a02 = affine.get(0, 2)[0]
                        val a10 = affine.get(1, 0)[0]; val a11 = affine.get(1, 1)[0]; val a12 = affine.get(1, 2)[0]

                        val currentX = lastValidOffset?.width?.toDouble() ?: targetOffset.width.toDouble()
                        val currentY = lastValidOffset?.height?.toDouble() ?: targetOffset.height.toDouble()

                        val transformedX = a00 * currentX + a01 * currentY + a02
                        val transformedY = a10 * currentX + a11 * currentY + a12

                        val alpha = TrackingConfig.EMA_ALPHA
                        val prevX = lastValidOffset?.width?.toDouble() ?: currentX
                        val prevY = lastValidOffset?.height?.toDouble() ?: currentY

                        val smoothedX = (alpha * transformedX + (1.0 - alpha) * prevX).toFloat()
                        val smoothedY = (alpha * transformedY + (1.0 - alpha) * prevY).toFloat()

                        updatedOffset = SizeF(smoothedX, smoothedY)
                        prevFrame.releaseSafe()
                        prevFrame = currentFrame
                        lastValidOffset = updatedOffset

                        if (goodNext.size < TrackingConfig.REFILL_THRESHOLD) {
                            val newCorners = MatOfPoint()
                            Imgproc.goodFeaturesToTrack(currentFrame, newCorners, TrackingConfig.MAX_FEATURES, TrackingConfig.QUALITY_LEVEL, TrackingConfig.MIN_DISTANCE)
                            val combinedPoints = goodNext.toMutableList().apply { addAll(newCorners.toList()) }
                            val limitedPoints = combinedPoints.take(TrackingConfig.MAX_FEATURES)
                            prevCornerPoint.releaseSafe()
                            prevCornerPoint = MatOfPoint2f().apply { fromList(limitedPoints) }
                            newCorners.releaseSafe()
                        } else {
                            prevCornerPoint.releaseSafe()
                            prevCornerPoint = dstPoints
                        }
                    }
                    releaseMats(srcPoints, inliers, affine)
                    if (prevCornerPoint != dstPoints) dstPoints.releaseSafe()
                }

                if (updatedOffset == null) {
                    prevFrame.releaseSafe()
                    prevFrame = currentFrame
                    updatedOffset = lastValidOffset ?: targetOffset
                }
                releaseMats(status, err, nextCornerPoint)
                return updatedOffset
            }
        }.getOrNull()

    override fun stopToUseOpticalFlow() {
        releaseMats(prevFrame, prevCornerPoint)
        prevFrame = null
        prevCornerPoint = null
        isAnchorSet = false
        lastValidOffset = null
    }

    private fun Mat?.releaseSafe() = this?.release()
    private fun Iterable<Mat?>.releaseAll() = forEach { it?.releaseSafe() }
    private fun releaseMats(vararg mats: Mat?) = mats.forEach { it?.releaseSafe() }
}
