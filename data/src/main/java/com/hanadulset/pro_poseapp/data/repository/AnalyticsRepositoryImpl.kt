package com.hanadulset.pro_poseapp.data.repository

import com.hanadulset.pro_poseapp.data.datasource.interfaces.FAAnalyticsDataSource
import com.hanadulset.pro_poseapp.domain.model.CaptureEventEntity
import com.hanadulset.pro_poseapp.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val faAnalyticsDataSource: FAAnalyticsDataSource
) : AnalyticsRepository {

    override fun saveAppOpenEvent() {
        faAnalyticsDataSource.logEvent("EVENT_APP_OPEN")
    }

    override fun saveAppClosedEvent() {
        faAnalyticsDataSource.logEvent("EVENT_APP_CLOSE")
    }

    override fun saveUserAgreeToUseEvent() {
        faAnalyticsDataSource.logEvent(
            "EVENT_SUCCESS_TO_USE",
            mapOf("user_answer" to true.toString())
        )
    }

    override fun saveCapturedEvent(
        captureEventData: CaptureEventEntity,
        estimationResult: List<Triple<Float, Float, Float>?>
    ) {
        val params = HashMap<String, Any>()
        captureEventData.run {
            params["poseID"] = poseID.toDouble()
            params["backgroundId"] = backgroundId?.toDouble() ?: -1.0
            params["prevRecommendPoses"] = prevRecommendPoses.toString()
            params["backgroundHog"] = backgroundHog.toString()
            params["human_pose_estimation"] = estimationResult.toString()
        }
        faAnalyticsDataSource.logEvent("EVENT_CAPTURE", params)
    }
}
