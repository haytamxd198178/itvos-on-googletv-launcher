package com.liquidtv.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/** Slow drifting colour blobs that take on the focused app's tint. */
@Composable
fun Wallpaper(accent: Color) {
    val t = rememberInfiniteTransition(label = "wallpaper")
    val a by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(22_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "a",
    )
    val b by t.animateFloat(
        1f, 0f,
        infiniteRepeatable(tween(31_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "b",
    )
    val second = accent.hueShift(55f)

    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0B0D1A), Color(0xFF05060C))))

        fun blob(c: Color, cx: Float, cy: Float, r: Float, alpha: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(c.copy(alpha = alpha), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r,
                ),
                radius = r,
                center = Offset(cx, cy),
            )
        }

        val w = size.width
        val h = size.height
        blob(accent, w * (0.15f + 0.35f * a), h * (0.20f + 0.25f * b), w * 0.55f, 0.55f)
        blob(second, w * (0.85f - 0.35f * b), h * (0.75f - 0.30f * a), w * 0.50f, 0.42f)
        blob(Color(0xFF3A2A8F), w * 0.5f, h * 1.0f, w * 0.60f, 0.35f)

        // bottom vignette so labels stay readable
        drawRect(
            Brush.verticalGradient(
                0.55f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.55f),
            )
        )
    }
}

fun Color.hueShift(degrees: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[0] = (hsv[0] + degrees) % 360f
    return Color(android.graphics.Color.HSVToColor(hsv))
}
