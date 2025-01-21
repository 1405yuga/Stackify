package com.example.stackify.ui.shopping_cart_fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stackify.R
import com.example.stackify.ShoppingCartProto.CartItem
import com.example.stackify.ShoppingCartProto.ShoppingCart
import com.example.stackify.databinding.FragmentShoppingCartBinding
import com.example.stackify.network.cart_data.ShoppingCartRepository
import com.example.stackify.network.cart_data.ShoppingCartSerializer
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private val Context.shoppingCartDataStore: DataStore<ShoppingCart> by dataStore(
    fileName = "shopping_cart.pb",
    serializer = ShoppingCartSerializer
)

class ShoppingCartFragment : Fragment() {
    private val TAG = this.javaClass.simpleName
    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var shoppingCartRepository: ShoppingCartRepository
    private lateinit var shoppingCartViewModel: ShoppingCartViewModel
    private lateinit var shoppingCartListAdapter: ShoppingCartListAdapter
    private lateinit var swipeHelper: ItemTouchHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShoppingCartBinding.inflate(layoutInflater, container, false)
        shoppingCartRepository = ShoppingCartRepository(requireContext().shoppingCartDataStore)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shoppingCartViewModel = ViewModelProvider(
            this,
            ShoppingCartViewModel.provideFactory((shoppingCartRepository))
        )[ShoppingCartViewModel::class.java]
        shoppingCartListAdapter = ShoppingCartListAdapter(
            addItemView = { currentPos, enteredIndex ->
                shoppingCartViewModel.addWithTransferAndReturn(
                    currentPos = currentPos,
                    enteredIndex = enteredIndex
                )
                Log.d(
                    TAG,
                    "List after adding new item view:${
                        shoppingCartViewModel.tempCartItemsList?.toList()
                    }"
                )
                shoppingCartListAdapter.submitList(shoppingCartViewModel.tempCartItemsList?.toList())
            },
            removeItem = { currentPos ->
                val cursorIndex: Int? =
                    shoppingCartViewModel.deleteWithTransferAndReturnCursorIndex(currentPos = currentPos)
                shoppingCartListAdapter.submitList(shoppingCartViewModel.tempCartItemsList?.toList())
                Log.d(TAG, "After delete cursorIndex : $cursorIndex")
                val indexToFocus = if (currentPos == 0) {
                    if (shoppingCartViewModel.tempCartItemsList.isNullOrEmpty()) null else 0
                } else {
                    currentPos - 1
                }
                Log.d(TAG, "prevPos $currentPos")
                indexToFocus?.let { itf ->
                    val currentEditText =
                        binding.shoppingListRecyclerView
                            .findViewHolderForAdapterPosition(itf)
                            ?.itemView
                            ?.findViewById<EditText>(R.id.item_name_edit_text)
                    //set focus
                    currentEditText?.requestFocus()
                    cursorIndex?.let { currentEditText?.setSelection(it) }
                }
            },
            updateIsPurchased = { position, isPurchased ->
                Log.d(TAG, "Update is purchased : $isPurchased")
                val updatedItem =
                    shoppingCartViewModel.tempCartItemsList?.get(position)?.toBuilder()
                        ?.setIsPurchased(isPurchased)
                        ?.build()
                updatedItem?.let { shoppingCartViewModel.tempCartItemsList?.set(position, it) }
            },
            updateItemName = { position, itemName ->
                val updatedItem =
                    shoppingCartViewModel.tempCartItemsList?.get(position)?.toBuilder()
                        ?.setItemName(itemName)
                        ?.build()
                updatedItem?.let { shoppingCartViewModel.tempCartItemsList?.set(position, it) }
            },
            updateStock = { position, stock ->
                val updatedItem =
                    shoppingCartViewModel.tempCartItemsList?.get(position)?.toBuilder()
                        ?.setStock(stock)
                        ?.build()
                updatedItem?.let { shoppingCartViewModel.tempCartItemsList?.set(position, it) }
            })
        applyBindings()
        applyDeleteOnSwipe()
    }

    private fun applyDeleteOnSwipe() {
        swipeHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val cartItem = shoppingCartListAdapter.currentList[position]

                shoppingCartViewModel.tempCartItemsList?.removeAt(index = position)
                Log.d(
                    TAG,
                    "List after deletion : ${shoppingCartViewModel.tempCartItemsList?.size}"
                )
                shoppingCartListAdapter.submitList(shoppingCartViewModel.tempCartItemsList?.toList())
                shoppingCartListAdapter.notifyItemRemoved(position)
                binding.shoppingListRecyclerView.adapter = shoppingCartListAdapter

                Snackbar.make(
                    binding.shoppingListRecyclerView,
                    "${cartItem.itemName} removed",
                    Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    shoppingCartViewModel.tempCartItemsList?.add(
                        index = position,
                        element = cartItem
                    )
                    shoppingCartListAdapter.submitList(shoppingCartViewModel.tempCartItemsList?.toList())
                }.show()
            }

        })
        swipeHelper.attachToRecyclerView(binding.shoppingListRecyclerView)
    }

    private fun applyBindings() {
        binding.shoppingListRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.addItemTextButton.setOnClickListener {
            lifecycleScope.launch {
                shoppingCartViewModel.tempCartItemsList?.add(
                    index = 0,
                    element = CartItem.getDefaultInstance()
                )
            }
            Log.d(TAG, "List : ${shoppingCartViewModel.tempCartItemsList}")
            shoppingCartListAdapter.submitList(shoppingCartViewModel.tempCartItemsList?.toList())
            setFocusOnEditTextAt(0)
        }
        binding.addItemFloatingBtn.setOnClickListener {
            lifecycleScope.launch {
                val updatedList = shoppingCartViewModel.tempCartItemsList?.apply {
                    add(
                        index = shoppingCartViewModel.tempCartItemsList!!.size,
                        element = CartItem.getDefaultInstance()
                    )

                }
                Log.d(TAG, "Added List : ${shoppingCartViewModel.tempCartItemsList?.size}")
                Log.d(TAG, "List : ${updatedList?.size}")
                shoppingCartListAdapter.submitList(updatedList)
                val currentPos = (updatedList?.size ?: 1) - 1
                shoppingCartListAdapter.notifyItemInserted(currentPos)
                setFocusOnEditTextAt(currentPos)
            }
        }
        binding.saveButton.setOnClickListener {
            Log.d(
                TAG,
                "List-Items to be saved : ${shoppingCartViewModel.tempCartItemsList?.size}"
            )
            shoppingCartViewModel.saveShoppingCart(onSuccess = {
                findNavController().popBackStack()
            })
        }
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.shoppingListRecyclerView.adapter = shoppingCartListAdapter
        shoppingCartListAdapter.submitList(shoppingCartViewModel.tempCartItemsList?.toList())
        shoppingCartViewModel.onDataLoaded = { cartItems ->
            Log.d(TAG, "Initial List : ${cartItems}")
            shoppingCartListAdapter.submitList(cartItems.toList())
        }
    }

    private fun setFocusOnEditTextAt(position: Int) {
        binding.shoppingListRecyclerView.post {
            lifecycleScope.launch {
                delay(200)
                Log.d(TAG, "Item inserted position : $position")
                binding.shoppingListRecyclerView
                    .findViewHolderForAdapterPosition(position)
                    .let { vh ->
                        if (vh == null) {
                            Log.d(TAG, "new item null")
                        } else {
                            try {
                                Log.d(TAG, "Edit text found")
                                vh.itemView.findViewById<EditText>(R.id.item_name_edit_text)
                                    .requestFocus()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
        }
    }
}