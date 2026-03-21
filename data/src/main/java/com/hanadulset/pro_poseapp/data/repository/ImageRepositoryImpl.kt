package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.data.datasource.interfaces.FileHandleDataSource
import com.hanadulset.pro_poseapp.data.mapper.toDomain
import com.hanadulset.pro_poseapp.data.mapper.toDto
import com.hanadulset.pro_poseapp.data.model.ImageResultDto
import com.hanadulset.pro_poseapp.domain.model.ImageResultEntity
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.domain.model.CaptureEventEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.net.URL
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileHandleDataSource: FileHandleDataSource
) : ImageRepository {

    companion object {
        private const val ERROR_UNSUPPORTED_TYPE = "지원하지 않는 이미지 타입입니다."
    }

    // 최신 프레임을 내부적으로 보관 (Pull 방식 공급용)
    @Volatile
    private var latestFrameMat: Mat? = null

    // 프레임 캡처 활성화 플래그 (분석 요청 시에만 true)
    @Volatile
    private var isCapturing = false

    override fun startCapture() {
        isCapturing = true
    }

    override fun stopCapture() {
        isCapturing = false
        synchronized(this) {
            latestFrameMat?.release()
            latestFrameMat = null
        }
    }

    // 새로운 프레임 수신 시: 캡처 활성화 상태에서만 Mat 변환, 아니면 즉시 close
    override fun onFrameReceived(image: Any) {
        if (image is ImageProxy) {
            if (isCapturing) {
                val mat = castToMatInternal(image)
                image.close()
                if (mat != null && !mat.empty()) {
                    synchronized(this) {
                        latestFrameMat?.release()
                        latestFrameMat = mat
                    }
                } else {
                    mat?.release()
                }
            } else {
                image.close() // 분석 요청이 없으면 즉시 close
            }
        }
    }

    // 최신 프레임을 꺼내서 반환 (null 초기화 방지하여 프레임 재사용 지원)
    override fun acquireFrame(): Any? = synchronized(this) {
        val frame = latestFrameMat
        // 프레임 소유권을 넘길 때 원본을 비우지 않고,
        // 필요 시 clone()을 통해 소비자가 독립적으로 release 할 수 있게 제공합니다.
        // 현재 로직 파이프라인에서 Mat의 경우 명시적 복사가 필요합니다.
        return if (frame is Mat && !frame.empty()) frame.clone() else frame
    }

    // 캡처된 사진 처리 및 갤러리 저장
    @ExperimentalGetImage
    override suspend fun processCapturedPhoto(image: Any, eventData: CaptureEventEntity): String {
        val bitmap = when (image) {
            is Bitmap -> image
            is ImageProxy -> {
                val b = imageToBitmap(image.image!!, image.imageInfo.rotationDegrees)
                image.close()
                b
            }
            else -> throw IllegalArgumentException(ERROR_UNSUPPORTED_TYPE)
        }
        return fileHandleDataSource.saveImageToGallery(bitmap).toString()
    }

    // 모든 저장된 이미지 로드
    override suspend fun loadAllCapturedImages(): List<ImageResultEntity> {
        return fileHandleDataSource.loadCapturedImages(true).map { it.toDomain() }
    }

    // 이미지 삭제
    override suspend fun deleteCapturedImage(uri: String): Boolean {
        return fileHandleDataSource.deleteCapturedImage(Uri.parse(uri))
    }

    // 고정 화면용 가이드 라인 이미지(Canny Edge) 생성
    override suspend fun <T> getFixedImage(imageOrMat: Any): T {
        val mat = castToMatInternal(imageOrMat) ?: throw IllegalArgumentException(ERROR_UNSUPPORTED_TYPE)

        val edgeMat = Mat()
        Imgproc.cvtColor(mat, edgeMat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.Canny(edgeMat, edgeMat, 50.0, 150.0)
        
        val resultBitmap = matToBitmap(edgeMat)
        
        mat.release()
        edgeMat.release()
        
        return resultBitmap as T
    }

    // 마지막으로 저장된 이미지 URI 획득
    override suspend fun getLastSavedImageUri(): String? {
        return fileHandleDataSource.loadCapturedImages(true).lastOrNull()?.dataUri?.toString()
    }

    // 임의 객체를 OpenCV Mat 핸들로 변환
    override suspend fun convertToMat(image: Any): Any? = withContext(Dispatchers.IO) {
        castToMatInternal(image)
    }

    // Mat 자원 해제
    override fun releaseMat(mat: Any) {
        (mat as? Mat)?.release()
    }

    // --- 내부 이미지 처리 헬퍼 ---

    private fun castToMatInternal(image: Any): Mat? {
        return when (image) {
            is Mat -> if (image.empty()) null else image.clone()
            is Bitmap -> {
                if (image.isRecycled) return null
                val mat = Mat()
                Utils.bitmapToMat(image, mat)
                mat
            }
            is ImageProxy -> {
                val bitmap = imageToBitmap(image.image!!, image.imageInfo.rotationDegrees)
                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat)
                mat
            }
            is String -> { // URI 문자열
                 try {
                    val uri = Uri.parse(image)
                    val stream = context.contentResolver.openInputStream(uri)
                    if (stream != null) {
                        val bitmap = BitmapFactory.decodeStream(stream)
                        stream.close()
                        val mat = Mat()
                        Utils.bitmapToMat(bitmap, mat)
                        mat
                    } else null
                } catch (e: Exception) {
                    android.util.Log.e("ImageRepository", "Error loading image from URI: $image", e)
                    null
                }
            }
            else -> null
        }
    }

    private fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    private fun imageToBitmap(image: Image, imageRotation: Int): Bitmap {
        var res: Bitmap? = null
        when (image.format) {
            ImageFormat.YUV_420_888 -> {
                val yBuffer = image.planes[0].buffer
                val uBuffer = image.planes[1].buffer
                val vBuffer = image.planes[2].buffer
                val nv21 = ByteArray(yBuffer.remaining() + uBuffer.remaining() + vBuffer.remaining())
                yBuffer.get(nv21, 0, yBuffer.remaining())
                vBuffer.get(nv21, yBuffer.remaining(), vBuffer.remaining())
                uBuffer.get(nv21, yBuffer.remaining() + vBuffer.remaining(), uBuffer.remaining())

                val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
                val out = ByteArrayOutputStream()
                yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
                res = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
            }
            ImageFormat.JPEG -> {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
                res = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }

        if (imageRotation != 0 && res != null) {
            res = Bitmap.createBitmap(
                res, 0, 0, res.width, res.height,
                Matrix().apply { postRotate(imageRotation.toFloat()) }, true
            )
        }
        return res!!
    }
}
