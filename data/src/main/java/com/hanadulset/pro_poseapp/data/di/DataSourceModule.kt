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
    abstract fun bindTrackingDataSource(
        trackingDataSourceImpl: TrackingDataSourceImpl
    ): TrackingDataSource

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
    @Binds
    @Singleton
    abstract fun bindFAAnalyticsDataSource(
        faAnalyticsDataSourceImpl: FAAnalyticsDataSourceImpl
    ): FAAnalyticsDataSource

    @Binds
    @Singleton
    abstract fun bindUserDataSource(
        userDataSourceImpl: UserDataSourceImpl
    ): UserDataSource

    @Binds
    @Singleton
    abstract fun bindNetworkDataSource(
        networkDataSourceImpl: NetworkDataSourceImpl
    ): NetworkDataSource


}
