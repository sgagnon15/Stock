package com.sergeapps.stock.ui

import android.net.Uri
import com.sergeapps.stock.data.model.Item

data class ItemDetailUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,

    val localSelectedPhotoUri: Uri? = null,
    val isUploadingPhoto: Boolean = false,

    val errorMessage: String? = null
)
