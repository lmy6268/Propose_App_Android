package com.hanadulset.pro_poseapp.presentation.feature.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.GalleryUseCases
import com.hanadulset.pro_poseapp.utils.camera.ImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val galleryUseCases: GalleryUseCases
) : ViewModel() {
    private val _capturedImageState = MutableStateFlow<List<ImageResult>?>(null)
    val capturedImageState = _capturedImageState.asStateFlow()
    private val _deleteCompleteState = MutableStateFlow<Boolean?>(null)
    val deleteCompleteState = _deleteCompleteState.asStateFlow()


    fun loadImages() {
        _capturedImageState.value = null
        viewModelScope.launch {
            _capturedImageState.value = galleryUseCases.getImagesFromPicturesUseCase()
        }
    }

    fun deleteImage(index: Int, isOnDialog: Boolean) {
        _deleteCompleteState.value = false
        viewModelScope.launch {
            val checkState =
                if (isOnDialog.not()) galleryUseCases.deleteImageFromPicturesUseCase(uri = _capturedImageState.value!![index].dataUri!!)
                else isOnDialog
            if (checkState) {
                _capturedImageState.update {
                    val updatedList = it!!.toMutableList().apply {
                        removeAt(index)
                    }.toList()
                    updatedList
                }
                _deleteCompleteState.value = true
            }
        }
    }
}
