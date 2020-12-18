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
import com.ozzikrangir.productlist.data.RealtimeDBConnector
import com.ozzikrangir.productlist.data.model.Product
import com.ozzikrangir.productlist.data.model.ProductInfo
import java.util.*
import kotlin.collections.ArrayList


class ProductRecyclerViewAdapter(
    private val parentFragment: Fragment
) : RecyclerView.Adapter<ProductRecyclerViewAdapter.ViewHolder>() {

    var values: List<ProductInfo> = ArrayList()
    private var selected: ProductInfo? = null
    private var alertDialog: AlertDialog? = null


    @RequiresApi(Build.VERSION_CODES.N)
    private fun showPopupMenu(v: View, item: ProductInfo) {
        PopupMenu(parentFragment.context, v).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_edit -> {
                        selected = item
                        alertDialog?.show()
                        alertDialog?.findViewById<EditText>(R.id.edit_product_name)
                            ?.setText(selected?.product?.name)
                        alertDialog?.findViewById<EditText>(R.id.edit_price_number)
                            ?.setText(selected?.product?.price.toString())
                        alertDialog?.findViewById<EditText>(R.id.edit_quantity)
                            ?.setText(selected?.amount!!.toString())
                        true
                    }
                    R.id.action_delete -> {
                        val list =
                            RealtimeDBConnector.user!!.privateListsObj.firstOrNull { prod -> prod.products.any { obj -> obj.id == item.id } }
                        if (list != null) {
                            list.products.removeIf { prod -> prod.id == item.id }
                            RealtimeDBConnector.setList(list)
                        }
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

                        val list =
                            RealtimeDBConnector.user!!.privateListsObj.firstOrNull { it -> it.products.any { obj -> obj.id == selected?.id } }
                        val product =
                            RealtimeDBConnector.user!!.products.firstOrNull { it -> it.id == selected?.id }

                        if (product != null && list != null) {
                            product.name = name
                            product.price = price.toFloat()
                            RealtimeDBConnector.setProduct(product)

                            list.products.first { it.id == selected?.id }.amount = quantity.toInt()
                            RealtimeDBConnector.setList(list)
                        }
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
        holder.idView.text = item.product!!.name
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance(Locale.getDefault())


        holder.contentView.text = format.format(item.product!!.price)

        holder.quantity.text = item.amount.toString()
        holder.checkBox.isChecked = item.marked
        holder.itemView.setOnLongClickListener { view ->
            showPopupMenu(view, item)
            return@setOnLongClickListener true
        }
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val list =
                RealtimeDBConnector.user!!.privateListsObj.firstOrNull { it -> it.products.any { obj -> obj.id == item.id } }
            if (list != null) {
                list.products.first { it.id == item.id }.marked = isChecked
                RealtimeDBConnector.setList(list)
            }
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