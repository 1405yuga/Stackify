package com.example.stackify.ui.home_fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stackify.databinding.CardViewHomeItemBinding
import com.example.stackify.entity.catalog.CatalogListItem

class ViewHomeItemListAdapter(private val maxItems: Int) :
    ListAdapter<CatalogListItem, ViewHomeItemListAdapter.HomeItemViewHolder>(DiffCallBack) {
    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<CatalogListItem>() {
            override fun areItemsTheSame(
                oldItem: CatalogListItem,
                newItem: CatalogListItem
            ): Boolean {
                return oldItem.itemName == newItem.itemName
            }

            override fun areContentsTheSame(
                oldItem: CatalogListItem,
                newItem: CatalogListItem
            ): Boolean {
                return oldItem == newItem
            }
        }

    }

    class HomeItemViewHolder(private val binding: CardViewHomeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(catalogListItem: CatalogListItem) {
            binding.apply {
                itemName.text = catalogListItem.itemName
                quantity.text = catalogListItem.availableStock.toString()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
        return HomeItemViewHolder(
            CardViewHomeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return if (super.getItemCount() > maxItems) maxItems else super.getItemCount()
    }
}