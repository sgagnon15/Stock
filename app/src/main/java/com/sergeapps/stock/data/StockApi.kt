package com.sergeapps.stock.data

import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface StockApi {

    // EXISTANT : autres endpoints...

    // --- PHOTO ITEM ---

    @Multipart
    @POST("items/{itemId}/photo")
    suspend fun uploadItemPhoto(
        @Path("itemId") itemId: Long,
        @Part photo: MultipartBody.Part
    ): ItemDetailDto

    @DELETE("items/{itemId}/photo")
    suspend fun deleteItemPhoto(
        @Path("itemId") itemId: Long
    ): ItemDetailDto
}
