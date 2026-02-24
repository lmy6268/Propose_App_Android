package com.hanadulset.pro_poseapp.domain.model.wrapper

/**
 * Domain 모듈이 Android 프레임워크에 직접 의존하지 않도록
 * Android 타입을 감싸는 래퍼 인터페이스 모음.
 *
 * 각 래퍼의 실제 구현체는 Data/UI 레이어의 WrapperMapper.kt에 정의됩니다.
 */

// ── 기존 래퍼 ──────────────────────────────────────────

/** android.graphics.Bitmap 래퍼 */
interface ProposeImage

/** android.net.Uri 래퍼 */
interface ProposeUri

/** android.util.SizeF 래퍼 */
interface ProposeSize {
    val width: Float
    val height: Float
}

// ── 카메라 래퍼 ────────────────────────────────────────

/** androidx.lifecycle.LifecycleOwner 래퍼 */
interface ProposeLifecycleOwner

/** androidx.camera.core.Preview.SurfaceProvider 래퍼 */
interface ProposeSurfaceProvider

/** androidx.camera.core.ImageAnalysis.Analyzer 래퍼 */
interface ProposeAnalyzer

/** androidx.camera.core.MeteringPoint 래퍼 */
interface ProposeMeteringPoint
