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
    val itemNumber: String,
    val description: String
)

class ItemsListViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = StockSettingsStore(app)
    private val uiState = MutableStateFlow(ItemsListUiState(isLoading = true))
    val state: StateFlow<ItemsListUiState> = uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)
            val settings: StockSettings = settingsStore.settingsFlow.first()

            val api = StockApiFactory.create(settings)
            val repo = StockRepository(api)

            runCatching {
                val total = repo.loadItemsTotalPages(filter = uiState.value.filter.ifBlank { null })
                val page = uiState.value.page.coerceIn(1, maxOf(1, total))
                val items = repo.loadItemsPage(page = page, filter = uiState.value.filter.ifBlank { null })
                uiState.value.copy(
                    isLoading = false,
                    totalPages = maxOf(1, total),
                    page = page,
                    items = items.map {
                        ItemRowUi(
                            itemNumber = it.itemNumber.orEmpty(),
                            description = it.description.orEmpty()
                        )
                    }
                )
            }.onSuccess { newState ->
                uiState.value = newState
            }.onFailure { e ->
                uiState.value = uiState.value.copy(isLoading = false, error = e.message ?: "Erreur réseau")
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
