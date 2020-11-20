package com.ozzikrangir.productlist.ui.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ozzikrangir.productlist.R
import com.ozzikrangir.productlist.data.model.ProductsList
import com.ozzikrangir.productlist.data.provider.ProductsListContentProvider
import com.ozzikrangir.productlist.ui.utils.ListsContent
import java.text.SimpleDateFormat

/**
 * A fragment representing a list of Items.
 */
class ListsFragment : Fragment() {

    private var listsColumnCount = 2

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lists, container, false)
        val iso8601Format = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss"
        )


        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false);
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = GridLayoutManager(context, listsColumnCount)
                adapter = ListsRecyclerViewAdapter(this@ListsFragment)
                val observer: RecyclerView.AdapterDataObserver =
                    object : RecyclerView.AdapterDataObserver() {
                        override fun onChanged() {
                            val items = context.contentResolver.query(
                                ProductsListContentProvider.URI_LISTS,
                                null,
                                null,
                                null
                            )
                            ListsContent.ITEMS.clear()
                            if (items != null) ListsContent.ITEMS.addAll(
                                generateSequence { if (items.moveToNext()) items else null }
                                    .map {
                                        ProductsList(
                                            it.getString(1),
                                            iso8601Format.parse(it.getString(2)),
                                            it.getInt(
                                                0
                                            )
                                        )
                                    }
                                    .toMutableList()
                            )
                            items!!.close()
                            (adapter as ListsRecyclerViewAdapter).values = ListsContent.ITEMS
                            super.onChanged()
                        }
                    }
                (adapter as ListsRecyclerViewAdapter).registerAdapterDataObserver(observer)
                (adapter as ListsRecyclerViewAdapter).notifyDataSetChanged()
                (activity as MainActivity).listAdapter = (adapter as ListsRecyclerViewAdapter)
            }
        }
        return view
    }

}