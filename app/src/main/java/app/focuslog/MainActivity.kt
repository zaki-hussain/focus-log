package app.focuslog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Design tokens
val Bg = Color(0xFF0B0B0C)
val Ink = Color(0xFFE8E8E3)
val Dim = Color(0xFF7A7A74)
val Faint = Color(0xFF4A4A46)
val Line = Color(0xFF26262A)
val Accent = Color(0xFF6FD6E8)
val Pre = Color(0xFFA8837B)
val Good = Color(0xFF46C878)
val Fail = Color(0xFFD9604F)
val OnAccent = Color(0xFF07171B)
val OnGood = Color(0xFF071509)

val Karla = FontFamily(
    Font(R.font.karla_regular, FontWeight.Normal),
    Font(R.font.karla_medium, FontWeight.Medium),
    Font(R.font.karla_semibold, FontWeight.SemiBold),
    Font(R.font.karla_bold, FontWeight.Bold),
)
val Mono = FontFamily(
    Font(R.font.jetbrainsmono_light, FontWeight.Light),
    Font(R.font.jetbrainsmono_regular, FontWeight.Normal),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        setContent { App(store) }
    }
}

@Composable
fun App(store: Store) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(500)
        }
    }
    val pager = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(Bg)) {
        HorizontalPager(pager, Modifier.safeDrawingPadding()) { page ->
            if (page == 0) ClockScreen(store, now) { scope.launch { pager.animateScrollToPage(1) } }
            else StatsScreen(store, now) { scope.launch { pager.animateScrollToPage(0) } }
        }
    }
}

@Composable
fun Txt(
    text: String,
    size: TextUnit,
    color: Color = Ink,
    weight: FontWeight = FontWeight.Normal,
    family: FontFamily = Karla,
    spacing: TextUnit = 0.5.sp,
    align: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier,
) = BasicText(
    text, modifier,
    style = TextStyle(color, size, weight, fontFamily = family, letterSpacing = spacing, textAlign = align),
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
)

/** Pill chip used for minute presets and tags. */
@Composable
fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = if (selected) Accent else Line
    Box(
        Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, c, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Txt(label, 14.sp, if (selected) Accent else Dim)
    }
}

/** Understated outline button (Cancel / Terminate). */
@Composable
fun GhostButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Line, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp)
    ) {
        Txt(label, 13.sp, Dim, spacing = 1.sp)
    }
}

@Composable
fun Footer(store: Store, now: Long) {
    val today = store.sessions.totalSince(dayStart())
    val week = store.sessions.totalSince(weekStart())
    Txt(
        "Today ${fmtDur(today)} · This week ${fmtDur(week)}",
        12.sp, Dim, align = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    )
}
