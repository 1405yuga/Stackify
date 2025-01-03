package com.example.stackify.ui.edit_catlog_fragment

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.stackify.entity.catalog.Catalog
import com.example.stackify.entity.catalog.CatalogListItem

class UpdateCatalogViewModel : ViewModel() {
    private var _catalog: Catalog? = null
    val catalog: Catalog? get() = _catalog

    private val TAG = this.javaClass.simpleName

    fun initialise(catalog: Catalog) {
        if (this._catalog == null) {
            this._catalog = catalog
        } else {
            Log.d(TAG, "Already assigned")
        }
    }

    fun reAddHomeItem(pos: Int, catalogListItem: CatalogListItem): Catalog {
        val updatedHomeItemsList =
            this._catalog?.catalogListItems?.toMutableList()?.apply { add(pos, catalogListItem) }
        _catalog = updatedHomeItemsList?.let { _catalog?.copy(catalogListItems = it) }
        return _catalog ?: Catalog(
            category = "Untitled",
            catalogListItems = mutableListOf(CatalogListItem("Untitled", 0))
        )
    }
}