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
import com.sergeapps.stock.data.ItemDetailDto
import kotlinx.coroutines.flow.update


data class VendorUi(val name: String)

data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val itemDetail: ItemDetailDto? = null,
    val itemId: Int? = null,

    // --- PHOTO ---
    val imageUrl: String? = null,
    val localSelectedPhotoUri: Uri? = null,
    val isUploadingPhoto: Boolean = false,
    val photoVersion: Long = 0L,
    val apiKey: String = "",
    val vendorText: String = "",
    val vendorOptions: List<VendorUi> = emptyList(),
    val isVendorLoading: Boolean = false,
    val manufacturerText: String = "",
    val isManufacturerLoading: Boolean = false,
    val manufacturerOptions: List<ManufUi> = emptyList()
)

data class ManufUi(val name: String)

class ItemDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = StockSettingsStore(app)
    private lateinit var repository: StockRepository


    private val uiState = MutableStateFlow(ItemDetailUiState())
    val state: StateFlow<ItemDetailUiState> = uiState.asStateFlow()

    fun load(itemId: Int) {
        viewModelScope.launch {
            uiState.value = ItemDetailUiState(isLoading = true)

            val settings = settingsStore.settingsFlow.first()
            val api = StockApiFactory.create(settings)
            repository = StockRepository(api)

            runCatching {
                repository.loadItemDetail(itemId)
            }.onSuccess { dto ->
                uiState.value = ItemDetailUiState(
                    isLoading = false,
                    itemId = itemId,
                    itemDetail = dto,
                    imageUrl = dto.url,
                    apiKey = settings.apiKey,
                    vendorText = dto.vendor.orEmpty(),
                    manufacturerText = dto.manufacturer.orEmpty() // 👈 AJOUT ICI
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

        viewModelScope.launch {
            uiState.update { it.copy(isUploadingPhoto = true, error = null) }

            runCatching {
                repository.uploadPhoto(context, itemId, uri)
            }.onSuccess { response ->
                if (response.ok && !response.url.isNullOrBlank()) {
                    uiState.update {
                        it.copy(
                            imageUrl = response.url,
                            localSelectedPhotoUri = null,
                            isUploadingPhoto = false,
                            error = null,
                            photoVersion = System.currentTimeMillis()
                        )
                    }
                } else {
                    uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            error = response.error ?: "Upload échoué"
                        )
                    }
                }
            }.onFailure { e ->
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
                        localSelectedPhotoUri = null,
                        isUploadingPhoto = false,
                        error = null,
                        photoVersion = System.currentTimeMillis()
                    )
                }
            }.onFailure { e ->
                uiState.update {
                    it.copy(
                        imageUrl = null,
                        localSelectedPhotoUri = null,
                        isUploadingPhoto = false
                    )
                }
            }
        }
    }

    fun onVendorTextChanged(text: String) {
        uiState.value = uiState.value.copy(vendorText = text)
        refreshVendorOptions(text)
    }

    private fun refreshVendorOptions(text: String) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isVendorLoading = true)

            val rows = repository.loadVendors(
                nbItems = 9999,
                pageNumber = 1
            )

            uiState.value = uiState.value.copy(
                isVendorLoading = false,
                vendorOptions = rows.map { VendorUi(it.description) }
            )
        }
    }

    fun refreshManufacturers(pageNumber: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isManufacturerLoading = true, error = null) }

            runCatching {
                repository.fetchManufacturers(nbItems = 9999, pageNumber = pageNumber)
            }.onSuccess { names ->
                uiState.update {
                    it.copy(
                        manufacturerOptions = names.map { name ->
                            ManufUi(name = name)
                        },
                        isManufacturerLoading = false
                    )
                }
            }.onFailure { e ->
                android.util.Log.e("MANUF", "refreshManufacturers failed", e)
                uiState.update {
                    it.copy(
                        isManufacturerLoading = false,
                        error = e.message ?: "Erreur manufList"
                    )
                }
            }
        }
    }

    fun onVendorSelected(vendor: String) {
        uiState.value = uiState.value.copy(vendorText = vendor, vendorOptions = emptyList())
        // plus tard: marquer l’item comme modifié / sauvegarder
    }

    fun onVendorOpen() {
        refreshVendorOptions(uiState.value.vendorText)
    }

    fun onManufacturerTextChanged(value: String) {
        uiState.update { it.copy(manufacturerText = value) }
    }

    fun onManufacturerOpen() {
        refreshManufacturers(pageNumber = 1)
    }

    fun onManufacturerSelected(value: String) {
        uiState.update {
            it.copy(
                manufacturerText = value,
                manufacturerOptions = emptyList()
            )
        }
    }
}




