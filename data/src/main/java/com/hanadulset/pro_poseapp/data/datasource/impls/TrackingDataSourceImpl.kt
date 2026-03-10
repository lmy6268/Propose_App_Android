package com.hanadulset.pro_poseapp.data.datasource.impls

import com.hanadulset.pro_poseapp.data.datasource.interfaces.TrackingDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class TrackingDataSourceImpl @Inject constructor() : TrackingDataSource {

    private val _trackingFlow = MutableStateFlow<Pair<Float, Float>?>(null)
    override val trackingFlow: StateFlow<Pair<Float, Float>?> = _trackingFlow.asStateFlow()

    private val isTracking = AtomicBoolean(false)
    private var prevFrame: Mat? = null
    private var prevCornerPoint: MatOfPoint2f? = null
    private var lastValidOffset: Pair<Float, Float>? = null

    override fun startTracking(initialMat: Mat, initialOffset: Pair<Float, Float>) {
        if (initialMat.empty()) {
            android.util.Log.e("TrackingDataSource", "startTracking failed: initialMat is empty")
            return
        }
        val gray = Mat()
        Imgproc.cvtColor(initialMat, gray, Imgproc.COLOR_RGB2GRAY)
        
        val corners = MatOfPoint()
        Imgproc.goodFeaturesToTrack(gray, corners, 500, 0.01, 10.0)
        
        prevCornerPoint?.release()
        prevCornerPoint = MatOfPoint2f().apply { fromList(corners.toList()) }
        
        prevFrame?.release()
        prevFrame = gray.clone()
        
        lastValidOffset = initialOffset
        _trackingFlow.value = initialOffset
        
        corners.release()
        gray.release()
        isTracking.set(true)
    }

    override fun stopTracking() {
        isTracking.set(false)
        _trackingFlow.value = null
        prevFrame?.release(); prevFrame = null
        prevCornerPoint?.release(); prevCornerPoint = null
    }

    override fun processFrame(currentMat: Mat) {
        if (!isTracking.get() || currentMat.empty()) return
        
        val currentFrame = Mat()
        Imgproc.cvtColor(currentMat, currentFrame, Imgproc.COLOR_RGB2GRAY)
        
        val prev = prevFrame ?: return
        val currentPrevCornerPoint = prevCornerPoint ?: return
        
        val status = MatOfByte()
        val err = MatOfFloat()
        val nextCornerPoint = MatOfPoint2f()
        
        Video.calcOpticalFlowPyrLK(prev, currentFrame, currentPrevCornerPoint, nextCornerPoint, status, err)
        
        val statusList = status.toList()
        val goodNext = mutableListOf<Point>()
        val goodPrev = mutableListOf<Point>()
        val prevList = currentPrevCornerPoint.toList()
        val nextList = nextCornerPoint.toList()
        
        for (i in statusList.indices) {
            if (statusList[i].toInt() == 1) {
                goodPrev.add(prevList[i])
                goodNext.add(nextList[i])
            }
        }
        
        if (goodNext.size > 10) {
            val srcPoints = MatOfPoint2f().apply { fromList(goodPrev) }
            val dstPoints = MatOfPoint2f().apply { fromList(goodNext) }
            val affine = Calib3d.estimateAffinePartial2D(srcPoints, dstPoints, Mat(), Calib3d.RANSAC, 3.0)
            
            if (!affine.empty()) {
                val updated = calculateSmoothedOffset(affine)
                _trackingFlow.value = updated
                updateTrackingState(currentFrame, dstPoints, goodNext)
            } else stopTracking()
            
            srcPoints.release(); dstPoints.release(); affine.release()
        } else stopTracking()
        
        status.release(); err.release(); nextCornerPoint.release(); currentFrame.release()
    }

    private fun calculateSmoothedOffset(affine: Mat): Pair<Float, Float> {
        val a02 = affine.get(0, 2)[0]; val a12 = affine.get(1, 2)[0]
        val currentX = lastValidOffset?.first ?: 0f
        val currentY = lastValidOffset?.second ?: 0f
        val alpha = 0.8f
        val smoothedX = (alpha * (currentX + a02) + (1.0 - alpha) * currentX).toFloat()
        val smoothedY = (alpha * (currentY + a12) + (1.0 - alpha) * currentY).toFloat()
        return Pair(smoothedX, smoothedY).also { lastValidOffset = it }
    }

    private fun updateTrackingState(current: Mat, dstPoints: MatOfPoint2f, goodNext: List<Point>) {
        prevFrame?.release()
        prevFrame = current.clone()
        if (goodNext.size < 150) {
            val newCorners = MatOfPoint()
            Imgproc.goodFeaturesToTrack(current, newCorners, 500, 0.01, 10.0)
            val combined = goodNext.toMutableList().apply { addAll(newCorners.toList()) }.take(500)
            prevCornerPoint?.release()
            prevCornerPoint = MatOfPoint2f().apply { fromList(combined) }
            newCorners.release()
        } else {
            prevCornerPoint?.release()
            prevCornerPoint = MatOfPoint2f().apply { fromList(dstPoints.toList()) }
        }
    }
}
