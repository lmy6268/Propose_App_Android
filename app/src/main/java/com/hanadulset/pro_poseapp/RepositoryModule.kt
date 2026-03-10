package com.hanadulset.pro_poseapp

import com.hanadulset.pro_poseapp.data.repository.AIRepositoryImpl
import com.hanadulset.pro_poseapp.data.repository.AnalyticsRepositoryImpl
import com.hanadulset.pro_poseapp.data.repository.ImageRepositoryImpl
import com.hanadulset.pro_poseapp.data.repository.UserRepositoryImpl
import com.hanadulset.pro_poseapp.domain.repository.AIRepository
import com.hanadulset.pro_poseapp.domain.repository.AnalyticsRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindImageRepository(imageRepositoryImpl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(aiRepositoryImpl: AIRepositoryImpl): AIRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(analyticsRepositoryImpl: AnalyticsRepositoryImpl): AnalyticsRepository
}
