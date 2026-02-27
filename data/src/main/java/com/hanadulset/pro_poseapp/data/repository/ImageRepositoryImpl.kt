package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.SizeF
import androidx.core.graphics.scale
import com.hanadulset.pro_poseapp.data.datasource.FileHandleDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ImageProcessDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ModelRunnerDataSourceDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.feature.CompDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.feature.PoseDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.utils.camera.ImageResult
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(@param:ApplicationContext private val applicationContext: Context) :
    ImageRepository {
    private val modelRunnerImpl by lazy {
        ModelRunnerDataSourceDataSourceImpl(applicationContext)
    }

    private val poseDataSourceImpl by lazy {
        PoseDataSourceImpl(applicationContext)
    }

    private val imageProcessDataSource by lazy {
        ImageProcessDataSourceImpl()
    }

    private val fileHandleDataSource by lazy {
        FileHandleDataSourceImpl(applicationContext)
    }

    private val compDataSource by lazy {
        CompDataSourceImpl(modelRunnerImpl)
    }


    override suspend fun getRecommendCompInfo(backgroundBitmap: Bitmap) =
        withContext(Dispatchers.Default) {
            compDataSource.recommendCompData(backgroundBitmap)
        }


    override suspend fun getRecommendPose(
        backgroundBitmap: Bitmap
    ): PoseDataResult = withContext(Dispatchers.Default) {
        poseDataSourceImpl.recommendPose(backgroundBitmap)
    }


    override suspend fun getFixedScreen(backgroundBitmap: Bitmap): Bitmap =
        withContext(Dispatchers.Default) {
            imageProcessDataSource.getFixedImage(bitmap = backgroundBitmap)
        }


    override suspend fun getLatestImage(): Uri? = withContext(Dispatchers.IO) {
        val data = fileHandleDataSource.loadCapturedImages(false)
        if (data.isEmpty()) null
        else data[0].dataUri
    }


    override suspend fun preRunModel(): Boolean {
        poseDataSourceImpl.preparePoseData()
        return modelRunnerImpl.preRun()
    }

    //이미지에서 포즈를 가져오기
    override suspend fun getPoseFromImage(uri: Uri?): Bitmap? = withContext(Dispatchers.IO) {
        uri?.let { targetUri ->
            val contentResolver = applicationContext.contentResolver
            val backgroundBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, targetUri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, targetUri)
            }

            backgroundBitmap.copy(Bitmap.Config.RGB_565, true).let { source ->
                val is16By9 = source.width / source.height.toFloat() == 9 / 16F
                val (w, h) = if (is16By9) 720 to 1280 else 480 to 640

                source.scale(w, h).let { scaled ->
                    getFixedScreen(scaled).also {
                        scaled.recycle()
                        source.recycle()
                        if (backgroundBitmap != source) backgroundBitmap.recycle()
                    }
                }
            }
        }
    }

    override suspend fun loadAllCapturedImages(): List<ImageResult> = withContext(Dispatchers.IO) {
        fileHandleDataSource.loadCapturedImages(true)
    }


    override suspend fun deleteCapturedImage(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        fileHandleDataSource.deleteCapturedImage(uri)
    }

    override suspend fun updateOffsetPoint(
        backgroundBitmap: Bitmap,
        targetOffset: SizeF
    ): SizeF? = withContext(Dispatchers.Default) {
        imageProcessDataSource.useOpticalFlow(
            targetOffset = targetOffset,
            bitmap = backgroundBitmap
        )
    }

    override fun stopPointOffset() {
        imageProcessDataSource.stopToUseOpticalFlow()
    }


}
