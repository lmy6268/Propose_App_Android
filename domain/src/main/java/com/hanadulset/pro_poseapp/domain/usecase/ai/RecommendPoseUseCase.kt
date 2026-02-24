package com.hanadulset.pro_poseapp.domain.usecase.ai

import com.hanadulset.pro_poseapp.domain.model.pose.PoseDataResultModel
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeImage
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class RecommendPoseUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(backgroundImage: ProposeImage): PoseDataResultModel =
            repository.getRecommendPose(backgroundImage)
}
