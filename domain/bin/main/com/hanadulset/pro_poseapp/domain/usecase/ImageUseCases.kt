package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.repository.AIRepository
import com.hanadulset.pro_poseapp.domain.usecase.image.CaptureImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.image.GetLatestImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.image.ProcessImageFrameUseCase
import com.hanadulset.pro_poseapp.domain.usecase.image.ShowFixedScreenUseCase
import javax.inject.Inject

/**
 * 이미지 처리 및 분석과 관련된 UseCase들을 모아둔 허브 클래스입니다.
 * 기존의 CameraUseCases에서 이미지 처리 성격이 강해짐에 따라 이름을 변경하였습니다.
 */
data class ImageUseCases @Inject constructor(
    val captureImageUseCase: CaptureImageUseCase, // 이미지 캡처 및 저장
    val getLatestImageUseCase: GetLatestImageUseCase, // 최근 촬영 이미지 획득
    val showFixedScreenUseCase: ShowFixedScreenUseCase, // 고정 화면(가이드) 표시
    val processImageFrameUseCase: ProcessImageFrameUseCase // 실시간 프레임 분석 처리
)
