package com.example.stackify.ui.edit_catlog_fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.stackify.R
import com.example.stackify.databinding.FragmentUpdateCatalogBinding
import com.example.stackify.entity.catalog.Catalog
import com.example.stackify.entity.catalog.CatalogListItem
import com.example.stackify.helper.Converters
import com.example.stackify.network.catalog_db.BaseApplication
import com.example.stackify.ui.CatalogViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder

class UpdateCatalogFragment : Fragment() {
    private lateinit var binding: FragmentUpdateCatalogBinding
    private val TAG = this.javaClass.simpleName
    private lateinit var updateHomeItemListAdapter: UpdateHomeItemListAdapter
    private val viewModel: UpdateCatalogViewModel by viewModels()
    private lateinit var swipeHelper: ItemTouchHelper
    private val catalogViewModel: CatalogViewModel by activityViewModels {
        CatalogViewModel.CatalogViewModelFactory((activity?.application as BaseApplication).database.getCatalogDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val catalogJson = arguments?.getString("CATALOG")
        val catalog = if (catalogJson != null) Converters().jsonToCatalog(catalogJson)
        else Catalog(category = "", catalogListItems = mutableListOf(CatalogListItem("", 0)))
        Log.d(TAG, "Catalog : $catalog")
        viewModel.initialise(catalog = catalog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdateCatalogBinding.inflate(layoutInflater, container, false)
        updateHomeItemListAdapter =
            UpdateHomeItemListAdapter(
                addNewItemView = { currentIndex, enterPressedPos ->
                    addNewTextField(currentIndex, enterPressedPos)
                },
                removeItemView = this::removeCurrentListItem
            )
        updateHomeItemListAdapter.submitList(viewModel.catalog?.catalogListItems)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBinding()
        applyDeleteOnSwipe()
    }

    private fun removeCurrentListItem(currentPosition: Int) {
        Log.d(TAG, "removeCurrentListItem called")
        Log.d(
            TAG,
            "List before changes\n" +
                    GsonBuilder().setPrettyPrinting().create()
                        .toJson(viewModel.catalog?.catalogListItems)
        )
        val newCursorPos =
            viewModel.catalog?.deleteWithTransferAndReturnCursorIndex(index = currentPosition)
        val newList = viewModel.catalog?.catalogListItems
        Log.d(
            TAG,
            "List after changes\n" +
                    GsonBuilder().setPrettyPrinting().create()
                        .toJson(newList)
        )
        updateHomeItemListAdapter.submitList(
            newList
        )
//        {
//            binding.homeItemsRecyclerView.post {
//                val indexToFocus = if (currentPosition == 0) {
//                    if (newList.isNullOrEmpty()) null else 0
//                } else {
//                    currentPosition - 1
//                }
//                Log.d(TAG, "prevPos $currentPosition")
//                indexToFocus?.let { itf ->
//                    val currentEditText =
//                        binding.homeItemsRecyclerView
//                            .findViewHolderForAdapterPosition(itf)
//                            ?.itemView
//                            ?.findViewById<EditText>(R.id.item_name)
//                    //set focus
//                    currentEditText?.requestFocus()
//                    newCursorPos?.let { currentEditText?.setSelection(it) }
//                }
//            }
//        }
        updateHomeItemListAdapter.notifyItemRemoved(currentPosition)

        val indexToFocus = if (currentPosition == 0) {
            if (newList.isNullOrEmpty()) null else 0
        } else {
            currentPosition - 1
        }
        Log.d(TAG, "prevPos $currentPosition")
        indexToFocus?.let { itf ->
            val currentEditText =
                binding.homeItemsRecyclerView
                    .findViewHolderForAdapterPosition(itf)
                    ?.itemView
                    ?.findViewById<EditText>(R.id.item_name)
            //set focus
            currentEditText?.requestFocus()
            newCursorPos?.let { currentEditText?.setSelection(it) }
        }

    }

    private fun addNewTextField(currentIndex: Int, enterPressedPos: Int) {
        Log.d(
            TAG,
            "List before changes\n" +
                    GsonBuilder().setPrettyPrinting().create()
                        .toJson(viewModel.catalog?.catalogListItems)
        )
        viewModel.catalog?.addWithTransferAndReturn(
            index = currentIndex,
            enterPressedPos = enterPressedPos
        )
        Log.d(
            TAG,
            "List after changes\n" +
                    GsonBuilder().setPrettyPrinting().create()
                        .toJson(viewModel.catalog?.catalogListItems)
        )
        updateHomeItemListAdapter.submitList(viewModel.catalog?.catalogListItems)
        updateHomeItemListAdapter.notifyItemInserted(currentIndex + 1)
        binding.homeItemsRecyclerView.post {
            binding.homeItemsRecyclerView
                .findViewHolderForAdapterPosition(currentIndex + 1)
                .let { vh ->
                    if (vh == null) {
                        Log.d(TAG, "new item null")
                    } else {
                        try {
                            vh.itemView.findViewById<EditText>(R.id.item_name_edit_text)
                                .requestFocus()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        }
    }

    private fun applyDeleteOnSwipe() {
        swipeHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val homeItem = updateHomeItemListAdapter.currentList[position]
                viewModel.catalog?.catalogListItems?.removeAt(index = viewHolder.adapterPosition)
                Log.d(TAG, "List after deleting item : ${viewModel.catalog?.catalogListItems}")
                updateHomeItemListAdapter.submitList(viewModel.catalog?.catalogListItems)
                binding.homeItemsRecyclerView.adapter = updateHomeItemListAdapter
                Snackbar.make(
                    binding.homeItemsRecyclerView,
                    "${homeItem.itemName} deleted",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Undo") {
                        viewModel.reAddHomeItem(position, homeItem)
                        updateHomeItemListAdapter.submitList(viewModel.catalog?.catalogListItems)
                    }.show()
            }
        })
        swipeHelper.attachToRecyclerView(binding.homeItemsRecyclerView)
    }

    private fun applyBinding() {
        binding.apply {
            categoryText.setText(viewModel.catalog?.category)
            homeItemsRecyclerView.adapter = updateHomeItemListAdapter
            saveButton.setOnClickListener {
                val categoryText = binding.categoryText.text.toString().trim()
                if (categoryText.isEmpty()) {
                    binding.categoryText.error = "Category required!"
                } else {
                    viewModel.catalog?.updateCategory(categoryText)
                    viewModel.catalog?.let { c ->
                        catalogViewModel.addCatalog(
                            catalog = c,
                            onSuccess = { navigateToBackFragment() },
                            onFailure = {
                                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }
            addHomeItemTextButton.setOnClickListener {
                viewModel.catalog?.catalogListItems?.add(
                    index = 0,
                    element = CatalogListItem(itemName = "", availableStock = 0)
                )
                updateHomeItemListAdapter.submitList(viewModel.catalog?.catalogListItems)
                updateHomeItemListAdapter.notifyItemInserted(0)
                binding.homeItemsRecyclerView.post {
                    val currentEditText =
                        binding.homeItemsRecyclerView
                            .findViewHolderForAdapterPosition(0)
                            ?.itemView?.findViewById<EditText>(R.id.item_name_edit_text)
                    //set focus
                    currentEditText?.requestFocus()
                    //open- keyboard
                    val imm: InputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(currentEditText, 0)
                }

                binding.homeItemsRecyclerView.adapter = updateHomeItemListAdapter
            }
            backButton.setOnClickListener { navigateToBackFragment() }
//            categoryText.apply {
//                requestFocus()
//                setSelection(this.length())
//            }
        }
    }

    private fun navigateToBackFragment() {
        Log.d(TAG, "Back button clicked")
        findNavController().popBackStack()
    }
}