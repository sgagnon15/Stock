package com.sergeapps.stock.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.stock.data.StockApiFactory
import com.sergeapps.stock.data.StockRepository
import com.sergeapps.stock.data.StockSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val itemId: Int? = null,

    val itemNumber: String = "",
    val description: String = "",
    val vendor: String = "",
    val manufacturer: String = "",
    val uom: String = "",
    val barcode: String = "",
    val minLevel: String = "",
    val maxLevel: String = "",
    val vendorUrl: String = "",

    val imageUrl: String? = null,

    // --- PHOTO ---
    val localSelectedPhotoUri: Uri? = null,
    val isUploadingPhoto: Boolean = false
)

class ItemDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = StockSettingsStore(app)
    private var repository: StockRepository? = null

    private val uiState = MutableStateFlow(ItemDetailUiState())
    val state: StateFlow<ItemDetailUiState> = uiState.asStateFlow()

    fun load(itemId: Int) {
        viewModelScope.launch {
            uiState.value = ItemDetailUiState(isLoading = true)
            uiState.value = ItemDetailUiState(isLoading = true, error = "load($itemId) appelé")

            val settings = settingsStore.settingsFlow.first()
            val api = StockApiFactory.create(settings)
            repository = StockRepository(api)

            runCatching {
                repository!!.loadItemDetail(itemId)

            }.onSuccess { dto ->
                uiState.value = ItemDetailUiState(
                    isLoading = false,
                    itemId = itemId,
                    itemNumber = dto.itemNumber.toString(),
                    description = dto.description,
                    vendor = dto.vendor.orEmpty(),
                    manufacturer = dto.manufacturer.orEmpty(),
                    uom = dto.uom.orEmpty(),
                    barcode = dto.barcode.orEmpty(),
                    minLevel = dto.minLevel.orEmpty(),
                    maxLevel = dto.maxLevel.orEmpty(),
                    vendorUrl = dto.vendorUrl.orEmpty(),
                    imageUrl = dto.url
                )
            }.onFailure { e ->
                uiState.value = ItemDetailUiState(
                    isLoading = false,
                    error = e.message ?: "Erreur réseau"
                )
            }
        }
    }

    fun onPickPhoto(uri: Uri) {
        uiState.update {
            it.copy(localSelectedPhotoUri = uri)
        }
    }

    fun uploadPickedPhoto(context: Context) {
        val itemId = uiState.value.itemId ?: return
        val uri = uiState.value.localSelectedPhotoUri ?: return
        val repo = repository ?: return

        viewModelScope.launch {
            uiState.update { it.copy(isUploadingPhoto = true) }

            runCatching {
                repo.uploadPhoto(context, itemId, uri)
            }.onSuccess { dto ->
                val response = repo.uploadPhoto(context, itemId, uri)

                if (response.ok && !response.url.isNullOrBlank()) {
                    uiState.update {
                        it.copy(
                            imageUrl = response.url,
                            localSelectedPhotoUri = null,
                            isUploadingPhoto = false
                        )
                    }
                } else {
                    uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            error = response.error ?: "Upload échoué"
                        )
                    }
                }            }.onFailure { e ->
                uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        error = e.message ?: "Erreur upload photo"
                    )
                }
            }
        }
    }

    fun deletePhoto() {
        val itemId = uiState.value.itemId ?: return
        val pictureUrl = uiState.value.imageUrl ?: return
        val repo = repository ?: return

        viewModelScope.launch {
            uiState.update { it.copy(isUploadingPhoto = true, error = null) }

            runCatching {
                repo.deletePhoto(itemId, pictureUrl)
            }.onSuccess {
                uiState.update {
                    it.copy(
                        imageUrl = null,
                        isUploadingPhoto = false
                    )
                }
            }.onFailure { e ->
                uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        error = e.message ?: "Erreur suppression photo"
                    )
                }
            }
        }
    }
}
