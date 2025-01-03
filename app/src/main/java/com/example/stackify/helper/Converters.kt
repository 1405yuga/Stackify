package com.example.stackify.helper

import androidx.room.TypeConverter
import com.example.stackify.entity.catalog.Catalog
import com.example.stackify.entity.catalog.CatalogListItem
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun listToJson(catalogListItems: List<CatalogListItem>): String =
        Gson().toJson(catalogListItems)

    @TypeConverter
    fun jsonToList(value: String): List<CatalogListItem> =
        Gson().fromJson(value, Array<CatalogListItem>::class.java).toList()

    fun catalogToJson(catalog: Catalog): String = Gson().toJson(catalog)

    fun jsonToCatalog(value: String): Catalog = Gson().fromJson(value, Catalog::class.java)
}