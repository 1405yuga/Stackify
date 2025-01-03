package com.example.stackify.ui.edit_catlog_fragment

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stackify.databinding.CardUpdateHomeItemBinding
import com.example.stackify.entity.catalog.CatalogListItem

class UpdateHomeItemListAdapter(
    private val addNewItemView: (currentIndex: Int, enterPressedIndex: Int) -> (Unit),
    private val removeItemView: (currentIndex: Int) -> Unit
) :
    ListAdapter<CatalogListItem, UpdateHomeItemListAdapter.HomeItemViewHolder>(DiffCallBack) {
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

    class HomeItemViewHolder(
        private val binding: CardUpdateHomeItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val TAG = this.javaClass.simpleName

        fun bind(
            catalogListItem: CatalogListItem,
            addNewItemView: (currentIndex: Int, enterPressedIndex: Int) -> Unit,
            removeItemView: (currentIndex: Int) -> Unit
        ) {
            Log.d(TAG, "ViewHolder called -----")
            binding.itemNameEditText.setText(catalogListItem.itemName)
            binding.itemNameEditText.setOnKeyListener { view, i, keyEvent ->
                //on enter press - add new textview with remaining text
                Log.d(TAG, "MyEnter keycode $i, Enter keycode ${KeyEvent.KEYCODE_ENTER}")
                if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                    addNewItemView(adapterPosition, binding.itemNameEditText.selectionStart)
                    true
                } else if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_DEL) {
                    if (binding.itemNameEditText.selectionStart == 0 && adapterPosition > 0) {
                        removeItemView(adapterPosition)
                    }
                    true
                } else false
            }
            //set focus to last character
            binding.itemNameEditText.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    val editText = view as EditText
                    editText.setSelection(editText.length())
                }
            }

            //set entered value
            binding.itemNameEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0!!.trim().isNotEmpty()) catalogListItem.itemName = p0.toString()
                    Log.d(TAG, "onTextChanged $p0")
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            fun refreshCount() {
                binding.quantity.text = catalogListItem.availableStock.toString()
            }
            binding.addQuantity.setOnClickListener {
                catalogListItem.availableStock++
                refreshCount()
            }
            binding.subQuantity.setOnClickListener {
                catalogListItem.decreaseQty()
                refreshCount()
            }
            refreshCount()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
        return HomeItemViewHolder(
            CardUpdateHomeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeItemViewHolder, position: Int) {
        holder.bind(
            catalogListItem = getItem(position),
            addNewItemView = addNewItemView,
            removeItemView = removeItemView
        )
    }
}