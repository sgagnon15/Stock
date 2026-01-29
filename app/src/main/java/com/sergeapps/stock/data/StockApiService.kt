package com.sergeapps.stock.data

import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.DELETE


interface StockApiService {

    @GET("itemlist")
    suspend fun getItemList(
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("orderby") orderBy: String = "description",
        @Query("nbitems") nbItems: Int = 6,
        @Query("filter") filter: String? = null
    ): List<ItemListDto>

    @GET("nbpagesitem")
    suspend fun getNbPagesItem(
        @Query("nbitems") nbItems: Int = 6,
        @Query("filter") filter: String? = null
    ): NbPagesDto

    @GET("itemdetail")
    suspend fun getItemDetail(
        @Query("id") id: Int
    ): ItemDetailDto

    @GET("vendorlist")
    suspend fun getVendorList(
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("nbitems") nbItems: Int = 10,
        @Query("filter") filter: String? = null
    ): List<VendorRowDto>

    @GET("manufList")
    suspend fun getManufList(
        @Query("nbitems") nbItems: Int,
        @Query("pagenumber") pageNumber: Int
    ): List<ManufDto>

    @Multipart
    @POST("uploadpic")
    suspend fun uploadPic(
        @Query("id") id: Int,
        @Part file: MultipartBody.Part
    ): UploadPicResponseDto

    @DELETE("picture")
    suspend fun deletePicture(
        @Query("id") id: Int,
        @Query("url") url: String
    ): DeletePictureResponseDto
}
