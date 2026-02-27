package com.hanadulset.pro_poseapp.data.di

import com.hanadulset.pro_poseapp.data.datasource.impls.*
import com.hanadulset.pro_poseapp.data.datasource.interfaces.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindVisionProcessDataSource(
        visionProcessDataSourceImpl: VisionProcessDataSourceImpl
    ): VisionProcessDataSource

    @Binds
    @Singleton
    abstract fun bindCompDataSource(
        compDataSourceImpl: CompDataSourceImpl
    ): CompDataSource

    @Binds
    @Singleton
    abstract fun bindPoseDataSource(
        poseDataSourceImpl: PoseDataSourceImpl
    ): PoseDataSource

    @Binds
    @Singleton
    abstract fun bindFileHandleDataSource(
        fileHandleDataSourceImpl: FileHandleDataSourceImpl
    ): FileHandleDataSource
    
    // 기존에 있던 다른 데이터소스들도 여기에 함께 정의합니다.
    @Binds
    @Singleton
    abstract fun bindUserDataSource(
        userDataSourceImpl: UserDataSourceImpl
    ): UserDataSource

    @Binds
    @Singleton
    abstract fun bindCameraDataSource(
        cameraDataSourceImpl: CameraDataSourceImpl
    ): CameraDataSource
}
