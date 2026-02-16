package com.hanadulset.pro_poseapp

import android.app.Application

import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader


@HiltAndroidApp
class ProPoseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OpenCVLoader.initLocal()
    }
}