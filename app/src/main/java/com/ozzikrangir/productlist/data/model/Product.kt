package com.ozzikrangir.productlist.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Product(
    var name: String= "",
    var price: Float = 0f,
    var id: Int = 0,
    var owner: User = User(),
)
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "price" to price,
            "id" to id,
            "owner" to owner
        )
    }

    override fun toString(): String {
        return ExcludeToStringUtils.getToString(this)
    }
}