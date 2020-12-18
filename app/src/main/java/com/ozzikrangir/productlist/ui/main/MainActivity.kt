package com.ozzikrangir.productlist.ui.main

import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.ozzikrangir.productlist.R
import com.ozzikrangir.productlist.data.RealtimeDBConnector
import com.ozzikrangir.productlist.data.model.Product
import com.ozzikrangir.productlist.data.model.ProductInfo
import com.ozzikrangir.productlist.data.model.ProductList
import com.ozzikrangir.productlist.data.model.User
import com.ozzikrangir.productlist.ui.details.ProductRecyclerViewAdapter
import com.ozzikrangir.productlist.ui.login.LoginActivity
import com.ozzikrangir.productlist.ui.settings.SettingsActivity


class MainActivity : AppCompatActivity() {

    companion object {
        private var sInstance: MainActivity? = null
        public fun getInstance(): MainActivity? {
            return sInstance
        }
    }

    private var listInputDialog: AlertDialog? = null
    private var productInputDialog: AlertDialog? = null
    var listAdapter: ListsRecyclerViewAdapter? = null
    var productAdapter: ProductRecyclerViewAdapter? = null
    var listId: Int = 0
    private var external = false
    private var selected: Product? = null

    private lateinit var mAuth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {

        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser
        if (currentUser != null) {

            RealtimeDBConnector.init()
            RealtimeDBConnector.getUserData(currentUser.uid)
            if (RealtimeDBConnector.user == null)
                RealtimeDBConnector.addUser(User(currentUser.uid))
        }


        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val alternative = preferences.getBoolean("alternative", false)
        if (alternative)
            setTheme(R.style.Theme_ProductListAlternative_NoActionBar)

        listId = intent.getIntExtra("listId", listId)
        val productId = intent.getIntExtra("productId", 0)
        sInstance = this;
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navigator: View = findViewById<View>(R.id.nav_host_fragment)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            navigator.findNavController().navigateUp()
        }
        var darkMode = preferences.getBoolean("darkmode", false)
        if (darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        delegate.applyDayNight()

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        findViewById<FloatingActionButton>(R.id.add_fab).setOnClickListener {
            when (navigator.findNavController().currentDestination!!.id) {
                R.id.lists_fragment -> {
                    listInputDialog?.show()
                }
                R.id.products_fragment -> {
                    productInputDialog?.show()
                }
            }
        }
        listInputDialog = let {
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            val editListName = R.id.edit_list_name
            builder.setView(inflater.inflate(R.layout.dialog_list_form, null))
                .setTitle(R.string.dialog_add_list_title)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { _, _ ->
                        val name = listInputDialog?.findViewById<EditText>(editListName)?.text
                        if (!name.isNullOrEmpty()) {
                            RealtimeDBConnector.setList(ProductList(name.toString(), id = RealtimeDBConnector.newListId))
                            listAdapter?.notifyDataSetChanged()
                            listInputDialog?.findViewById<EditText>(editListName)?.text = null

                        } else {
                            TODO("TOASTER EMPTY NAME")
                        }

                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { _, _ ->
                        listInputDialog?.findViewById<EditText>(editListName)?.text = null
                    })
            builder.create()
        }
        productInputDialog = let {
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            val editProductName = R.id.edit_product_name
            val editProductPrice = R.id.edit_price_number
            val editProductQuantity = R.id.edit_quantity
            builder.setView(inflater.inflate(R.layout.dialog_product_form, null))
                .setTitle(R.string.dialog_add_product_title)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { _, _ ->

                        val name = productInputDialog?.findViewById<EditText>(editProductName)?.text
                        val price =
                            productInputDialog?.findViewById<EditText>(editProductPrice)?.text
                        val quantity =
                            productInputDialog?.findViewById<EditText>(editProductQuantity)?.text
                        if (!(name.isNullOrEmpty() || price.isNullOrEmpty() || quantity.isNullOrEmpty())) {
                            if (!external) {
                                val id = RealtimeDBConnector.newProductId

                                val product = Product(
                                    name.toString(),
                                    price.toString().toFloat(),
                                    id,
                                    RealtimeDBConnector.user!!
                                )
                                RealtimeDBConnector.setProduct(product)

                                val list =
                                    RealtimeDBConnector.user!!.privateListsObj.firstOrNull { list -> list.id == listId }
                                if (list != null) {
                                    val prodInfo = ProductInfo(
                                        id,
                                        false,
                                        false,
                                        quantity.toString().toInt(),
                                        product
                                    )
                                    list.products.add(prodInfo)
                                    RealtimeDBConnector.setList(list)
                                    Intent().also { intent ->
                                        intent.action = "com.example.broadcast.MY_NOTIFICATION"
                                        intent.component = ComponentName(
                                            "com.ozzikrangir.productlistnotification",
                                            "com.ozzikrangir.productlistnotification.Receiver"
                                        )
                                        intent.putExtra("listId", listId)
                                        intent.putExtra("productId", productId)
                                        intent.putExtra("productName", name.toString())
                                        sendOrderedBroadcast(
                                            intent,
                                            "com.ozzikrangir.productlist.permissions.NOTIFICATION_PERMISSION"
                                        )
                                    }
                                }
                            }
                        }else if (selected != null) {
                        } else {
                            TODO("TOASTER EMPTY NAME")
                        }

                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { _, _ ->
                        listInputDialog?.findViewById<EditText>(editProductName)?.text = null
                        listInputDialog?.findViewById<EditText>(editProductPrice)?.text = null
                        listInputDialog?.findViewById<EditText>(editProductQuantity)?.text =
                            null
                    })
            builder.create()
        }
        productInputDialog?.setTitle(R.string.dialog_add_list_title)
        external = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                }
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                mAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java).apply {
                }
                startActivity(intent)
            true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenu.ContextMenuInfo
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_product, menu)
    }

    override fun onStart() {
        super.onStart()
//        if (RealtimeDBConnector.user!=null) {
//            val product1 = Product("produkt", 21.37f, 1, RealtimeDBConnector.user!!)
//            RealtimeDBConnector.setProduct(product1)
//            val productList = ProductList("name", id = 1)
//            val productInfo = ProductInfo(product1.id, amount = 21)
//            productList.products.add(productInfo)
//            RealtimeDBConnector.user!!.privateListsObj.add(productList)
//            RealtimeDBConnector.setList(productList)
//
//        }
//        println(RealtimeDBConnector.user!!.privateListsObj)
    }
}