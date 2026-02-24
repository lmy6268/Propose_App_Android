package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.SizeF
import androidx.core.graphics.scale
import com.hanadulset.pro_poseapp.data.datasource.interfaces.CompDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.FileHandleDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ImageProcessDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ModelRunnerDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.PoseDataSource
import com.hanadulset.pro_poseapp.data.mapper.toBitmap
import com.hanadulset.pro_poseapp.data.mapper.toDomainWrapper
import com.hanadulset.pro_poseapp.data.mapper.toSize
import com.hanadulset.pro_poseapp.data.mapper.toUri
import com.hanadulset.pro_poseapp.domain.model.camera.ImageResultModel
import com.hanadulset.pro_poseapp.domain.model.pose.PoseDataResultModel
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeImage
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeSize
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeUri
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ImageRepositoryImpl
@Inject
constructor(
        @param:ApplicationContext private val applicationContext: Context,
        private val modelRunnerDataSource: ModelRunnerDataSource,
        private val poseDataSource: PoseDataSource,
        private val imageProcessDataSource: ImageProcessDataSource,
        private val fileHandleDataSource: FileHandleDataSource,
        private val compDataSource: CompDataSource
) : ImageRepository {

    override suspend fun getRecommendCompInfo(backgroundImage: ProposeImage) =
            withContext(Dispatchers.IO) {
                compDataSource.recommendCompData(backgroundImage.toBitmap())
            }

    override suspend fun getRecommendPose(backgroundImage: ProposeImage): PoseDataResultModel =
            withContext(Dispatchers.IO) { poseDataSource.recommendPose(backgroundImage.toBitmap()) }

    override suspend fun getFixedScreen(backgroundImage: ProposeImage): ProposeImage =
            withContext(Dispatchers.IO) {
                imageProcessDataSource
                        .getFixedImage(bitmap = backgroundImage.toBitmap())
                        .toDomainWrapper()
            }

    override suspend fun getLatestImage(): ProposeUri? =
            withContext(Dispatchers.IO) {
                val data = fileHandleDataSource.loadCapturedImages(false)
                if (data.isEmpty()) null else data[0].dataUri.toDomainWrapper()
            }

    override suspend fun preRunModel(): Boolean {
        poseDataSource.preparePoseData()
        return modelRunnerDataSource.preRun()
    }

    // 이미지에서 포즈를 가져오기
    override suspend fun getPoseFromImage(uri: ProposeUri?): ProposeImage? =
            withContext(Dispatchers.IO) {
                uri?.let { targetUri ->
                    val actualUri = targetUri.toUri()
                    val contentResolver = applicationContext.contentResolver
                    val backgroundBitmap =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeBitmap(
                                        ImageDecoder.createSource(contentResolver, actualUri)
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                MediaStore.Images.Media.getBitmap(contentResolver, actualUri)
                            }

                    backgroundBitmap.copy(Bitmap.Config.RGB_565, true).let { source ->
                        val is16By9 = source.width / source.height.toFloat() == 9 / 16F
                        val (w, h) = if (is16By9) 720 to 1280 else 480 to 640

                        source.scale(w, h).let { scaled ->
                            getFixedScreen(scaled.toDomainWrapper()).also {
                                scaled.recycle()
                                source.recycle()
                                if (backgroundBitmap != source) backgroundBitmap.recycle()
                            }
                        }
                    }
                }
            }

    override suspend fun loadAllCapturedImages(): List<ImageResultModel> =
            withContext(Dispatchers.IO) { fileHandleDataSource.loadCapturedImages(true) }

    override suspend fun deleteCapturedImage(uri: ProposeUri): Boolean =
            withContext(Dispatchers.IO) { fileHandleDataSource.deleteCapturedImage(uri.toUri()) }

    override suspend fun updateOffsetPoint(
            backgroundImage: ProposeImage,
            targetOffset: ProposeSize
    ): ProposeSize? =
            withContext(Dispatchers.Default) {
                imageProcessDataSource.useOpticalFlow(
                                targetOffset = SizeF(targetOffset.width, targetOffset.height),
                                bitmap = backgroundImage.toBitmap()
                        )
                        ?.let { SizeF(it.width, it.height).toSize().toDomainWrapper() }
            }

    override fun stopPointOffset() {
        imageProcessDataSource.stopToUseOpticalFlow()
    }
}
