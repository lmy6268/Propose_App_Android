package com.hanadulset.pro_poseapp.ui.core

import androidx.camera.view.PreviewView
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.hanadulset.pro_poseapp.ui.feature.camera.CameraViewModel
import com.hanadulset.pro_poseapp.ui.feature.gallery.GalleryViewModel
import com.hanadulset.pro_poseapp.ui.feature.splash.PrepareServiceViewModel

object MainScreen {
    private val PERMISSIONS_REQUIRED = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    } else {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    }

    @Composable
    fun MainScreen(
        modifier: Modifier = Modifier,
        navHostController: NavHostController,
    ) {
        Surface(modifier = modifier) {
            ContainerView(navController = navHostController)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    @Composable
    private fun ContainerView(
        navController: NavHostController,
        cameraViewModel: CameraViewModel = hiltViewModel(),
        prepareServiceViewModel: PrepareServiceViewModel = hiltViewModel(),
        galleryViewModel: GalleryViewModel = hiltViewModel()
    ) {
        val multiplePermissionsState = rememberMultiplePermissionsState(permissions = PERMISSIONS_REQUIRED.toList()) {}
        val isPermissionAllowed = multiplePermissionsState.allPermissionsGranted
        
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        
        val previewView = rememberUpdatedState(newValue = PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        })

        val cameraInit = {
            cameraViewModel.bindCameraToLifeCycle(
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = previewView.value.surfaceProvider,
                previewRotation = previewView.value.rotation.toInt()
            )
        }

        // NavHost 로직을 MainNavHost로 완전히 위임
        MainNavHost(
            navController = navController,
            isPermissionAllowed = isPermissionAllowed,
            multiplePermissionsState = multiplePermissionsState,
            cameraViewModel = cameraViewModel,
            prepareServiceViewModel = prepareServiceViewModel,
            galleryViewModel = galleryViewModel,
            cameraInit = cameraInit,
            previewView = { previewView.value }
        )
    }
}
