package com.hanadulset.pro_poseapp.ui

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.hanadulset.pro_poseapp.core.designsystem.theme.ProPoseTheme
import com.hanadulset.pro_poseapp.ui.core.MainScreen
import com.hanadulset.pro_poseapp.ui.utils.eventlog.AnalyticsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val analyticsManager by lazy { AnalyticsManager(this.contentResolver) }

    //전체화면 적용

    private fun setFullScreen() {
        actionBar?.hide()
        // WindowInsetsControllerCompat를 사용하면 버전 분기 없이 처리가 가능합니다.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            // 상태바 숨기기
            hide(WindowInsetsCompat.Type.statusBars())
            // 스와이프 시 일시적으로 시스템바가 나타나도록 설정 (IMMERSIVE_STICKY와 유사)
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun initActivity() {
        setFullScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED //회전 고정
        analyticsManager.saveAppOpenEvent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        initActivity()

        setContent {
            val navController = rememberNavController()  //화면 네비게이션 기능을 관리하는 컨트롤러

            ProPoseTheme {
                MainScreen.MainScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(), //시스템의 네비게이션 높이에 맞게 패딩을 적용할 수 있게 함.
                    navController,
                )
            }
        }

    }

    override fun onStop() {
        super.onStop()
        //앱 종료 이벤트 발생
        analyticsManager.saveAppClosedEvent()
    }


}