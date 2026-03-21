package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.model.CaptureEventEntity
import com.hanadulset.pro_poseapp.domain.repository.AnalyticsRepository
import javax.inject.Inject

class AnalyticsUseCases @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    fun saveAppOpenEvent() = analyticsRepository.saveAppOpenEvent()
    fun saveAppClosedEvent() = analyticsRepository.saveAppClosedEvent()

    fun saveCapturedEvent(
        captureEventData: CaptureEventEntity,
        estimationResult: List<Triple<Float, Float, Float>?>
    ) = analyticsRepository.saveCapturedEvent(captureEventData, estimationResult)
}
