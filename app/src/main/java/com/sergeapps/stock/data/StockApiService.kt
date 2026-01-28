package com.sergeapps.stock.data

import retrofit2.http.GET
import retrofit2.http.Query

interface StockApiService {

    // D'après le projet MIT Stock.aia:
    // - /itemList?nbitems=9&orderby=description&pagenumber=
    // - /nbpagesitem?nbitems=9&filter=
    @GET("itemList")
    suspend fun getItemList(
        @Query("nbitems") nbItems: Int = 9,
        @Query("orderby") orderBy: String = "description",
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("filter") filter: String? = null
    ): List<ItemSummaryDto>

    @GET("nbpagesitem")
    suspend fun getNbPagesItem(
        @Query("nbitems") nbItems: Int = 9,
        @Query("filter") filter: String? = null,
        @Query("searchstr") searchStr: String? = null
    ): Int

    // Exemple utilisé par Screen1: /info
    @GET("info")
    suspend fun getInfo(): Map<String, String>
}
