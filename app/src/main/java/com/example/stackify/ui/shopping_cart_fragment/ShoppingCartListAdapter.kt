package com.example.stackify.ui.shopping_cart_fragment

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.stackify.ShoppingCartProto
import com.example.stackify.databinding.CardShoppingItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ShoppingCartListAdapter(
    private val addItemView: (currentPos: Int, enterPressedIndex: Int) -> Unit,
    private val removeItem: (currentPos: Int) -> (Unit),
    private val updateIsPurchased: (currentPos: Int, isPurchased: Boolean) -> (Unit),
    private val updateItemName: (currentPos: Int, itemName: String) -> (Unit),
    private val updateStock: (currentPos: Int, stock: Int) -> (Unit)
) :
    ListAdapter<ShoppingCartProto.CartItem, ShoppingCartListAdapter.ShoppingCartViewHolder>(
        DiffCallBack
    ) {
    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<ShoppingCartProto.CartItem>() {
            override fun areItemsTheSame(
                oldItem: ShoppingCartProto.CartItem,
                newItem: ShoppingCartProto.CartItem
            ): Boolean {
                return oldItem.itemId == newItem.itemId
            }

            override fun areContentsTheSame(
                oldItem: ShoppingCartProto.CartItem,
                newItem: ShoppingCartProto.CartItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ShoppingCartViewHolder(private val binding: CardShoppingItemBinding) :
        ViewHolder(binding.root) {
        val TAG = this.javaClass.simpleName
        fun bind(
            cartItem: ShoppingCartProto.CartItem,
            addItemView: (currentPos: Int, enterPressedIndex: Int) -> Unit,
            removeItem: (currentPos: Int) -> (Unit),
            updateIsPurchased: (currentPos: Int, isPurchased: Boolean) -> (Unit),
            updateItemName: (currentPos: Int, itemName: String) -> (Unit),
            updateStock: (currentPos: Int, stock: Int) -> (Unit)
        ) {
            Log.d(TAG, "Viewholder called---------")
            binding.itemNameEditText.setText(cartItem.itemName)
            binding.stock.text = cartItem.stock.toString()
            binding.itemNameEditText.setOnKeyListener { view, i, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                    if (isAdapterPositionValid(adapterPosition))
                        addItemView(
                            adapterPosition,
                            binding.itemNameEditText.selectionStart
                        )
                    true
                } else if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_DEL) {
                    if (binding.itemNameEditText.selectionStart == 0 && adapterPosition > 0) {
                        if (isAdapterPositionValid(adapterPosition)) removeItem(adapterPosition)
                    }
                    true
                } else {
                    false
                }
            }
            binding.itemCheckBox.isChecked = cartItem.isPurchased
            binding.itemCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (cartItem.isPurchased != isChecked) {
                    updateIsPurchased(adapterPosition, isChecked)
                    Log.d(TAG, "isPurchased value : $isChecked")
                }
            }
            var stock = cartItem.stock
            binding.addQuantity.setOnClickListener {
                stock++
                binding.stock.text = stock.toString()
                if (isAdapterPositionValid(adapterPosition)) updateStock(adapterPosition, stock)
            }
            binding.subQuantity.setOnClickListener {
                if (stock > 0) stock--
                binding.stock.text = stock.toString()
                if (isAdapterPositionValid(adapterPosition)) updateStock(adapterPosition, stock)
            }
            binding.itemNameEditText.addTextChangedListener(object : TextWatcher {
                private var doJob: Job? = null
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    doJob?.cancel()
                    doJob = CoroutineScope(Dispatchers.Main).launch {
//                        delay(500)
                        p0?.let {
                            Log.d(TAG, "AFTER text changed $p0")
                            if (isAdapterPositionValid(adapterPosition)) updateItemName(
                                adapterPosition,
                                p0.toString()
                            )
                        }
                    }
                }
            })
        }

        fun isAdapterPositionValid(position: Int): Boolean {
            return position != RecyclerView.NO_POSITION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartViewHolder {
        return ShoppingCartViewHolder(
            CardShoppingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ShoppingCartViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            addItemView = addItemView,
            removeItem = removeItem,
            updateItemName = updateItemName,
            updateStock = updateStock,
            updateIsPurchased = updateIsPurchased
        )
    }
}