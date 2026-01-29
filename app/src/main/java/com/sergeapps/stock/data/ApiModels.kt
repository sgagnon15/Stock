package com.sergeapps.stock.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class VendorRowDto(
    val pagenumber: String,
    val description: String
)

@Serializable
data class NbPagesDto(
    @SerialName("nbpages") val nbPages: String
)

@Serializable
data class ItemDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("itemnumber") val itemNumber: Int,
    @SerialName("description") val description: String,
    @SerialName("uom") val uom: String? = null,
    @SerialName("barcode") val barcode: String? = null,
    @SerialName("manufacturer") val manufacturer: String? = null,
    @SerialName("vendor") val vendor: String? = null,
    @SerialName("minlevel") val minLevel: String? = null,
    @SerialName("maxlevel") val maxLevel: String? = null,
    @SerialName("creationdate") val creationDate: String? = null,
    @SerialName("sku") val sku: String? = null,
    @SerialName("vendorUrl") val vendorUrl: String? = null,
    @SerialName("classid") val classId: Int? = null,
    @SerialName("modelnum") val modelNum: String? = null,
    @SerialName("avgcost") val avgCost: String? = null,
    @SerialName("url") val url: String? = null
)

@Serializable
data class ItemListDto(
    @SerialName("id") val id: Int,
    @SerialName("itemnumber") val itemNumber: Int,
    @SerialName("description") val description: String,
    @SerialName("uom") val uom: String? = null,
    @SerialName("barcode") val barcode: String? = null,
    @SerialName("manufacturer") val manufacturer: String? = null,
    @SerialName("vendor") val vendor: String? = null,
    @SerialName("minlevel") val minLevel: String? = null,
    @SerialName("maxlevel") val maxLevel: String? = null,
    @SerialName("creationdate") val creationDate: String? = null,
    @SerialName("sku") val sku: String? = null,
    @SerialName("vendorUrl") val vendorUrl: String? = null,
    @SerialName("classid") val classId: Int? = null,
    @SerialName("modelnum") val modelNum: String? = null,
    @SerialName("avgcost") val avgCost: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("pagenumber") val pageNumber: String? = null
)
