package com.liquidtv.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * "Liquid glass" surface: translucent frosted fill, bright specular rim,
 * and a soft diagonal sheen. [strength] 0..1+ brightens it (used on focus).
 */
fun Modifier.glass(shape: Shape, strength: Float = 1f): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = (0.20f * strength).coerceAtMost(0.9f)),
                Color.White.copy(alpha = (0.07f * strength).coerceAtMost(0.9f)),
            )
        )
    )
    .background(
        Brush.linearGradient(
            0f to Color.White.copy(alpha = 0.18f * strength),
            0.45f to Color.Transparent,
            1f to Color.White.copy(alpha = 0.06f * strength),
        )
    )
    .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = (0.65f * strength).coerceAtMost(1f)),
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = (0.32f * strength).coerceAtMost(1f)),
            )
        ),
        shape = shape,
    )
