package com.hanadulset.pro_poseapp.build_logic.convention

// Matches with libs.versions.toml and convention plugin call ids
internal object ConventionRes {

    object Plugin {
        const val ANDROID_APPLICATION = "android.application"
        const val ANDROID_LIBRARY = "android.library"
        const val KOTLIN_ANDROID = "jetbrains.kotlin.android"
        const val KOTLIN_JVM = "jetbrains.kotlin.jvm"
        const val HILT = "dagger.hilt"
        const val KSP = "google.devtool.ksp"
        const val ROOM = "androidx.room"
        const val ANDROID_LIBRARY_CONVENTION = "propose.android.library"
        const val HILT_CONVENTION = "propose.android.hilt"
    }

    object Library {
        const val APPLICATION_ID = "com.hanadulset.pro_poseapp"
        const val HILT_ANDROID = "hilt.android"
        const val HILT_COMPILER = "hilt.compiler"

        const val ROOM_RUNTIME = "room.runtime"
        const val ROOM_KTX = "room.ktx"
        const val ROOM_COMPILER = "room.compiler"

        const val COMPOSE_COMPILER = "compose.compiler"
        const val COMPOSE_HILT_NAVIGATION = "androidx.hilt.navigation.compose"
        const val COMPOSE_RUNTIME = "androidx.lifecycle.runtimeCompose"
        const val COMPOSE_VIEWMODEL = "androidx.lifecycle.viewModelCompose"
        const val COMPOSE_BOM = "androidx.compose.composeBom"
        const val COMPOSE_UI_TOOLING = "androidx-compose-uiTooling"
        const val COMPOSE_UI_TOOLING_PREVIEW = "androidx-compose-uiToolingPreview"
    }
}