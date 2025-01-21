package com.example.stackify.ui.shopping_cart_fragment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stackify.ShoppingCartProto.CartItem
import com.example.stackify.network.cart_data.ShoppingCartRepository
import kotlinx.coroutines.launch

class ShoppingCartViewModel(private val cartRepository: ShoppingCartRepository) : ViewModel() {
    private val TAG = this.javaClass.simpleName
    var tempCartItemsList: MutableList<CartItem>? = null

    var onDataLoaded: ((MutableList<CartItem>) -> (Unit))? = null

    init {
        viewModelScope.launch {
            try {
                tempCartItemsList = cartRepository.getInitialCart().cartItemListList.toMutableList()
                Log.d(TAG, "Data populated : ${tempCartItemsList!!.size}")
                onDataLoaded?.invoke(tempCartItemsList!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveShoppingCart(onSuccess: () -> (Unit)) {
        viewModelScope.launch {
            tempCartItemsList?.let {
                Log.d(TAG, "List - items before saving ${it.size}")
                cartRepository.addAllItems(cartItemsList = it)
                onSuccess()
            }
        }
    }

    fun addWithTransferAndReturn(currentPos: Int, enteredIndex: Int) {
        val current = this.tempCartItemsList?.getOrNull(currentPos)

        current?.itemName.let { it ->
            val cartItem = if (it.isNullOrEmpty()) {
                CartItem.getDefaultInstance()
            } else {
                val firstPart = try {
                    it.slice(0 until enteredIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                val secondPart = try {
                    it.slice(enteredIndex until it.length)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                if (firstPart != null) {
                    val updatedItem =
                        this.tempCartItemsList?.get(currentPos)?.toBuilder()
                            ?.setItemName(firstPart)
                            ?.build()
                    updatedItem?.let { this.tempCartItemsList?.set(currentPos, it) }
                }
                val newItem = CartItem.getDefaultInstance()
                    ?.toBuilder()
                    ?.setItemName(secondPart)
                    ?.build()
                newItem
            }
            cartItem?.let { it1 ->
                Log.d(TAG, "cart item created---------")
                try {
                    val updatesList = this.tempCartItemsList?.apply {
                        add(
                            index = currentPos + 1,
                            element = it1
                        )
                    }
                    this.tempCartItemsList = updatesList
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }

    }

    fun deleteWithTransferAndReturnCursorIndex(currentPos: Int): Int? {
        try {
            val cursorIndex: Int?
            if (currentPos > 0) {
                val toDelete = this.tempCartItemsList?.getOrNull(index = currentPos)
                val destination = this.tempCartItemsList?.getOrNull(index = currentPos - 1)
                cursorIndex = destination?.itemName?.length
                val destinationItemName = destination?.itemName
                val newDestination = destination
                    ?.toBuilder()
                    ?.setItemName(destinationItemName + toDelete?.itemName)
                    ?.build()
                newDestination?.let { this.tempCartItemsList?.set(currentPos - 1, it) }
            } else {
                cursorIndex = null
            }
            this.tempCartItemsList?.removeAt(index = currentPos)
            return cursorIndex
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    companion object {
        fun provideFactory(cartRepository: ShoppingCartRepository): Factory {
            return Factory(cartRepository)
        }

        class Factory(
            private val cartRepository: ShoppingCartRepository
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ShoppingCartViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ShoppingCartViewModel(cartRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

