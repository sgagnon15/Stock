package com.sergeapps.stock.data

class StockRepository(
    private val api: StockApiService
) {
    suspend fun loadItemsPage(page: Int, filter: String?): List<ItemSummaryDto> {
        return api.getItemList(pageNumber = page, filter = filter)
    }

    suspend fun loadItemsTotalPages(filter: String?): Int {
        return api.getNbPagesItem(filter = filter)
    }
}
