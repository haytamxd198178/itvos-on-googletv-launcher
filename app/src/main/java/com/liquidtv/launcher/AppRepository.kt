package com.liquidtv.launcher

import android.content.Context
import android.content.Intent
import android.graphics.Color as AColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppEntry(
    val label: String,
    val packageName: String,
    val launchIntent: Intent,
    val icon: ImageBitmap,
    val tint: Color,
)

object AppRepository {

    /** TV apps first (sorted), then regular launcher apps (sorted). */
    fun load(ctx: Context): List<AppEntry> {
        val pm = ctx.packageManager
        val seen = HashSet<String>()
        val result = ArrayList<AppEntry>()

        for (category in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            val group = ArrayList<AppEntry>()
            for (ri in pm.queryIntentActivities(query, 0)) {
                val pkg = ri.activityInfo.packageName
                if (pkg == ctx.packageName || !seen.add(pkg)) continue

                val intent = Intent(Intent.ACTION_MAIN)
                    .addCategory(category)
                    .setClassName(pkg, ri.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                val bitmap = runCatching { ri.loadIcon(pm).toBitmap(256, 256) }.getOrNull() ?: continue
                group += AppEntry(
                    label = ri.loadLabel(pm).toString(),
                    packageName = pkg,
                    launchIntent = intent,
                    icon = bitmap.asImageBitmap(),
                    tint = dominantTint(bitmap),
                )
            }
            result += group.sortedBy { it.label.lowercase() }
        }
        return result
    }

    /** Average colour of the icon's opaque, non-grey pixels, pushed toward a vivid mid tone. */
    private fun dominantTint(bmp: android.graphics.Bitmap): Color {
        var r = 0L; var g = 0L; var b = 0L; var n = 0L
        val step = 8
        for (y in 0 until bmp.height step step) {
            for (x in 0 until bmp.width step step) {
                val p = bmp.getPixel(x, y)
                if (AColor.alpha(p) < 200) continue
                val hsv = FloatArray(3)
                AColor.colorToHSV(p, hsv)
                if (hsv[1] < 0.15f || hsv[2] < 0.15f) continue // skip greys / near-black
                r += AColor.red(p); g += AColor.green(p); b += AColor.blue(p); n++
            }
        }
        if (n == 0L) return Color(0xFF6C7BFF)
        val avg = AColor.rgb((r / n).toInt(), (g / n).toInt(), (b / n).toInt())
        val hsv = FloatArray(3)
        AColor.colorToHSV(avg, hsv)
        hsv[1] = hsv[1].coerceIn(0.55f, 0.95f)
        hsv[2] = hsv[2].coerceIn(0.60f, 0.95f)
        return Color(AColor.HSVToColor(hsv))
    }
}
