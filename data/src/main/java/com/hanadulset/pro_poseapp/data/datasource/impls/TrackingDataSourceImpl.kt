package com.hanadulset.pro_poseapp.data.datasource.impls

import android.util.Log
import com.hanadulset.pro_poseapp.data.datasource.interfaces.TrackingDataSource
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import javax.inject.Inject

class TrackingDataSourceImpl @Inject constructor() : TrackingDataSource {

    companion object {
        private const val TAG = "TrackingDataSource"
        private const val MAX_FEATURES = 500
        private const val MIN_FEATURES_FOR_TRACKING = 4
        private const val MIN_FEATURES_REFRESH_THRESHOLD = 150
        private const val SMOOTHING_ALPHA = 0.7f
        private const val FRAME_INTERVAL_MS = 16L
    }

    override fun setTracker(
        initialMat: Mat,
        initialOffset: Pair<Float, Float>,
        frameProvider: suspend () -> Mat?
    ): Flow<Pair<Float, Float>?> = flow {
        if (initialMat.empty()) {
            Log.e(TAG, "setTracker failed: initialMat is empty")
            emit(null); return@flow
        }

        val frameWidth = initialMat.cols().toFloat().coerceAtLeast(1f)
        val frameHeight = initialMat.rows().toFloat().coerceAtLeast(1f)

        // 초기 그레이스케일 변환 및 피처 탐색
        val gray = Mat()
        Imgproc.cvtColor(initialMat, gray, Imgproc.COLOR_RGB2GRAY)
        val corners = MatOfPoint()
        Imgproc.goodFeaturesToTrack(gray, corners, MAX_FEATURES, 0.01, 10.0)

        if (corners.toList().size < MIN_FEATURES_FOR_TRACKING) {
            Log.w(TAG, "초기 피처 포인트 부족 (${corners.toList().size}개)")
            corners.release(); gray.release()
            emit(null); return@flow
        }

        // 트래킹 상태 (모두 flow 로컬 변수)
        var prevFrame: Mat = gray.clone()
        var prevCornerPoint = MatOfPoint2f().apply { fromList(corners.toList()) }
        var lastValidOffset = initialOffset
        corners.release(); gray.release()

        Log.d(TAG, "트래킹 시작: offset=$initialOffset, frame=${frameWidth}x${frameHeight}")
        emit(initialOffset)

        try {
            while (currentCoroutineContext().isActive) {
                val frame = frameProvider()
                if (frame == null || frame.empty()) {
                    frame?.release()
                    delay(FRAME_INTERVAL_MS); continue
                }

                val result = computeOpticalFlow(
                    frame, prevFrame, prevCornerPoint,
                    frameWidth, frameHeight, lastValidOffset
                )
                frame.release()

                when (result) {
                    is FrameResult.Tracked -> {
                        lastValidOffset = result.offset
                        prevFrame.release(); prevFrame = result.newPrevFrame
                        prevCornerPoint.release(); prevCornerPoint = result.newCornerPoints
                        emit(result.offset)
                    }
                    is FrameResult.Refreshed -> {
                        prevFrame.release(); prevFrame = result.newPrevFrame
                        prevCornerPoint.release(); prevCornerPoint = result.newCornerPoints
                        // 피처만 갱신됨, 오프셋 변화 없으므로 emit 생략
                    }
                    is FrameResult.Skipped -> {
                        // 피처를 아예 못 찾음 — 다음 프레임에서 재시도
                    }
                }

                delay(FRAME_INTERVAL_MS)
            }
        } finally {
            prevFrame.release()
            prevCornerPoint.release()
            Log.d(TAG, "트래킹 종료")
        }
    }


    // ── 프레임 처리 결과 ──

    private sealed class FrameResult {
        /** 트래킹 성공: 새 오프셋 + 갱신된 상태 */
        class Tracked(
            val offset: Pair<Float, Float>,
            val newPrevFrame: Mat,
            val newCornerPoints: MatOfPoint2f
        ) : FrameResult()

        /** 피처 재탐색 성공 (오프셋 변화 없음) */
        class Refreshed(
            val newPrevFrame: Mat,
            val newCornerPoints: MatOfPoint2f
        ) : FrameResult()

        /** 피처를 찾지 못함 — 다음 프레임에서 재시도 */
        data object Skipped : FrameResult()
    }

    // ── 옵티컬 플로우 계산 ──

