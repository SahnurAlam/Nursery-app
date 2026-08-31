package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.R

enum class AppIconTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val aliasName: String,
    val previewDrawableRes: Int
) {
    DEFAULT(
        id = "default",
        title = "Official Sahnur Logo",
        subtitle = "Original authentic nursery emblem",
        aliasName = "com.example.MainActivityDefault",
        previewDrawableRes = R.drawable.app_launcher_logo_1787670340900
    ),
    EMERALD(
        id = "emerald",
        title = "Emerald Botanical",
        subtitle = "Fresh green nursery sprout & pot",
        aliasName = "com.example.MainActivityEmerald",
        previewDrawableRes = R.drawable.ic_app_icon_preview_emerald
    ),
    GOLD(
        id = "gold",
        title = "Golden Flora",
        subtitle = "Warm luxury gold & plant flora",
        aliasName = "com.example.MainActivityGold",
        previewDrawableRes = R.drawable.ic_app_icon_preview_gold
    ),
    DARK(
        id = "dark",
        title = "Dark Forest",
        subtitle = "Modern minimalist night plant badge",
        aliasName = "com.example.MainActivityDark",
        previewDrawableRes = R.drawable.ic_app_icon_preview_dark
    );

    companion object {
        fun fromId(id: String?): AppIconTheme =
            entries.find { it.id == id } ?: DEFAULT
    }
}

object AppIconManager {

    /**
     * Changes the real Android application launcher icon without shortcuts or duplicate apps.
     * Uses Android's activity-alias mechanism to enable the chosen icon alias and disable others.
     */
    fun setAppIcon(context: Context, targetTheme: AppIconTheme): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName

            AppIconTheme.entries.forEach { theme ->
                val component = ComponentName(packageName, theme.aliasName)
                val state = if (theme == targetTheme) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                packageManager.setComponentEnabledSetting(
                    component,
                    state,
                    PackageManager.DONT_KILL_APP
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Determines which activity-alias icon is currently active in the Android system.
     */
    fun getCurrentActiveTheme(context: Context): AppIconTheme {
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            for (theme in AppIconTheme.entries) {
                val component = ComponentName(packageName, theme.aliasName)
                val state = packageManager.getComponentEnabledSetting(component)
                if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    return theme
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return AppIconTheme.DEFAULT
    }
}
