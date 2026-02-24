package com.hanadulset.pro_poseapp.data.datasource.feature

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.data.datasource.interfaces.CompDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ModelRunnerDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CompDataSourceImpl @Inject constructor(private val modelRunner: ModelRunnerDataSource) :
    CompDataSource {
    override suspend fun recommendCompData(backgroundBitmap: Bitmap): Pair<Float, Float> =
        withContext(Dispatchers.Default) { modelRunner.runVapNet(backgroundBitmap) }
}