    private fun computeOpticalFlow(
        frame: Mat,
        prevFrame: Mat,
        prevCornerPoint: MatOfPoint2f,
        frameWidth: Float,
        frameHeight: Float,
        lastValidOffset: Pair<Float, Float>
    ): FrameResult {
        val currentFrame = Mat()
        Imgproc.cvtColor(frame, currentFrame, Imgproc.COLOR_RGB2GRAY)

        val status = MatOfByte()
        val err = MatOfFloat()
        val nextCornerPoint = MatOfPoint2f()
        Video.calcOpticalFlowPyrLK(prevFrame, currentFrame, prevCornerPoint, nextCornerPoint, status, err)

        val statusList = status.toList()
        val goodPrev = mutableListOf<Point>()
        val goodNext = mutableListOf<Point>()
        val prevList = prevCornerPoint.toList()
        val nextList = nextCornerPoint.toList()

        for (i in statusList.indices) {
            if (statusList[i].toInt() == 1) {
                goodPrev.add(prevList[i])
                goodNext.add(nextList[i])
            }
        }

        val result: FrameResult

        if (goodNext.size > MIN_FEATURES_FOR_TRACKING) {
            val srcPoints = MatOfPoint2f().apply { fromList(goodPrev) }
            val dstPoints = MatOfPoint2f().apply { fromList(goodNext) }
            val affine = Calib3d.estimateAffinePartial2D(
                srcPoints, dstPoints, Mat(), Calib3d.RANSAC, 3.0
            )

            result = if (!affine.empty()) {
                val offset = calculateSmoothedOffset(
                    affine, frameWidth, frameHeight, lastValidOffset
                )
                val newPrevFrame = currentFrame.clone()
                val newCorners = refreshCornerPoints(currentFrame, dstPoints, goodNext)
                FrameResult.Tracked(offset, newPrevFrame, newCorners)
            } else {
                tryRefreshFeatures(currentFrame) ?: FrameResult.Skipped
            }

            srcPoints.release(); dstPoints.release(); affine.release()
        } else {
            result = tryRefreshFeatures(currentFrame) ?: FrameResult.Skipped
        }

        status.release(); err.release(); nextCornerPoint.release(); currentFrame.release()
        return result
    }

    // ── 헬퍼 메서드 ──

    /** 픽셀 변위를 프레임 크기로 정규화하여 스무딩된 오프셋 계산 */
    private fun calculateSmoothedOffset(
        affine: Mat,
        frameWidth: Float,
        frameHeight: Float,
        lastValidOffset: Pair<Float, Float>
    ): Pair<Float, Float> {
        val normalizedDx = (affine.get(0, 2)[0] / frameWidth).toFloat()
        val normalizedDy = (affine.get(1, 2)[0] / frameHeight).toFloat()
        val curX = lastValidOffset.first
        val curY = lastValidOffset.second
        return Pair(
            SMOOTHING_ALPHA * (curX + normalizedDx) + (1f - SMOOTHING_ALPHA) * curX,
            SMOOTHING_ALPHA * (curY + normalizedDy) + (1f - SMOOTHING_ALPHA) * curY
        )
    }

    /** 필요에 따라 피처 포인트를 보충하여 새 코너 목록 반환 */
    private fun refreshCornerPoints(
        currentFrame: Mat,
        dstPoints: MatOfPoint2f,
        goodNext: List<Point>
    ): MatOfPoint2f {
        return if (goodNext.size < MIN_FEATURES_REFRESH_THRESHOLD) {
            val newCorners = MatOfPoint()
            Imgproc.goodFeaturesToTrack(currentFrame, newCorners, MAX_FEATURES, 0.01, 10.0)
            val combined = (goodNext + newCorners.toList()).take(MAX_FEATURES)
            newCorners.release()
            MatOfPoint2f().apply { fromList(combined) }
        } else {
            MatOfPoint2f().apply { fromList(dstPoints.toList()) }
        }
    }

    /** 피처 전체 재탐색 시도. 성공 시 Refreshed, 실패 시 null(=Skipped) */
    private fun tryRefreshFeatures(currentFrame: Mat): FrameResult.Refreshed? {
        val newCorners = MatOfPoint()
        Imgproc.goodFeaturesToTrack(currentFrame, newCorners, MAX_FEATURES, 0.01, 10.0)
        val newList = newCorners.toList()
        newCorners.release()

        return if (newList.size > MIN_FEATURES_FOR_TRACKING) {
            Log.d(TAG, "피처 재탐색 성공: ${newList.size}개")
            FrameResult.Refreshed(
                newPrevFrame = currentFrame.clone(),
                newCornerPoints = MatOfPoint2f().apply { fromList(newList) }
            )
        } else {
            null
        }
    }
}
