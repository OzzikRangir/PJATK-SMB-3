package com.ozzikrangir.productlist.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
data class ProductList(
    var name: String = "",
    var date: Long = Date().time,
    var id: Int = 0,
    var owner: String = "",
    var products: MutableList<ProductInfo> = mutableListOf(),

    @ExcludeToString @Exclude @set:Exclude @get:Exclude var ownerObj: User = User()
)
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "date" to date,
            "id" to id,
            "owner" to owner,
            "products" to products
        )
    }

    @Exclude
    override fun toString(): String {
        return ExcludeToStringUtils.getToString(this)
    }
}