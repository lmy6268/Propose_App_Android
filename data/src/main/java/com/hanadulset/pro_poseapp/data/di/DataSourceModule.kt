package com.hanadulset.pro_poseapp.data.di

import com.hanadulset.pro_poseapp.data.datasource.FileHandleDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ImageProcessDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ModelRunnerDataSourceDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.feature.CompDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.feature.PoseDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.interfaces.CompDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.FileHandleDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ImageProcessDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ModelRunnerDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.PoseDataSource
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
    abstract fun bindModelRunnerDataSource(impl: ModelRunnerDataSourceDataSourceImpl): ModelRunnerDataSource

    @Binds
    @Singleton
    abstract fun bindPoseDataSource(impl: PoseDataSourceImpl): PoseDataSource

    @Binds
    @Singleton
    abstract fun bindImageProcessDataSource(impl: ImageProcessDataSourceImpl): ImageProcessDataSource

    @Binds
    @Singleton
    abstract fun bindFileHandleDataSource(impl: FileHandleDataSourceImpl): FileHandleDataSource

    @Binds
    @Singleton
    abstract fun bindCompDataSource(impl: CompDataSourceImpl): CompDataSource
}
