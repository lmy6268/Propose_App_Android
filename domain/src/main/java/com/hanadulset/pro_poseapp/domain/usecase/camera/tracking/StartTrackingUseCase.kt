package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeImage
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeSize
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class StartTrackingUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(
        targetOffset: ProposeSize, backgroundImage: ProposeImage
    ): ProposeSize? {
        val duration =
            measureTimedValue { imageRepository.updateOffsetPoint(backgroundImage, targetOffset) }
        return duration.value
    }


}