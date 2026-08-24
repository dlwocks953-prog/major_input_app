package com.example.myapplication

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "favorites")

enum class FavoriteType(val key: String) {
    MAJOR("favorite_majors"),
    JOB("favorite_jobs"),
    SCHOOL("favorite_schools")
}

object FavoriteStore {

    fun favorites(context: Context, type: FavoriteType): Flow<Set<String>> {
        val prefKey = stringSetPreferencesKey(type.key)
        return context.dataStore.data.map { prefs ->
            prefs[prefKey] ?: emptySet()
        }
    }

    suspend fun toggle(context: Context, type: FavoriteType, name: String) {
        val prefKey = stringSetPreferencesKey(type.key)
        context.dataStore.edit { prefs ->
            val current = prefs[prefKey] ?: emptySet()
            prefs[prefKey] = if (name in current) current - name else current + name
        }
    }
}