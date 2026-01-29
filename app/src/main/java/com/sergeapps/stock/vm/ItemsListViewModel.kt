package com.sergeapps.stock.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.stock.data.StockApiFactory
import com.sergeapps.stock.data.StockRepository
import com.sergeapps.stock.data.StockSettings
import com.sergeapps.stock.data.StockSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ItemsListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: String = "",
    val page: Int = 1,
    val totalPages: Int = 1,
    val items: List<ItemRowUi> = emptyList()
)

data class ItemRowUi(
    val id: Int,
    val itemNumber: String,
    val description: String,
    val vendor: String,
    val manufacturer: String,
    val imageUrl: String?
)


class ItemsListViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = StockSettingsStore(app)
    private val uiState = MutableStateFlow(ItemsListUiState(isLoading = true))
    val state: StateFlow<ItemsListUiState> = uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)
            val settings: StockSettings = settingsStore.settingsFlow.first()

            runCatching {
                viewModelScope.launch {
                    uiState.value = uiState.value.copy(isLoading = true, error = "Début refresh()")

                    val settings = settingsStore.settingsFlow.first()
                    val api = StockApiFactory.create(settings)
                    val repo = StockRepository(api)

                    val nbItems = 10
                    val filter = uiState.value.filter.ifBlank { null }

                    // 1) nbpagesitem
                    val total = try {
                        uiState.value = uiState.value.copy(error = "Appel nbpagesitem…")
                        repo.loadItemsTotalPages(nbItems = nbItems, filter = filter)
                    } catch (e: Exception) {
                        uiState.value = uiState.value.copy(isLoading = false, error = "Erreur nbpagesitem: ${e.message}")
                        return@launch
                    }

                    val currentPage = uiState.value.page.coerceIn(1, maxOf(1, total))

                    // 2) itemlist
                    val items = try {
                        uiState.value = uiState.value.copy(error = "Appel itemlist…")
                        repo.loadItemsPage(page = currentPage, nbItems = nbItems)
                    } catch (e: Exception) {
                        uiState.value = uiState.value.copy(isLoading = false, error = "Erreur itemlist: ${e.message}")
                        return@launch
                    }

                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = null,
                        totalPages = maxOf(1, total),
                        page = currentPage,
                        items = items.map {
                            ItemRowUi(
                                id = it.id,
                                itemNumber = it.itemNumber.toString(),
                                description = it.description,
                                vendor = it.vendor.orEmpty(),
                                manufacturer = it.manufacturer.orEmpty(),
                                imageUrl = it.url
                            )
                        }
                    )
                }
            }
        }
    }

    fun setFilter(newFilter: String) {
        uiState.value = uiState.value.copy(filter = newFilter, page = 1)
    }

    fun nextPage() {
        val next = (uiState.value.page + 1).coerceAtMost(uiState.value.totalPages)
        uiState.value = uiState.value.copy(page = next)
        refresh()
    }

    fun prevPage() {
        val prev = (uiState.value.page - 1).coerceAtLeast(1)
        uiState.value = uiState.value.copy(page = prev)
        refresh()
    }
}
