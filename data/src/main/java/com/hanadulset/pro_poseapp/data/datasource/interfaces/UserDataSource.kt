package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.domain.model.UserSetModel

interface UserDataSource {
    suspend fun saveUserSet(userSet: UserSetModel)
    suspend fun loadUserSet(): UserSetModel

    suspend fun saveUserSuccessToTermOfUse()
    suspend fun checkUserSuccessToTermOfUse(): Boolean

}