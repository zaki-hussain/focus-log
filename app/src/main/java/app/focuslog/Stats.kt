package app.focuslog

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val CellEmpty = Color(0xFF1B1B1E)
private val CellBase = Color(0xFF16181A)
private val AccentDim = Accent.copy(alpha = 0.35f)

@Composable
fun StatsScreen(store: Store, now: Long, goClock: () -> Unit) {
    val context = LocalContext.current
    var range by remember { mutableStateOf("All") }
    val counted = store.sessions.filter { !it.failed }
    val d0 = dayStart(); val w0 = weekStart(); val m0 = monthStart(); val y0 = yearStart()
    val rangeStarts = listOf("Today" to d0, "Week" to w0, "Month" to m0, "Year" to y0, "All" to 0L)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically,
        ) {
            Txt("← Clock", 13.sp, Dim, modifier = Modifier.clickable(onClick = goClock).padding(4.dp))
            Txt("Stats", 18.sp, weight = FontWeight.Bold)
            Txt("Export", 13.sp, Dim, modifier = Modifier
                .clickable {
                    context.startActivity(Intent.createChooser(
                        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, store.exportCsv()),
                        "Export focus log"))
                }
                .padding(4.dp))
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 24.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                rangeStarts.forEach { (label, t) ->
                    Column(Modifier.weight(1f), Arrangement.spacedBy(6.dp)) {
                        Txt(label, 11.sp, Dim)
                        Txt(fmtDur(counted.filter { it.start >= t }.sumOf { it.duration }), 13.sp, family = Mono)
                    }
                }
            }

            val rStart = rangeStarts.first { it.first == range }.second
            val ranged = counted.filter { it.start >= rStart }
            Spacer(Modifier.height(36.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Header("By hour of day")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rangeStarts.forEach { (label, _) ->
                        Txt(label, 11.sp, if (range == label) Accent else Faint,
                            modifier = Modifier.clickable { range = label }.padding(vertical = 2.dp))
                    }
                }
            }
            val hourSums = LongArray(24)
            ranged.forEach { hourSums[Instant.ofEpochMilli(it.start).atZone(zone).hour] += it.duration }
            BarRow(hourSums.toList(), 2.dp)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                listOf("00", "06", "12", "18", "24").forEach { Txt(it, 11.sp, Faint) }
            }

            Spacer(Modifier.height(32.dp))
            Header("By day of week")
            val dowSums = LongArray(7)
            ranged.forEach { dowSums[Instant.ofEpochMilli(it.start).atZone(zone).dayOfWeek.value - 1] += it.duration }
            BarRow(dowSums.toList(), 6.dp)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                "MTWTFSS".forEach { Txt(it.toString(), 11.sp, Faint, align = TextAlign.Center, modifier = Modifier.weight(1f)) }
            }

            Spacer(Modifier.height(32.dp))
            Header("Weekly total")
            val weekSums = LongArray(10)
            counted.forEach {
                val idx = 9 - ((w0 + 7 * DAY - 1 - it.start) / (7 * DAY)).toInt()
                if (idx in 0..9) weekSums[idx] += it.duration
            }
            BarRow(weekSums.toList(), 6.dp)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Txt("10 wk ago", 11.sp, Faint)
                Txt("this wk", 11.sp, Faint)
            }

            Spacer(Modifier.height(32.dp))
            Header("Daily heatmap")
            Heatmap(counted, w0, now)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Txt("16 wk ago", 11.sp, Faint)
                Txt("today", 11.sp, Faint)
            }

            val byTag = counted.filter { it.tag != null && it.start >= m0 }
                .groupBy { it.tag!! }.mapValues { (_, v) -> v.sumOf { it.duration } }
                .toList().sortedByDescending { it.second }.take(5)
            if (byTag.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Header("This month by tag")
                val tagMax = byTag.first().second
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    byTag.forEachIndexed { i, (tag, ms) ->
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
                            Txt(tag, 12.sp, Dim, maxLines = 1, modifier = Modifier.width(72.dp))
                            Box(Modifier.weight(1f)) {
                                Box(
                                    Modifier
                                        .fillMaxWidth((ms.toFloat() / tagMax).coerceAtLeast(0.02f))
                                        .height(8.dp)
                                        .background(if (i == 0) Accent else AccentDim, RoundedCornerShape(4.dp))
                                )
                            }
                            Txt(fmtDur(ms), 12.sp, family = Mono, align = TextAlign.End, modifier = Modifier.width(56.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Header("History")
                Txt("Hold an entry for actions", 11.sp, Faint)
            }
            val history = store.sessions.sortedByDescending { it.start }.take(60)
            if (history.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Txt("No sessions yet", 12.sp, Faint)
            } else {
                Spacer(Modifier.height(8.dp))
                history.forEach { HistoryEntry(store, it) }
            }
        }
    }
}

