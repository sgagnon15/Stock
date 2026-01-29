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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.CancellationException

data class ManufDto(
    val pagenumber: String?,
    val description: String?
)

data class ManufUi(
    val name: String
)

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
    private val filterFlow = MutableStateFlow("")
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            filterFlow
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { newFilter ->
                    uiState.value = uiState.value.copy(filter = newFilter, page = 1)
                    refresh()
                }
        }
    }

    fun refresh() {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            try {
                uiState.value = uiState.value.copy(isLoading = true, error = null)

                val settings = settingsStore.settingsFlow.first()
                val api = StockApiFactory.create(settings)
                val repo = StockRepository(api)

                val nbItems = 6
                val filter = uiState.value.filter.ifBlank { null }

                val total = repo.loadItemsTotalPages(nbItems, filter)
                val currentPage = uiState.value.page.coerceIn(1, maxOf(1, total))

                val items = repo.loadItemsPage(
                    page = currentPage,
                    nbItems = nbItems
                )

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
            } catch (e: Exception) {
                // ⚠️ Important : ignorer l'annulation volontaire
                if (e is CancellationException) return@launch

                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun onFilterChanged(newFilter: String) {
        uiState.value = uiState.value.copy(
            filter = newFilter,
            page = 1
        )
        filterFlow.value = newFilter
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

    private fun ManufDto.toUi(): ManufUi {
        return ManufUi(name = description.orEmpty())
    }
}
