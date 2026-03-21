package com.hanadulset.pro_poseapp.data.datasource.impls

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.hanadulset.pro_poseapp.data.datasource.interfaces.FAAnalyticsDataSource
import javax.inject.Inject

import dagger.hilt.android.qualifiers.ApplicationContext

class FAAnalyticsDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FAAnalyticsDataSource {

    private val contentResolver = context.contentResolver

    @SuppressLint("HardwareIds")
    private val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    private val isOnDebug = true // 실제로는 BuildConfig 등을 사용해 설정 가능

    override fun logEvent(eventName: String, params: Map<String, Any>?) {
        val finalParams = HashMap<String, Any>()
        finalParams["deviceID"] = deviceID
        finalParams["timestamp"] = System.currentTimeMillis()
        
        params?.forEach { (key, value) ->
            if (value is String && value.length > 99) {
                value.chunked(99).forEachIndexed { index, s ->
                    finalParams["${key}_$index"] = s
                }
            } else {
                finalParams[key] = value
            }
        }

        // 실제 운영 환경에서는 FirebaseAnalytics.getInstance(context).logEvent(eventName, finalParams.toBundle())
        if (isOnDebug) {
            Log.d("FAAnalytics", "Event: $eventName, UserID: $deviceID, Params: $finalParams")
        }
    }
}
