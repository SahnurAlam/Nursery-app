package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import java.io.File

/**
 * Centralized nursery logo component used across the entire application.
 * Automatically displays the persistent custom logo if configured,
 * or gracefully falls back to the default nursery logo.
 * Preserves original image colors (no tinting/recoloring) and maintains aspect ratio.
 */
@Composable
fun NurseryLogo(
    customLogoPath: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = CircleShape,
    contentScale: ContentScale = ContentScale.Fit,
    border: BorderStroke? = null,
    backgroundColor: Color = Color.White,
    contentDescription: String? = "Nursery Logo",
    defaultResId: Int = R.drawable.app_launcher_logo_1787670340900
) {
    val context = LocalContext.current
    val logoFile = remember(customLogoPath) {
        if (!customLogoPath.isNullOrBlank()) {
            val file = File(customLogoPath)
            if (file.exists() && file.length() > 0) file else null
        } else null
    }

    Surface(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier),
        shape = shape,
        color = backgroundColor,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (logoFile != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(logoFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    error = painterResource(id = defaultResId),
                    fallback = painterResource(id = defaultResId)
                )
            } else {
                Image(
                    painter = painterResource(id = defaultResId),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }
        }
    }
}
