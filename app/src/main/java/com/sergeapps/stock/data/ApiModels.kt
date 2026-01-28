package com.sergeapps.stock.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Modèles "minimum" (à ajuster selon les JSON exacts de ton serveur) ---

@Serializable
data class ItemSummaryDto(
    @SerialName("itemnumber") val itemNumber: String? = null,
    @SerialName("description") val description: String? = null
)

@Serializable
data class PagedResultDto<T>(
    @SerialName("items") val items: List<T> = emptyList()
)
