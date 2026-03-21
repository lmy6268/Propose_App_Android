package com.hanadulset.pro_poseapp.data.datasource.interfaces

interface FAAnalyticsDataSource {
    fun logEvent(eventName: String, params: Map<String, Any>? = null)
}