@Composable
private fun Header(text: String) = Txt(text, 13.sp, weight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))

/** Rounded-top bars; the tallest bar is full accent, the rest dimmed. */
@Composable
private fun BarRow(values: List<Long>, gap: androidx.compose.ui.unit.Dp) {
    val max = values.max().coerceAtLeast(1)
    Row(Modifier.fillMaxWidth().height(64.dp), Arrangement.spacedBy(gap), Alignment.Bottom) {
        values.forEach { v ->
            val h = if (v == 0L) 2 else (64 * (v.toFloat() / max).coerceAtLeast(0.04f)).roundToInt()
            Box(
                Modifier.weight(1f).height(h.dp)
                    .background(if (v == max) Accent else AccentDim, RoundedCornerShape(3.dp, 3.dp, 0.dp, 0.dp))
            )
        }
    }
}

/** GitHub-style grid: 16 week columns × 7 weekday rows, ending this week. */
@Composable
private fun Heatmap(counted: List<Session>, weekStart: Long, now: Long) {
    val gridStart = weekStart - 15 * 7 * DAY
    val daily = LongArray(112)
    counted.forEach {
        val i = ((it.start - gridStart) / DAY).toInt()
        if (i in 0..111) daily[i] += it.duration
    }
    val max = daily.max().coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(7) { day ->
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(3.dp)) {
                repeat(16) { week ->
                    val i = week * 7 + day
                    val color = when {
                        gridStart + i * DAY > now -> Color.Transparent
                        daily[i] == 0L -> CellEmpty
                        else -> lerp(CellBase, Accent, (daily[i].toFloat() / max).coerceAtLeast(0.25f))
                    }
                    Box(Modifier.weight(1f).aspectRatio(1f).background(color, RoundedCornerShape(3.dp)))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryEntry(store: Store, s: Session) {
    var open by remember(s) { mutableStateOf(false) }
    var editing by remember(s) { mutableStateOf(false) }
    var editVal by remember(s) { mutableStateOf("") }
    val start = Instant.ofEpochMilli(s.start).atZone(zone)
    val end = Instant.ofEpochMilli(s.end).atZone(zone)
    Column(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onLongClick = { open = true; editing = false }, onClick = { open = false; editing = false })
            .drawBehind { drawLine(Line, Offset(0f, size.height), Offset(size.width, size.height)) }
            .padding(vertical = 10.dp),
        Arrangement.spacedBy(4.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            val day = start.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)
            Txt(
                "$day %02d/%02d  %02d:%02d–%02d:%02d".format(
                    start.dayOfMonth, start.monthValue,
                    start.hour, start.minute, end.hour, end.minute),
                13.sp, Dim, family = Mono,
            )
            Txt(fmtDur(s.duration), 13.sp, if (s.failed) Dim else Ink, family = Mono)
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Txt(s.tag ?: "", 12.sp, Accent)
            if (s.failed) Txt("failed", 12.sp, Fail)
        }
        if (open && !editing) {
            Row(Modifier.padding(top = 6.dp, bottom = 2.dp), Arrangement.spacedBy(20.dp)) {
                Txt("Delete", 13.sp, Fail, modifier = Modifier.clickable { store.delete(s) })
                Txt("Edit tag", 13.sp, Accent, modifier = Modifier.clickable { editVal = s.tag ?: ""; editing = true })
                Txt("Cancel", 13.sp, Dim, modifier = Modifier.clickable { open = false })
            }
        }
        if (editing) {
            val focus = remember { FocusRequester() }
            BasicTextField(
                editVal,
                { editVal = it },
                Modifier
                    .width(180.dp)
                    .focusRequester(focus)
                    .drawBehind { drawLine(Accent, Offset(0f, size.height), Offset(size.width, size.height)) }
                    .padding(vertical = 4.dp),
                textStyle = TextStyle(Ink, 12.sp, fontFamily = Karla),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    store.editTag(s, editVal.trim().ifEmpty { null })
                    editing = false; open = false
                }),
                singleLine = true,
                cursorBrush = SolidColor(Accent),
            )
            LaunchedEffect(Unit) { focus.requestFocus() }
        }
    }
}
