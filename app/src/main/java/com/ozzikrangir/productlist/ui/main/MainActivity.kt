package com.ozzikrangir.productlist.ui.main

import android.app.AlertDialog
import android.content.ComponentName
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
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
import com.ozzikrangir.productlist.ui.details.ProductRecyclerViewAdapter
import com.ozzikrangir.productlist.R
import com.ozzikrangir.productlist.data.model.Product
import com.ozzikrangir.productlist.data.provider.DBHandler
import com.ozzikrangir.productlist.data.provider.ProductsListContentProvider
import com.ozzikrangir.productlist.ui.settings.SettingsActivity
import com.ozzikrangir.productlist.ui.utils.ProductContent


class MainActivity : AppCompatActivity() {

    private var listInputDialog: AlertDialog? = null
    private var productInputDialog: AlertDialog? = null
    var listAdapter: ListsRecyclerViewAdapter? = null
    var productAdapter: ProductRecyclerViewAdapter? = null
    var listId: Int = 0
    private var external = false
    private var selected: Product? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val alternative = preferences.getBoolean("alternative", false)
        if (alternative)
            setTheme(R.style.Theme_ProductListAlternative_NoActionBar)
        listId = intent.getIntExtra("listId", listId)
        val productId = intent.getIntExtra("productId", 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
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
                            val values = ContentValues()
                            values.put(DBHandler.COLUMN_LIST_NAME, name.toString())
                            contentResolver.insert(ProductsListContentProvider.URI_LISTS, values)
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
                                val values = ContentValues()
                                values.put(DBHandler.COLUMN_PRODUCT_NAME, name.toString())
                                values.put(DBHandler.COLUMN_PRICE, price.toString())
                                values.put(DBHandler.COLUMN_QUANTITY, quantity.toString())
                                val uri = contentResolver.insert(
                                    Uri.parse(ProductsListContentProvider.URI_LISTS.toString() + "/" + listId),
                                    values
                                )
                                if (uri != null) {
                                    Intent().also { intent ->
                                        intent.action = "com.example.broadcast.MY_NOTIFICATION"
                                        intent.component = ComponentName(
                                            "com.ozzikrangir.productlistnotification",
                                            "com.ozzikrangir.productlistnotification.Receiver"
                                        )
                                        val productId = uri.pathSegments.last()
                                        intent.putExtra("listId", listId)
                                        intent.putExtra("productId", productId.toInt())
                                        intent.putExtra("productName", name.toString())
                                        sendOrderedBroadcast(
                                            intent,
                                            "com.ozzikrangir.productlist.permissions.NOTIFICATION_PERMISSION"
                                        )
                                    }
                                }
                            } else if (selected != null) {
                                val productValues = ContentValues()
                                val listValues = ContentValues()
                                if (selected?.name != name.toString())
                                    productValues.put(
                                        DBHandler.COLUMN_PRODUCT_NAME,
                                        name.toString()
                                    )
                                if (selected?.price != price.toString().toFloat())
                                    productValues.put(DBHandler.COLUMN_PRICE, price.toString())
                                if (selected?.amount != quantity.toString().toInt())
                                    listValues.put(DBHandler.COLUMN_QUANTITY, quantity.toString())
                                if (!productValues.isEmpty)
                                contentResolver.update(
                                    Uri.parse(ProductsListContentProvider.URI_PRODUCTS.toString() + "/" + productId),
                                    productValues,
                                    null,
                                    null
                                )
                                if (!listValues.isEmpty)
                                contentResolver.update(
                                    Uri.parse(ProductsListContentProvider.URI_PRODUCT_LISTS.toString() + "/" + listId + "/" + productId),
                                    listValues,
                                    null,
                                    null
                                )
                                external = false
                                selected = null
                            }

                            productAdapter?.notifyDataSetChanged()
                            productInputDialog?.findViewById<EditText>(editProductName)?.text = null
                            productInputDialog?.findViewById<EditText>(editProductPrice)?.text =
                                null
                            productInputDialog?.findViewById<EditText>(editProductQuantity)?.text =
                                null
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
        if (listId != 0) {
            val action = ListsFragmentDirections.actionListsFragmentToProductsFragment(listId)
            action.arguments.putInt("listId", listId)
            navigator.findNavController().navigate(action)
            println(Uri.parse(ProductsListContentProvider.URI_LISTS.toString() + "/" + listId + "/" + productId))
            val it = contentResolver.query(
                Uri.parse(ProductsListContentProvider.URI_LISTS.toString() + "/" + listId + "/" + productId),
                null,
                null,
                null
            )
            if (it!!.moveToNext()) {
                selected =
                    Product(
                        it.getString(2),
                        it.getFloat(3),
                        it.getInt(0),
                        listId,
                        it.getInt(4) == 1,
                        it.getInt(1)
                    )
                it.close()
                external = true
                productInputDialog?.show()
                productInputDialog?.setTitle(R.string.dialog_edit_product_title)
                productInputDialog?.findViewById<EditText>(R.id.edit_product_name)
                    ?.setText(selected?.name)
                productInputDialog?.findViewById<EditText>(R.id.edit_price_number)
                    ?.setText(selected?.price.toString())
                productInputDialog?.findViewById<EditText>(R.id.edit_quantity)
                    ?.setText(selected?.amount.toString())
            }
        }
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
}