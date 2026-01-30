package com.sergeapps.stock.vm.item

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

data class ItemsListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: String = "",
    val page: Int = 1,
    val totalPages: Int = 1,
    val items: List<ItemRowUi> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true
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
    private var nextPageNumber: Int = 1
    private val itemsPerPage: Int = 15

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
                nextPageNumber = 1

                uiState.value = uiState.value.copy(
                    isLoading = true,
                    isInitialLoading = true,
                    isLoadingMore = false,
                    canLoadMore = true,
                    error = null,
                    items = emptyList(),
                    page = 1,
                    totalPages = 1
                )

                val settings = settingsStore.settingsFlow.first()
                val api = StockApiFactory.create(settings)
                val repository = StockRepository(api)

                val loadedItems = repository.loadItemsPage(
                    page = nextPageNumber,
                    nbItems = itemsPerPage
                )

                val mappedItems = loadedItems.map { dto ->
                    ItemRowUi(
                        id = dto.id,
                        itemNumber = dto.itemNumber.toString(),
                        description = dto.description,
                        vendor = dto.vendor.orEmpty(),
                        manufacturer = dto.manufacturer.orEmpty(),
                        imageUrl = dto.url
                    )
                }

                val canLoadMore = loadedItems.size >= itemsPerPage

                uiState.value = uiState.value.copy(
                    isLoading = false,
                    isInitialLoading = false,
                    isLoadingMore = false,
                    canLoadMore = canLoadMore,
                    error = null,
                    items = mappedItems,
                    page = 1,
                    totalPages = 1
                )

                nextPageNumber = 2
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    return@launch
                }

                uiState.value = uiState.value.copy(
                    isLoading = false,
                    isInitialLoading = false,
                    isLoadingMore = false,
                    error = exception.message
                )
            }
        }
    }

    fun loadMore() {
        val currentState = uiState.value

        if (currentState.isLoadingMore) {
            return
        }

        if (!currentState.canLoadMore) {
            return
        }

        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                isLoadingMore = true,
                error = null
            )

            try {
                val settings = settingsStore.settingsFlow.first()
                val api = StockApiFactory.create(settings)
                val repository = StockRepository(api)

                val loadedItems = repository.loadItemsPage(
                    page = nextPageNumber,
                    nbItems = itemsPerPage
                )

                val mappedItems = loadedItems.map { dto ->
                    ItemRowUi(
                        id = dto.id,
                        itemNumber = dto.itemNumber.toString(),
                        description = dto.description,
                        vendor = dto.vendor.orEmpty(),
                        manufacturer = dto.manufacturer.orEmpty(),
                        imageUrl = dto.url
                    )
                }

                val updatedList = currentState.items + mappedItems
                val canLoadMore = loadedItems.size >= itemsPerPage

                uiState.value = uiState.value.copy(
                    items = updatedList,
                    isLoadingMore = false,
                    canLoadMore = canLoadMore
                )

                if (canLoadMore) {
                    nextPageNumber += 1
                }
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    return@launch
                }

                uiState.value = uiState.value.copy(
                    isLoadingMore = false,
                    error = exception.message ?: "Erreur loadMore"
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
}
