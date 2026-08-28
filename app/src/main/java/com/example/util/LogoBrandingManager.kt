package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import com.example.MainActivity
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Utility responsible for managing the dynamic nursery logo branding and
 * generating Android-compatible launcher icon assets from the user's uploaded image.
 *
 * Strict Preservation Rules:
 * - Preserves the uploaded image exactly as provided.
 * - No color changes, no filters, no redesign, no added graphics, and no distortion.
 * - Only performs proportional scaling to generate launcher icon assets.
 */
object LogoBrandingManager {

    private const val BRANDING_DIR = "branding"
    private const val LOGO_FILE_NAME = "nursery_active_logo.png"
    private const val LAUNCHER_ICON_FILE_NAME = "launcher_icon_custom.png"
    private const val LAUNCHER_SHORTCUT_ID = "dynamic_nursery_launcher_shortcut"

    /**
     * Saves the uploaded logo file in its original fidelity and generates
     * Android launcher icon assets without altering color or content.
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

            // Clean up any older logo/icon files
            brandingDir.listFiles()?.forEach { file ->
                if (file.isFile) file.delete()
            }

            val logoFile = File(brandingDir, LOGO_FILE_NAME)

            // Step 1: Copy exact raw source stream with zero alteration
            val copySuccess = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(logoFile).use { output ->
                    input.copyTo(output)
                    true
                }
            } ?: false

            if (!copySuccess || !logoFile.exists() || logoFile.length() == 0L) {
                return null
            }

            // Step 2: Generate Android-compatible launcher icon asset (512x512 with safe padding)
            val launcherIconFile = File(brandingDir, LAUNCHER_ICON_FILE_NAME)
            generateLauncherIconAsset(logoFile, launcherIconFile)

            // Step 3: Update Android dynamic launcher shortcut on API 26+
            updateLauncherShortcut(context, launcherIconFile, nurseryName)

            logoFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a 512x512 launcher icon bitmap maintaining strict aspect ratio and visual fidelity.
     */
    private fun generateLauncherIconAsset(sourceFile: File, outputFile: File) {
        try {
            val sourceBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return
            val targetSize = 512
            val outputBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outputBitmap)

            // Fill with pure white background for adaptive icon compatibility
            canvas.drawColor(Color.WHITE)

            // Safe margin for Android adaptive icon (72% inner safe zone)
            val safeSize = (targetSize * 0.72f)
            val scale = min(safeSize / sourceBitmap.width, safeSize / sourceBitmap.height)
            val scaledWidth = sourceBitmap.width * scale
            val scaledHeight = sourceBitmap.height * scale

            val left = (targetSize - scaledWidth) / 2f
            val top = (targetSize - scaledHeight) / 2f

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(
                sourceBitmap,
                null,
                RectF(left, top, left + scaledWidth, top + scaledHeight),
                paint
            )

            FileOutputStream(outputFile).use { out ->
                outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            sourceBitmap.recycle()
            outputBitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Updates or pins the dynamic launcher icon shortcut on Android 8.0+ devices.
     */
    private fun updateLauncherShortcut(
        context: Context,
        launcherIconFile: File,
        nurseryName: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                if (shortcutManager != null && launcherIconFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(launcherIconFile.absolutePath)
                    if (bitmap != null) {
                        val launchIntent = Intent(context, MainActivity::class.java).apply {
                            action = Intent.ACTION_MAIN
                            addCategory(Intent.CATEGORY_LAUNCHER)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }

                        val shortcut = ShortcutInfo.Builder(context, LAUNCHER_SHORTCUT_ID)
                            .setShortLabel(nurseryName.take(15))
                            .setLongLabel(nurseryName)
                            .setIcon(Icon.createWithAdaptiveBitmap(bitmap))
                            .setIntent(launchIntent)
                            .build()

                        shortcutManager.dynamicShortcuts = listOf(shortcut)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Resets branding back to the default app logo and clears custom launcher icon assets and shortcuts.
     */
    fun resetToDefaultLogo(context: Context) {
        try {
            val brandingDir = File(context.filesDir, BRANDING_DIR)
            if (brandingDir.exists()) {
                brandingDir.listFiles()?.forEach { it.delete() }
                brandingDir.delete()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                shortcutManager?.removeDynamicShortcuts(listOf(LAUNCHER_SHORTCUT_ID))
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
}
