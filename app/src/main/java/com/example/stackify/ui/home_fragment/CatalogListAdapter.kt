package com.example.stackify.ui.home_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stackify.databinding.CardCatalogItemBinding
import com.example.stackify.entity.catalog.Catalog

class CatalogListAdapter(
    private val maxItemsDisplayed: Int,
    private val navigateToEdit: (catalog: Catalog) -> (Unit)
) :
    ListAdapter<Catalog, CatalogListAdapter.CatalogViewHolder>(DiffCallBack) {

    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<Catalog>() {
            override fun areItemsTheSame(oldItem: Catalog, newItem: Catalog): Boolean {
                return oldItem.category == newItem.category
            }

            override fun areContentsTheSame(oldItem: Catalog, newItem: Catalog): Boolean {
                return oldItem == newItem
            }
        }
    }

    class CatalogViewHolder(private val binding: CardCatalogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            catalog: Catalog,
            maxItemsDisplayed: Int,
            navigateToEdit: (catalog: Catalog) -> Unit
        ) {
            val viewHomeItemListAdapter = ViewHomeItemListAdapter(maxItemsDisplayed)
            viewHomeItemListAdapter.submitList(catalog.catalogListItems)
            binding.apply {
                catlogCard.setOnClickListener { navigateToEdit(catalog) }
                categoryTextView.text = catalog.category
                homeItemsRecyclerView.apply {
                    adapter = viewHomeItemListAdapter
                    setOnTouchListener { _, event ->
                        if (event.action == android.view.MotionEvent.ACTION_UP) {
                            catlogCard.performClick() // Directly call the click listener
                        }
                        false
                    }
                }
                if (catalog.catalogListItems.size > maxItemsDisplayed) {
                    val displayText =
                        "+ ${catalog.catalogListItems.size - maxItemsDisplayed} items..."
                    extraItemsTextView.text = displayText
                    extraItemsTextView.visibility = View.VISIBLE
                } else extraItemsTextView.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        return CatalogViewHolder(
            CardCatalogItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bind(getItem(position), maxItemsDisplayed, navigateToEdit)
    }
}