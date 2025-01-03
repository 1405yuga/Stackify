package com.example.stackify.ui.layout_store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.layoutPreference: DataStore<Preferences> by preferencesDataStore(name = "layout_preference")

class LayoutPreference(context: Context) {
    private val IS_GRID = booleanPreferencesKey("layout_is_grid")

    //save preference
    suspend fun saveLayoutPreference(context: Context, isLayoutGrid: Boolean) {
        context.layoutPreference.edit {
            it[IS_GRID] = isLayoutGrid
        }
    }

    //get preference
    val userLayoutPreference: Flow<Boolean> = context.layoutPreference.data.map {
        it[IS_GRID] ?: false
    }
}