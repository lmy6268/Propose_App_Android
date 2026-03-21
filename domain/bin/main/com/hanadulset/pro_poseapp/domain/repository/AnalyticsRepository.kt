package com.hanadulset.pro_poseapp.domain.repository

import com.hanadulset.pro_poseapp.domain.model.CaptureEventEntity

interface AnalyticsRepository {
    fun saveAppOpenEvent()
    fun saveAppClosedEvent()
    fun saveUserAgreeToUseEvent()
    fun saveCapturedEvent(
        captureEventData: CaptureEventEntity,
        estimationResult: List<Triple<Float, Float, Float>?>
    )
}
