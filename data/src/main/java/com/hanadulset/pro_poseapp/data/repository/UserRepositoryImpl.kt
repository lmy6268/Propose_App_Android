package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import com.hanadulset.pro_poseapp.data.datasource.impls.UserDataSourceImpl
import com.hanadulset.pro_poseapp.data.mapper.toDomain
import com.hanadulset.pro_poseapp.data.mapper.toDto
import com.hanadulset.pro_poseapp.data.datasource.interfaces.UserDataSource
import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.domain.repository.AnalyticsRepository
import com.hanadulset.pro_poseapp.domain.model.UserEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource,
    private val analyticsRepository: AnalyticsRepository
) : UserRepository {


    override suspend fun loadUserSet(): UserEntity = userDataSource.loadUserSet().toDomain()

    override suspend fun saveUserSet(userSet: UserEntity) = userDataSource.saveUserSet(userSet.toDto())
    override suspend fun saveUserSuccessToTermOfUse() {
        userDataSource.saveUserSuccessToTermOfUse()
        analyticsRepository.saveUserAgreeToUseEvent()
    }

    override suspend fun checkUserSuccessToTermOfUse() =
        userDataSource.checkUserSuccessToTermOfUse()
}