package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Utility responsible for managing nursery logo branding and custom launcher icon assets.
 *
 * Strict Fidelity Rules:
 * - Preserves the uploaded images in high fidelity.
 * - No unsolicited filters, redesigns, or distortive operations.
 */
object LogoBrandingManager {

    private const val BRANDING_DIR = "branding"
    private const val LOGO_FILE_NAME = "nursery_active_logo.png"
    private const val APP_ICON_FILE_NAME = "custom_app_launcher_icon.png"

    /**
     * Saves the uploaded logo file in its original fidelity for nursery branding (receipts, reports, UI).
     *
     * @return Absolute file path of the saved active logo, or null on failure.
     */
    fun saveLogoAndGenerateLauncherAssets(
        context: Context,
        sourceUri: Uri,
        nurseryName: String = "Nursery"
    ): String? {
        return try {
            val brandingDir = File(context.filesDir, BRANDING_DIR).apply {
                if (!exists()) mkdirs()
            }

            val logoFile = File(brandingDir, LOGO_FILE_NAME)

            // Copy exact raw source stream with zero alteration
            val copySuccess = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(logoFile).use { output ->
                    input.copyTo(output)
                    true
                }
            } ?: false

            if (!copySuccess || !logoFile.exists() || logoFile.length() == 0L) {
                return null
            }

            logoFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a separate uploaded image specifically as the Android launcher app icon source.
     *
     * @return Absolute file path of the saved custom app icon, or null on failure.
     */
    fun saveCustomAppIcon(
        context: Context,
        sourceUri: Uri,
        appName: String = "Sahnur Nursery"
    ): String? {
        return try {
            val brandingDir = File(context.filesDir, BRANDING_DIR).apply {
                if (!exists()) mkdirs()
            }

            val appIconFile = File(brandingDir, APP_ICON_FILE_NAME)

            // Copy exact raw source stream
            val copySuccess = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(appIconFile).use { output ->
                    input.copyTo(output)
                    true
                }
            } ?: false

            if (!copySuccess || !appIconFile.exists() || appIconFile.length() == 0L) {
                return null
            }

            appIconFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resets specifically the custom app icon.
     */
    fun resetCustomAppIcon(context: Context, appName: String = "Sahnur Nursery") {
        try {
            val brandingDir = File(context.filesDir, BRANDING_DIR)
            val appIconFile = File(brandingDir, APP_ICON_FILE_NAME)
            if (appIconFile.exists()) {
                appIconFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Resets branding back to the default app logo and clears custom branding files.
     */
    fun resetToDefaultLogo(context: Context) {
        try {
            val brandingDir = File(context.filesDir, BRANDING_DIR)
            if (brandingDir.exists()) {
                brandingDir.listFiles()?.forEach { it.delete() }
                brandingDir.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves the custom logo file if it exists and has content.
     */
    fun getActiveLogoFile(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        return if (file.exists() && file.length() > 0L) file else null
    }

    /**
     * Retrieves the custom launcher app icon file if it exists and has content.
     */
    fun getActiveAppIconFile(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        return if (file.exists() && file.length() > 0L) file else null
    }
}
