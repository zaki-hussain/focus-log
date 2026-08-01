package app.focuslog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun ClockScreen(store: Store, now: Long, goStats: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp)) {
        val active = store.active
        if (active == null) Idle(store, goStats) else Running(store, active, now)
        Footer(store, now)
    }
}

@Composable
private fun ColumnScope.Idle(store: Store, goStats: () -> Unit) {
    var curTag by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Txt("Focus Log", 18.sp, weight = FontWeight.Bold)
            Txt("Stats →", 13.sp, Dim, modifier = Modifier.clickable(onClick = goStats).padding(4.dp))
        }
        Spacer(Modifier.height(44.dp))
        Txt("Minimum", 14.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        MinimumDial(store)
        Spacer(Modifier.height(6.dp))
        Txt("Drag to adjust · Tap to type", 12.sp, Faint)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 45, 60, 90).forEach { m ->
                Chip("${m}m", store.lastMin == m) { store.setMinimum(m) }
            }
        }
        Spacer(Modifier.height(40.dp))
        Txt("Tag · Optional", 14.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        TagPicker(store, curTag) { curTag = it }
    }
    Spacer(Modifier.weight(1f))
    Box(
        Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Accent)
            .clickable { store.start(curTag) },
        contentAlignment = Alignment.Center,
    ) {
        Txt("Start", 24.sp, OnAccent, FontWeight.Medium, spacing = 2.sp)
    }
}

/** The big MM:SS minimum — drag horizontally to scrub, tap to type a value. */
@Composable
private fun MinimumDial(store: Store) {
    var editing by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    if (editing) {
        val focus = remember { FocusRequester() }
        BasicTextField(
            input,
            { input = it.filter(Char::isDigit).take(3) },
            Modifier
                .width(170.dp)
                .focusRequester(focus)
                .drawBehind { drawLine(Accent, Offset(0f, size.height), Offset(size.width, size.height)) },
            textStyle = TextStyle(Ink, 48.sp, FontWeight.Light, fontFamily = Mono),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                input.toIntOrNull()?.let { store.setMinimum(it) }
                editing = false
            }),
            singleLine = true,
            cursorBrush = SolidColor(Accent),
        )
        LaunchedEffect(Unit) { focus.requestFocus() }
    } else {
        val density = LocalDensity.current.density
        Txt(
            fmtMin(store.lastMin * 60_000L), 56.sp, Ink, FontWeight.Light, Mono, 0.sp,
            modifier = Modifier
                .pointerInput(Unit) {
                    var base = 0
                    var acc = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { base = store.lastMin; acc = 0f },
                        onHorizontalDrag = { _, dx ->
                            acc += dx
                            store.setMinimum(base + (acc / (5 * density)).roundToInt())
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        input = store.lastMin.toString()
                        editing = true
                    }
                },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagPicker(store: Store, curTag: String?, onPick: (String?) -> Unit) {
    var adding by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        store.tags.forEach { t ->
            Chip(t, curTag == t) { onPick(if (curTag == t) null else t) }
        }
        if (adding) {
            val focus = remember { FocusRequester() }
            BasicTextField(
                input,
                { input = it },
                Modifier
                    .width(110.dp)
                    .focusRequester(focus)
                    .drawBehind { drawLine(Accent, Offset(0f, size.height), Offset(size.width, size.height)) }
                    .padding(vertical = 10.dp),
                textStyle = TextStyle(Ink, 14.sp, fontFamily = Karla),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    input.trim().takeIf { it.isNotEmpty() }?.let { store.addTag(it); onPick(it) }
                    adding = false
                }),
                singleLine = true,
                cursorBrush = SolidColor(Accent),
            )
            LaunchedEffect(Unit) { focus.requestFocus() }
        } else {
            Chip("+", false) { input = ""; adding = true }
        }
    }
}

@Composable
private fun ColumnScope.Running(store: Store, active: Active, now: Long) {
    val elapsed = (now - active.start).coerceAtLeast(0)
    val pastMin = elapsed >= active.minimum
    Txt("Focus Log", 18.sp, Dim, FontWeight.Bold)
    Column(
        Modifier.weight(1f).fillMaxWidth(),
        Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        Alignment.CenterHorizontally,
    ) {
        Txt(
            fmtElapsed(elapsed),
            if (elapsed >= 3_600_000) 60.sp else 80.sp,
            if (pastMin) Good else Pre,
            FontWeight.Light, Mono, 2.sp,
        )
        Txt("Minimum ${fmtMin(active.minimum)}", 14.sp, Dim, family = Mono)
        active.tag?.let { Txt(it, 14.sp, Accent) }
    }
    when {
        pastMin -> Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Good)
                .clickable { store.end(failed = false) },
            contentAlignment = Alignment.Center,
        ) {
            Txt("Stop", 24.sp, OnGood, FontWeight.Medium, spacing = 2.sp)
        }
        elapsed < 30_000 -> Column(
            Modifier.fillMaxWidth().padding(vertical = 42.dp),
            Arrangement.spacedBy(10.dp),
            Alignment.CenterHorizontally,
        ) {
            GhostButton("Cancel") { store.cancel() }
            Txt("Within 30s — won't be logged", 11.sp, Faint)
        }
        else -> Box(Modifier.fillMaxWidth().padding(vertical = 56.dp), Alignment.Center) {
            GhostButton("Terminate") { store.end(failed = true) }
        }
    }
}
