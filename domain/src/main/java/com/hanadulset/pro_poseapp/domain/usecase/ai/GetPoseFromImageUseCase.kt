package com.hanadulset.pro_poseapp.domain.usecase.ai

import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeImage
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeProposeUri
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class GetPoseFromImageUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    suspend operator fun invoke(uri: ProposeUri?) = imageRepository.getPoseFromProposeImage(uri)
}