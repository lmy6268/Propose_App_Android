package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.data.model.UserDto

interface UserDataSource {
    suspend fun saveUserSet(userSet: UserDto)
    suspend fun loadUserSet(): UserDto

    suspend fun saveUserSuccessToTermOfUse()
    suspend fun checkUserSuccessToTermOfUse(): Boolean

}