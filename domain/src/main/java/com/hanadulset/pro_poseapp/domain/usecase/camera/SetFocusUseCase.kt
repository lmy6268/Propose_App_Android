package com.hanadulset.pro_poseapp.domain.usecase.camera

import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeMeteringPoint
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import javax.inject.Inject

class SetFocusUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    operator fun invoke(meteringPoint: ProposeMeteringPoint, durationMilliSeconds: Long) {
        cameraRepository.setFocus(meteringPoint, durationMilliSeconds)
    }
}