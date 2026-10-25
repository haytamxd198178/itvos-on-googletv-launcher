package com.liquidtv.launcher

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date

private val DefaultAccent = Color(0xFF5B6CFF)

@Composable
fun LauncherScreen(
    apps: List<AppEntry>,
    onOpen: (AppEntry) -> Unit,
    onInfo: (AppEntry) -> Unit,
    onSettings: () -> Unit,
) {
    var focusedApp by remember { mutableStateOf<AppEntry?>(null) }
    val accent by animateColorAsState(focusedApp?.tint ?: DefaultAccent, tween(900), label = "accent")
    val firstTile = remember { FocusRequester() }

    LaunchedEffect(apps.isNotEmpty()) {
        if (apps.isNotEmpty()) runCatching { firstTile.requestFocus() }
    }

    Box(Modifier.fillMaxSize()) {
        Wallpaper(accent)

        Column(Modifier.fillMaxSize()) {
            TopBar(onSettings)
            Hero(focusedApp)

            LazyVerticalGrid(
                columns = GridCells.Adaptive(176.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 80.dp, end = 80.dp, top = 28.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalArrangement = Arrangement.spacedBy(30.dp),
            ) {
                itemsIndexed(apps, key = { _, a -> a.packageName }) { index, app ->
                    AppTile(
                        app = app,
                        modifier = if (index == 0) Modifier.focusRequester(firstTile) else Modifier,
                        onFocused = { focusedApp = app },
                        onOpen = { onOpen(app) },
                        onInfo = { onInfo(app) },
                    )
                }
            }
        }
    }
}

// ───────────────────────────── Top bar ─────────────────────────────

@Composable
private fun TopBar(onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val now by produceState(Date()) {
        while (true) { value = Date(); delay(15_000) }
    }
    val time = remember(now) { DateFormat.getTimeFormat(ctx).format(now) }
    val date = remember(now) { DateFormat.format("EEEE, d MMMM", now).toString() }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 80.dp, vertical = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            BasicText(time, style = TextStyle(color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Light))
            BasicText(date, style = TextStyle(color = Color.White.copy(alpha = 0.65f), fontSize = 18.sp))
        }
        GlassButton("Settings", onClick = onSettings)
    }
}

@Composable
private fun GlassButton(text: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, spring(0.6f, Spring.StiffnessLow), label = "btn")
    val strength by animateFloatAsState(if (focused) 2.2f else 1f, tween(250), label = "btnGlass")
    val shape = RoundedCornerShape(50)

    BasicText(
        text,
        style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium),
        modifier = Modifier
            .onFocusChanged { focused = it.isFocused }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glass(shape, strength)
            .padding(horizontal = 30.dp, vertical = 14.dp),
    )
}

// ───────────────────────────── Hero shelf ─────────────────────────────

@Composable
private fun Hero(app: AppEntry?) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 80.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        AnimatedContent(
            targetState = app,
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(250)) },
            label = "hero",
        ) { a ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (a != null) {
                    Image(
                        bitmap = a.icon,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(150.dp)
                            .shadow(40.dp, RoundedCornerShape(34.dp), ambientColor = a.tint, spotColor = a.tint)
                            .background(tileBrush(a.tint), RoundedCornerShape(34.dp))
                            .padding(18.dp),
                    )
                    Spacer(Modifier.width(36.dp))
                }
                Column {
                    BasicText(
                        a?.label ?: greeting(),
                        style = TextStyle(color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    BasicText(
                        if (a != null) "Press OK to open  ·  hold OK for app info" else "Pick an app to get started",
                        style = TextStyle(color = Color.White.copy(alpha = 0.7f), fontSize = 22.sp),
                    )
                }
            }
        }
    }
}

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 5 -> "Good night"
        h < 12 -> "Good morning"
        h < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

// ───────────────────────────── App tile ─────────────────────────────

private fun tileBrush(tint: Color) = Brush.linearGradient(
    listOf(lerp(tint, Color.White, 0.18f), lerp(tint, Color.Black, 0.45f))
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: AppEntry,
    modifier: Modifier,
    onFocused: () -> Unit,
    onOpen: () -> Unit,
    onInfo: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.14f else 1f, spring(0.55f, Spring.StiffnessLow), label = "scale")
    val lift by animateFloatAsState(if (focused) 1f else 0f, tween(350), label = "lift")
    val shape = RoundedCornerShape(36.dp)

    Column(
        modifier.zIndex(if (focused) 1f else 0f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(176.dp)
                .onFocusChanged {
                    focused = it.isFocused
                    if (it.isFocused) onFocused()
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpen,
                    onLongClick = onInfo,
                )
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = -14f * lift
                }
                .shadow((4f + 28f * lift).dp, shape, ambientColor = app.tint, spotColor = app.tint)
                .background(tileBrush(app.tint), shape)
                .border(
                    width = (1.5f + 1.5f * lift).dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.45f + 0.45f * lift),
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.25f),
                        )
                    ),
                    shape = shape,
                ),
        ) {
            // Icon layer floats a little above the tile (tvOS-style depth)
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
                    .graphicsLayer {
                        val s = 1f + 0.08f * lift
                        scaleX = s
                        scaleY = s
                        translationY = -6f * lift
                    },
            )
            // Specular sheen over the top
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            0f to Color.White.copy(alpha = 0.30f + 0.12f * lift),
                            0.5f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.14f),
                        ),
                        shape,
                    )
            )
        }

        Spacer(Modifier.height(18.dp))
        BasicText(
            app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
            modifier = Modifier
                .width(190.dp)
                .graphicsLayer { alpha = lift; translationY = 8f * (1f - lift) },
        )
    }
}
