package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sahnur_nursery_prefs")

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val currencySymbol: String = "₹",
    val nurseryName: String = "Sahnur Nursery",
    val ownerName: String = "Sahnur Alam Mondal",
    val contactPhone: String = "+91 98765 00000",
    val nurseryAddress: String = "Main Road, Greenbelt Nursery Zone"
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val NURSERY_NAME = stringPreferencesKey("nursery_name")
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val CONTACT_PHONE = stringPreferencesKey("contact_phone")
        val NURSERY_ADDRESS = stringPreferencesKey("nursery_address")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val themeString = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = try {
            AppThemeMode.valueOf(themeString)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
        val currencySymbol = preferences[PreferencesKeys.CURRENCY_SYMBOL] ?: "₹"
        val nurseryName = preferences[PreferencesKeys.NURSERY_NAME] ?: "Sahnur Nursery"
        val ownerName = preferences[PreferencesKeys.OWNER_NAME] ?: "Sahnur Alam Mondal"
        val contactPhone = preferences[PreferencesKeys.CONTACT_PHONE] ?: "+91 98765 00000"
        val nurseryAddress = preferences[PreferencesKeys.NURSERY_ADDRESS] ?: "Main Road, Greenbelt Nursery Zone"

        UserPreferences(
            themeMode = themeMode,
            currencySymbol = currencySymbol,
            nurseryName = nurseryName,
            ownerName = ownerName,
            contactPhone = contactPhone,
            nurseryAddress = nurseryAddress
        )
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateCurrencySymbol(symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun updateNurseryProfile(name: String, owner: String, phone: String, address: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NURSERY_NAME] = name
            preferences[PreferencesKeys.OWNER_NAME] = owner
            preferences[PreferencesKeys.CONTACT_PHONE] = phone
            preferences[PreferencesKeys.NURSERY_ADDRESS] = address
        }
    }
}
