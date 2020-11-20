package com.ozzikrangir.productlist.ui.utils

import com.ozzikrangir.productlist.data.model.ProductsList
import java.util.ArrayList
import java.util.HashMap

object ListsContent {

    var ITEMS: MutableList<ProductsList> = ArrayList()
    val ITEM_MAP: MutableMap<String, ProductsList> = HashMap()

    init {
        // Add some sample items
    }

}