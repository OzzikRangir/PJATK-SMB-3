package com.ozzikrangir.productlist.ui.utils

import com.ozzikrangir.productlist.data.model.Product
import java.util.ArrayList
import java.util.HashMap

object ProductContent {

    var ITEMS: MutableList<Product> = ArrayList()
    val ITEM_MAP: MutableMap<String, Product> = HashMap()
}