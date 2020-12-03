package com.ozzikrangir.productlist.ui.details

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ozzikrangir.productlist.R
import com.ozzikrangir.productlist.data.model.Product
import com.ozzikrangir.productlist.data.provider.DBHandler
import com.ozzikrangir.productlist.data.provider.ProductsListContentProvider
import java.util.*
import kotlin.collections.ArrayList


class ProductRecyclerViewAdapter(
    private val parentFragment: Fragment
) : RecyclerView.Adapter<ProductRecyclerViewAdapter.ViewHolder>() {

    var values: List<Product> = ArrayList()
    private var selected: Product? = null
    private var alertDialog: AlertDialog? = null


    private fun showPopupMenu(v: View, item: Product) {
        PopupMenu(parentFragment.context, v).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_edit -> {
                        selected = item
                        alertDialog?.show()
                        alertDialog?.findViewById<EditText>(R.id.edit_product_name)
                            ?.setText(selected?.name)
                        alertDialog?.findViewById<EditText>(R.id.edit_price_number)
                            ?.setText(selected?.price.toString())
                        alertDialog?.findViewById<EditText>(R.id.edit_quantity)
                            ?.setText(selected?.amount.toString())
                        true
                    }
                    R.id.action_delete -> {
                        parentFragment.context?.contentResolver?.delete(
                            Uri.parse(
                                ProductsListContentProvider.URI_LISTS.toString() + "/" + item.list_id + "/" + item.id
                            ), null, null
                        )
                        notifyDataSetChanged()
                        true
                    }
                    else -> false
                }
            }
            inflate(R.menu.menu_product)
            show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_fragment_products, parent, false)
        alertDialog = let {
            val builder = AlertDialog.Builder(parentFragment.activity)
            val inflater = parentFragment.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_product_form, null))
                .setTitle(R.string.dialog_edit_product_title)
                .setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { _, _ ->
                        val name =
                            alertDialog?.findViewById<EditText>(R.id.edit_product_name)?.text.toString()
                        val price =
                            alertDialog?.findViewById<EditText>(R.id.edit_price_number)?.text.toString()
                        val quantity =
                            alertDialog?.findViewById<EditText>(R.id.edit_quantity)?.text.toString()
                        val productValues = ContentValues()
                        val listValues = ContentValues()
                        productValues.put(DBHandler.COLUMN_PRODUCT_NAME, name)
                        productValues.put(DBHandler.COLUMN_PRICE, price)
                        listValues.put(DBHandler.COLUMN_QUANTITY, quantity)
                        parentFragment.context?.contentResolver?.update(
                            Uri.parse(ProductsListContentProvider.URI_PRODUCTS.toString() + "/" + selected?.id),
                            productValues,
                            null,
                            null
                        )
                        parentFragment.context?.contentResolver?.update(
                            Uri.parse(ProductsListContentProvider.URI_PRODUCT_LISTS.toString() + "/" + selected?.list_id + "/" + selected?.id),
                            listValues,
                            null,
                            null
                        )
                        selected = null
                        notifyDataSetChanged()
                    })
                .setNegativeButton(
                    R.string.cancel,
                    DialogInterface.OnClickListener { _, _ ->
                        // User cancelled the dialog
                    })
            builder.create()
        }

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val item = values[position]
        holder.idView.text = item.name
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance(Locale.getDefault())


        holder.contentView.text = format.format(item.price)

        holder.quantity.text = item.amount.toString()
        holder.checkBox.isChecked = item.marked == true
        holder.itemView.setOnLongClickListener { view ->
            showPopupMenu(view, item)
            return@setOnLongClickListener true
        }
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val values = ContentValues()
            values.put(DBHandler.COLUMN_MARK, isChecked)
            parentFragment.context?.contentResolver?.update(
                Uri.parse(ProductsListContentProvider.URI_LISTS.toString() + "/" + item.list_id + "/" + item.id),
                values,
                null,
                null
            )

        }

    }


    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.item_number)
        val contentView: TextView = view.findViewById(R.id.content)
        val quantity: TextView = view.findViewById(R.id.quantity)
        val checkBox: CheckBox = view.findViewById(R.id.check_box)

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}