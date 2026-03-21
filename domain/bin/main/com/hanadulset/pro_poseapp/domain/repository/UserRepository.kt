package com.hanadulset.pro_poseapp.domain.repository

import com.hanadulset.pro_poseapp.domain.model.UserEntity
import kotlinx.coroutines.flow.Flow

//사용자 흔적을 기록하는 역할을 담당하는 레포지토리
interface UserRepository {
    suspend fun loadUserSet(): UserEntity
    suspend fun saveUserSet(userSet: UserEntity)
    suspend fun saveUserSuccessToTermOfUse()
    suspend fun checkUserSuccessToTermOfUse(): Boolean
}