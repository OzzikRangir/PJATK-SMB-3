package com.ozzikrangir.productlist.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.ozzikrangir.productlist.data.model.Product
import com.ozzikrangir.productlist.data.model.ProductInfo
import com.ozzikrangir.productlist.data.model.ProductList
import com.ozzikrangir.productlist.data.model.User
import com.ozzikrangir.productlist.ui.utils.ListsContent
import com.ozzikrangir.productlist.ui.main.MainActivity
import com.ozzikrangir.productlist.ui.utils.ProductContent


object RealtimeDBConnector {
    private var database =
        Firebase.database("https://pjatk-shopping-list-2020-default-rtdb.europe-west1.firebasedatabase.app/")
    private lateinit var databaseRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private lateinit var productsRef: DatabaseReference
    private lateinit var listsRef: DatabaseReference
    public var user: User? = null
    public var dataSnapshot: DataSnapshot? = null


    public var newProductId = 1
    public var newListId = 1

    fun init() {
        databaseRef = database.reference
//        databaseRef.setValue(null)
    }

    fun addUser(user: User) {
        databaseRef.child("users").child(user.uid).setValue(user)
    }

    fun setList(list: ProductList, delete: Boolean = false) {
        if (delete)
            listsRef.child("${list.id}").setValue(null)
        else
            listsRef.child("${list.id}").setValue(list)
    }

    fun setProduct(product: Product) {
        productsRef.child("${product.id}").setValue(product)
            .addOnSuccessListener {
                println("SUCCESS PRODUCT")
            }
            .addOnFailureListener {
                println(it)
            }
//        var map = mutableMapOf<String, Product>()
//        map[product.id.toString()] = product
//        databaseRef.child("products").child(user!!.uid).updateChildren(map as Map<String, Any>)

    }

    fun getUserData(uid: String) {
        userRef = databaseRef.child("users").child(uid).ref
        productsRef = databaseRef.child("products").child(uid).ref
        listsRef = databaseRef.child("lists").child(uid).ref

        userRef.addValueEventListener(userListener)

        productsRef.addValueEventListener(productsListener)

        listsRef.addValueEventListener(listsListener)

    }

    private val userListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            user = snapshot.getValue<User>()

        }

        override fun onCancelled(error: DatabaseError) {
            // Getting Post failed, log a message

//            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            // ...
        }
    }

    private val listsListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val productList = snapshot.getValue<MutableList<ProductList>>()
            if (productList != null) {
                ListsContent.ITEMS = productList.filterNotNull() as MutableList<ProductList>
                val maxList = ListsContent.ITEMS.maxByOrNull { it.id }
                if (maxList!=null)
                    newListId = maxList.id+1
                user?.privateListsObj = ListsContent.ITEMS
            }

            ListsContent.ITEMS.forEach { info ->
                val productInfos = mutableListOf<ProductInfo>()
                info.products.forEach { prod ->
                    prod.product = user!!.products.firstOrNull() { it.id == prod.id }
                    if (prod.product != null)
                        productInfos.add(prod)
                }
                ProductContent.ITEMS[info.id] = productInfos
            }
            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance()!!.listAdapter?.notifyDataSetChanged()
                MainActivity.getInstance()!!.productAdapter?.notifyDataSetChanged()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            println(error.toException())
        }
    }

    private val productsListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val products = snapshot.getValue<MutableList<Product>>()
            if (user != null && products != null) {

                val maxProd = products.filterNotNull().maxByOrNull { it.id }
                if (maxProd!=null)
                    newProductId = maxProd.id+1

                user!!.products = products.filterNotNull() as MutableList<Product>

                ListsContent.ITEMS.forEach { info ->
                    val productInfos = mutableListOf<ProductInfo>()
                    info.products.forEach { prod ->
                        prod.product = user!!.products.firstOrNull() { it.id == prod.id }
                        if (prod.product != null)
                            productInfos.add(prod)
                    }
                    ProductContent.ITEMS[info.id] = productInfos
                }
                if (MainActivity.getInstance() != null) {
                    MainActivity.getInstance()!!.listAdapter?.notifyDataSetChanged()
                    MainActivity.getInstance()!!.productAdapter?.notifyDataSetChanged()
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            println(error.toException())
        }
    }

}