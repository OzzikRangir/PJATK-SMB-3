package com.ozzikrangir.productlist.ui.main

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ozzikrangir.productlist.R
import com.ozzikrangir.productlist.data.model.ProductsList
import com.ozzikrangir.productlist.data.provider.DBHandler
import com.ozzikrangir.productlist.data.provider.ProductsListContentProvider
import com.ozzikrangir.productlist.ui.details.ProductsFragment
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class ListsRecyclerViewAdapter(
    private val parentFragment: Fragment
) : RecyclerView.Adapter<ListsRecyclerViewAdapter.ViewHolder>() {

    var values: List<ProductsList> = ArrayList()
    private var selected: ProductsList? = null
    private var alertDialog: AlertDialog? = null


    private fun showPopupMenu(v: View, item: ProductsList) {
        PopupMenu(parentFragment.context, v).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_edit -> {
                        selected = item
                        alertDialog?.show()
                        alertDialog?.findViewById<EditText>(R.id.edit_list_name)
                            ?.setText(selected?.name)
                        true
                    }
                    R.id.action_delete -> {
                        parentFragment.context?.contentResolver?.delete(
                            Uri.parse(
                                ProductsListContentProvider.URI_LISTS.toString() + "/" + item.id
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
            .inflate(R.layout.element_fragment_lists, parent, false)

        alertDialog = let {
            val builder = AlertDialog.Builder(parentFragment.activity)
            val inflater = parentFragment.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_list_form, null))
                .setTitle(R.string.dialog_edit_list_title)
                .setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { _, _ ->
                        val name =
                            alertDialog?.findViewById<EditText>(R.id.edit_list_name)?.text.toString()
                        val values = ContentValues()
                        values.put(DBHandler.COLUMN_LIST_NAME, name)
                        parentFragment.context?.contentResolver?.update(
                            Uri.parse(ProductsListContentProvider.URI_LISTS.toString() + "/" + selected?.id),
                            values,
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        holder.idView.text = item.name
        holder.contentView.text =
            item.date!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT))
        holder.itemView.setOnClickListener {
            val action = ListsFragmentDirections.actionListsFragmentToProductsFragment(item.id!!)
            action.arguments.putInt("listId", item.id!!)
            parentFragment.findNavController().navigate(action)

        }
        holder.itemView.setOnLongClickListener {
            showPopupMenu(it, item)

            return@setOnLongClickListener true
        }
    }


    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.item_name)
        val contentView: TextView = view.findViewById(R.id.content)

    }
}