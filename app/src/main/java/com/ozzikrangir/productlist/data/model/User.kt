package com.ozzikrangir.productlist.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class User(
    var uid: String = "",
    var sharedLists: MutableMap<String, String> = mutableMapOf(),

    @ExcludeToString @Exclude @set:Exclude @get:Exclude var products: MutableList<Product> = mutableListOf(),
    @ExcludeToString @Exclude @set:Exclude @get:Exclude var privateListsObj: MutableList<ProductList> = mutableListOf(),
    @ExcludeToString @Exclude @set:Exclude @get:Exclude var sharedListsObj: MutableList<ProductList> = mutableListOf()
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "sharedLists" to sharedLists,
        )
    }

    @Exclude
    override fun toString(): String {
        return ExcludeToStringUtils.getToString(this)
    }
}