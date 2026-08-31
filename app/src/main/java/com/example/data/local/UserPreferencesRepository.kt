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
    val nurseryAddress: String = "Main Road, Greenbelt Nursery Zone",
    val customLogoPath: String? = null,
    val customAppIconPath: String? = null,
    val appIconThemeId: String = "default",
    val invoiceNotes: String = DEFAULT_INVOICE_NOTES,
    val invoiceFooter: String = DEFAULT_INVOICE_FOOTER
) {
    companion object {
        const val DEFAULT_INVOICE_NOTES = "Thank you for buying from our nursery! Plant more trees."
        const val DEFAULT_INVOICE_FOOTER = "Visit Again!..."
    }
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val NURSERY_NAME = stringPreferencesKey("nursery_name")
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val CONTACT_PHONE = stringPreferencesKey("contact_phone")
        val NURSERY_ADDRESS = stringPreferencesKey("nursery_address")
        val CUSTOM_LOGO_PATH = stringPreferencesKey("custom_logo_path")
        val CUSTOM_APP_ICON_PATH = stringPreferencesKey("custom_app_icon_path")
        val APP_ICON_THEME_ID = stringPreferencesKey("app_icon_theme_id")
        val INVOICE_NOTES = stringPreferencesKey("invoice_notes")
        val INVOICE_FOOTER = stringPreferencesKey("invoice_footer")
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
        val customLogoPath = preferences[PreferencesKeys.CUSTOM_LOGO_PATH]?.takeIf { it.isNotBlank() }
        val customAppIconPath = preferences[PreferencesKeys.CUSTOM_APP_ICON_PATH]?.takeIf { it.isNotBlank() }
        val appIconThemeId = preferences[PreferencesKeys.APP_ICON_THEME_ID] ?: "default"
        val invoiceNotes = preferences[PreferencesKeys.INVOICE_NOTES] ?: UserPreferences.DEFAULT_INVOICE_NOTES
        val invoiceFooter = preferences[PreferencesKeys.INVOICE_FOOTER] ?: UserPreferences.DEFAULT_INVOICE_FOOTER

        UserPreferences(
            themeMode = themeMode,
            currencySymbol = currencySymbol,
            nurseryName = nurseryName,
            ownerName = ownerName,
            contactPhone = contactPhone,
            nurseryAddress = nurseryAddress,
            customLogoPath = customLogoPath,
            customAppIconPath = customAppIconPath,
            appIconThemeId = appIconThemeId,
            invoiceNotes = invoiceNotes,
            invoiceFooter = invoiceFooter
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

    suspend fun updateCustomLogoPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path.isNullOrBlank()) {
                preferences.remove(PreferencesKeys.CUSTOM_LOGO_PATH)
            } else {
                preferences[PreferencesKeys.CUSTOM_LOGO_PATH] = path
            }
        }
    }

    suspend fun updateCustomAppIconPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path.isNullOrBlank()) {
                preferences.remove(PreferencesKeys.CUSTOM_APP_ICON_PATH)
            } else {
                preferences[PreferencesKeys.CUSTOM_APP_ICON_PATH] = path
            }
        }
    }

    suspend fun updateAppIconThemeId(themeId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_ICON_THEME_ID] = themeId
        }
    }

    suspend fun updateInvoiceCustomization(notes: String, footer: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INVOICE_NOTES] = notes
            preferences[PreferencesKeys.INVOICE_FOOTER] = footer
        }
    }

    suspend fun resetInvoiceCustomization() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INVOICE_NOTES] = UserPreferences.DEFAULT_INVOICE_NOTES
            preferences[PreferencesKeys.INVOICE_FOOTER] = UserPreferences.DEFAULT_INVOICE_FOOTER
        }
    }
}
