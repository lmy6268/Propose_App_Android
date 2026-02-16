package com.hanadulset.pro_poseapp.presentation.core

import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.hanadulset.pro_poseapp.presentation.component.AnimatedSlideToRight
import com.hanadulset.pro_poseapp.presentation.core.permission.PermScreen
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraViewModel
import com.hanadulset.pro_poseapp.presentation.feature.camera.Screen
import com.hanadulset.pro_poseapp.presentation.feature.gallery.GalleryScreen
import com.hanadulset.pro_poseapp.presentation.feature.gallery.GalleryViewModel
import com.hanadulset.pro_poseapp.presentation.feature.setting.SettingScreen
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceScreens
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceViewModel
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class Page {
    Perm, AppUseAgreement, Cam, Setting, Splash, AppLoading, Images
}

enum class Graph {
    NotPermissionAllowed, PermissionAllowed, UsingCamera, DownloadProcess
}

object ProPoseTransition {
    val slideInToStart: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(300)
        )
    }

    val slideOutToEnd: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(300)
        )
    }

    val fastSlideInToStart: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(150)
        )
    }

    val fastSlideOutToEnd: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(150)
        )
    }

    val appLoadingExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(300, easing = LinearEasing)) + slideOutOfContainer(
            animationSpec = tween(300, easing = EaseOut),
            towards = AnimatedContentTransitionScope.SlideDirection.End
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun MainNavHost(
    navController: NavHostController,
    isPermissionAllowed: Boolean,
    multiplePermissionsState: MultiplePermissionsState,
    cameraViewModel: CameraViewModel,
    prepareServiceViewModel: PrepareServiceViewModel,
    galleryViewModel: GalleryViewModel,
    cameraInit: () -> Unit,
    previewView: () -> PreviewView
) {
    val previewState = cameraViewModel.previewState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isPermissionAllowed) Graph.PermissionAllowed.name else Graph.NotPermissionAllowed.name
    ) {
        notPermissionAllowGraph(
            navHostController = navController,
            prepareServiceViewModel = prepareServiceViewModel,
            multiplePermissionsState = multiplePermissionsState,
            cameraInit = cameraInit,
            previewState = previewState,
        )
        permissionAllowedGraph(
            navHostController = navController,
            cameraInit = cameraInit,
            previewState = previewState,
        )
        usingCameraGraph(
            navHostController = navController,
            galleryViewModel = galleryViewModel,
            previewView = previewView,
            cameraInit = cameraInit,
            cameraViewModel = cameraViewModel
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun NavGraphBuilder.notPermissionAllowGraph(
    navHostController: NavHostController,
    prepareServiceViewModel: PrepareServiceViewModel,
    multiplePermissionsState: MultiplePermissionsState,
    cameraInit: () -> Unit,
    previewState: State<CameraState>,
) {
    navigation(startDestination = Page.Splash.name, route = Graph.NotPermissionAllowed.name) {
        runSplashScreen(navHostController = navHostController, moveToNext = {
            if (it.not()) {
                navHostController.navigate(route = Page.AppUseAgreement.name) {
                    popUpTo(Page.Splash.name) { inclusive = true }
                }
            } else
                navHostController.navigate(route = Page.Perm.name) {
                    popUpTo(Page.Splash.name) { inclusive = true }
                }
        })
        runAppLoadingScreen(
            navHostController = navHostController,
            cameraInit = cameraInit,
            previewState = previewState
        )
        composable(route = Page.AppUseAgreement.name) {
            AppUseAgreementScreen.AppUseAgreementScreen {
                prepareServiceViewModel.successToUse()
                navHostController.navigate(route = Page.Perm.name) {
                    popUpTo(Page.Splash.name) { inclusive = true }
                }
            }
        }
        composable(route = Page.Perm.name) {
            PermScreen.PermScreen(multiplePermissionsState = multiplePermissionsState,
                permissionAllowed = {
                    navHostController.navigate(route = Graph.UsingCamera.name)
                })
        }
    }
}

private fun NavGraphBuilder.permissionAllowedGraph(
    navHostController: NavHostController,
    previewState: State<CameraState>,
    cameraInit: () -> Unit,
) {
    navigation(startDestination = Page.Splash.name, route = Graph.PermissionAllowed.name) {
        runSplashScreen(navHostController = navHostController, moveToNext = {
            navHostController.navigate(route = Page.AppLoading.name) {
                popUpTo(Page.Splash.name) { inclusive = true }
            }
        })
        runAppLoadingScreen(
            navHostController = navHostController,
            previewState = previewState,
            cameraInit = cameraInit,
        )
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun NavGraphBuilder.usingCameraGraph(
    navHostController: NavHostController,
    previewView: () -> PreviewView,
    cameraViewModel: CameraViewModel,
    galleryViewModel: GalleryViewModel,
    cameraInit: () -> Unit,
) {
    navigation(startDestination = Page.Cam.name, route = Graph.UsingCamera.name) {
        composable(route = Page.Cam.name,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }) {
            val isOnClose = remember { mutableStateOf(false) }
            val localActivity = LocalContext.current as Activity
            val userSet = cameraViewModel.userSetState.collectAsStateWithLifecycle()
            LaunchedEffect(key1 = userSet.value) {
                cameraViewModel.loadUserSet()
            }
            if (isOnClose.value.not()) {
                AnimatedSlideToRight(isVisible = userSet.value != null) {
                    Screen(cameraViewModel,
                        previewView = previewView,
                        onClickSettingBtnEvent = {
                            navHostController.navigate(route = Page.Setting.name) {
                                launchSingleTop = true
                            }
                        },
                        onClickGalleryBtn = {
                            navHostController.navigate(route = Page.Images.name) {}
                        },
                        cameraInit = cameraInit,
                        onFinishEvent = {
                            isOnClose.value = true
                            localActivity.finish()
                        },
                        userSet = { userSet.value!! })
                }
            }
        }

        composable(
            route = Page.Images.name,
            enterTransition = ProPoseTransition.slideInToStart,
            exitTransition = ProPoseTransition.slideOutToEnd
        ) {
            val imageList = galleryViewModel.capturedImageState.collectAsState()
            val context = LocalContext.current
            val deleteTargetIndex = remember { mutableStateOf<Int?>(null) }
            LaunchedEffect(Unit) { galleryViewModel.loadImages() }
            val coroutineScope = rememberCoroutineScope()
            val deleteLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = {
                    if (it.resultCode == Activity.RESULT_OK) {
                        galleryViewModel.deleteImage(deleteTargetIndex.value!!, true)
                        deleteTargetIndex.value = null
                    }
                })

            if (imageList.value != null) {
                GalleryScreen.GalleryScreen(imageList = imageList.value!!,
                    onDeleteImage = { index ->
                        coroutineScope.launch {
                            deleteTargetIndex.value = index
                            deleteImage(
                                context.contentResolver,
                                deleteLauncher,
                                galleryViewModel,
                                index = index,
                                uri = imageList.value!![index].dataUri!!,
                            )
                            galleryViewModel.deleteCompleteState.collectLatest {
                                it?.run { galleryViewModel.loadImages() }
                            }
                        }
                    },
                    onBackPressed = { navHostController.navigateUp() })
            }
        }

        composable(
            route = Page.Setting.name,
            enterTransition = ProPoseTransition.fastSlideInToStart,
            exitTransition = ProPoseTransition.fastSlideOutToEnd
        ) {
            val userSet by cameraViewModel.userSetState.collectAsStateWithLifecycle()
            LaunchedEffect(key1 = Unit, key2 = userSet) { cameraViewModel.loadUserSet() }
            AnimatedSlideToRight(isVisible = userSet != null) {
                SettingScreen.Screen(userSet = userSet!!, onSaveUserSet = { setting ->
                    cameraViewModel.saveUserSet(setting)
                }, onBackPressed = { navHostController.navigateUp() })
            }
        }
    }
}

private fun NavGraphBuilder.runSplashScreen(
    navHostController: NavHostController, moveToNext: (Boolean) -> Unit
) {
    composable(route = Page.Splash.name, enterTransition = {
        fadeIn(animationSpec = tween(300, easing = LinearEasing))
    }, exitTransition = {
        fadeOut(animationSpec = tween(300, easing = LinearEasing))
    }) {
        val prepareServiceViewModel = it.sharedViewModel<PrepareServiceViewModel>(navHostController = navHostController)
        val checkState = prepareServiceViewModel.checkUserSuccess.collectAsStateWithLifecycle()
        PrepareServiceScreens.SplashScreen { prepareServiceViewModel.checkToUse() }
        LaunchedEffect(checkState.value) {
            if (checkState.value != null) {
                delay(1000)
                moveToNext(checkState.value!!)
            }
        }
    }
}

private fun NavGraphBuilder.runAppLoadingScreen(
    navHostController: NavHostController,
    cameraInit: () -> Unit,
    previewState: State<CameraState>,
) {
    composable(route = Page.AppLoading.name, enterTransition = {
        fadeIn(animationSpec = tween(300, easing = LinearEasing))
    }, exitTransition = ProPoseTransition.appLoadingExit) {
        val prepareServiceViewModel = it.sharedViewModel<PrepareServiceViewModel>(navHostController = navHostController)
        val totalLoadedState = prepareServiceViewModel.totalLoadedState.collectAsState()

        PrepareServiceScreens.AppLoadingScreen(previewState = previewState,
            onAfterLoadedEvent = {
                navHostController.navigate(Graph.UsingCamera.name) {
                    popUpTo(route = Page.AppLoading.name) { inclusive = true }
                }
            },
            onPrepareToLoadCamera = {
                prepareServiceViewModel.preLoadModel()
                cameraInit()
            },
            totalLoadedState = { totalLoadedState.value })
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navHostController: NavHostController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) { navHostController.getBackStackEntry(navGraphRoute) }
    return hiltViewModel(parentEntry)
}

private fun deleteImage(
    contentResolver: ContentResolver,
    launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    galleryViewModel: GalleryViewModel,
    index: Int,
    uri: Uri,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        MediaStore.createDeleteRequest(contentResolver, arrayListOf(uri)).intentSender.run {
            launcher.launch(IntentSenderRequest.Builder(this).build())
        }
    } else {
        galleryViewModel.deleteImage(index, false)
    }
}
