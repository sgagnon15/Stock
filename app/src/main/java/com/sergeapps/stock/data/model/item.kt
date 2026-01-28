package com.sergeapps.stock.data.model

data class Item(
    val id: Long,
    val name: String,
    val quantity: Int,
    val photoUrl: String?   // <-- AJOUT
)