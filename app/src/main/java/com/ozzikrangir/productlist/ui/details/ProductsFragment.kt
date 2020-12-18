package com.ozzikrangir.productlist.ui.details

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ozzikrangir.productlist.R
import com.ozzikrangir.productlist.ui.main.MainActivity
import com.ozzikrangir.productlist.ui.utils.ProductContent

/**
 * A fragment representing a list of Items.
 */
class ProductsFragment : Fragment() {

    private var columnCount = 2
    private var productId = 1
    private val args: ProductsFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            productId = args.listId
        }
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).listId = productId
        val view = inflater.inflate(R.layout.fragment_lists, container, false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                val headerView = inflater.inflate(R.layout.element_fragment_products, container,false)
                val idView: TextView = headerView.findViewById(R.id.item_number)
                val contentView: TextView = headerView.findViewById(R.id.content)
                val quantity: TextView = headerView.findViewById(R.id.quantity)
                val checkBox: CheckBox = headerView.findViewById(R.id.check_box)
                idView.text= """    ${context.getString(R.string.name_header)}    """
                contentView.text = """    ${context.getString(R.string.price_header)}    """
                quantity.text = """    ${context.getString(R.string.quantity_header)}    """
                checkBox.visibility = View.INVISIBLE
                view.addItemDecoration(ProductsHeaderDecorator(
                    headerView
                    ,                    false
                    ,0.2f
                    ,0f,1))
                view.addItemDecoration(DividerItemDecoration(view.context, 1))
                adapter = ProductRecyclerViewAdapter(this@ProductsFragment)
                val observer: RecyclerView.AdapterDataObserver =
                    object : RecyclerView.AdapterDataObserver() {
                        override fun onChanged() {
                            (adapter as ProductRecyclerViewAdapter).values = ProductContent.ITEMS[productId]!!
                            super.onChanged()
                        }
                    }
                (adapter as ProductRecyclerViewAdapter).registerAdapterDataObserver(observer)
                (adapter as ProductRecyclerViewAdapter).notifyDataSetChanged()
                (activity as MainActivity).productAdapter = (adapter as ProductRecyclerViewAdapter)
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_PRODUCT_ID = "product-id"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ProductsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}