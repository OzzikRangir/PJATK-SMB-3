package com.ozzikrangir.productlist.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ProductInfo(
    var id: Int = 0,
    var marked: Boolean = false,
    var locked: Boolean = false,
    var amount: Int = 0,

    @ExcludeToString @Exclude @set:Exclude @get:Exclude var product: Product? = null,
)
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "marked" to marked,
            "locked" to locked,
            "amount" to amount
        )
    }

    @Exclude
    override fun toString(): String {
        return ExcludeToStringUtils.getToString(this)
    }
}