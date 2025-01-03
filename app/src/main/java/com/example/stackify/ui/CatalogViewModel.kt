package com.example.stackify.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.stackify.entity.catalog.Catalog
import com.example.stackify.network.catalog_db.CategoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogViewModel(private val categoryDao: CategoryDao) : ViewModel() {
    private val TAG = this.javaClass.simpleName

    val allCatalogs: LiveData<List<Catalog>> = categoryDao.getAllCatalogs().asLiveData()

    fun addCatalog(
        catalog: Catalog,
        onSuccess: (String) -> (Unit),
        onFailure: (String) -> (Unit)
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rowId = categoryDao.addCatalog(catalog)
                if (rowId != -1L) {
                    Log.d(TAG, "Added catalog successfully")
                    withContext(Dispatchers.Main) { onSuccess(catalog.category) }
                } else {
                    Log.e(TAG, "Failed to add catalog")
                    withContext(Dispatchers.Main) { onFailure("Failed to add catalog") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while adding catalog :" + e.message)
                withContext(Dispatchers.Main) { onFailure("Error while adding catalog") }
            }
        }
    }

    fun removeCatalog(
        catalog: Catalog,
        onSuccess: (String) -> (Unit),
        onFailure: (String) -> (Unit)
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rowId = categoryDao.deleteCatalog(catalog)
                if (rowId != -1) {
                    Log.d(TAG, "Deleted catalog successfully")
                    withContext(Dispatchers.Main) { onSuccess(catalog.category + " deleted") }
                } else {
                    Log.e(TAG, "Failed to delete catalog")
                    withContext(Dispatchers.Main) { onFailure("Failed to delete catalog") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while deleting catalog :" + e.message)
                withContext(Dispatchers.Main) { onFailure("Error while deleting catalog") }
            }
        }
    }

    class CatalogViewModelFactory(private val categoryDao: CategoryDao) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CatalogViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CatalogViewModel(categoryDao) as T
            }
            throw IllegalArgumentException("uknown viewmodel")
        }
    }
}