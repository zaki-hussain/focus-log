package app.focuslog

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Session(
    val start: Long,
    val end: Long,
    val minimum: Long,
    val tag: String?,
    val failed: Boolean,
) {
    val duration get() = end - start
}

data class Active(val start: Long, val minimum: Long, val tag: String?)

/** The whole app's state: a session log, an optional active session, and two small preferences. */
class Store(context: Context) {
    private val sp = context.getSharedPreferences("focuslog", Context.MODE_PRIVATE)

    var sessions by mutableStateOf(loadSessions())
        private set
    var active by mutableStateOf(loadActive())
        private set
    var lastMin by mutableStateOf(sp.getInt("lastMin", 60))
        private set
    var tags by mutableStateOf(loadTags())
        private set

    fun setMinimum(minutes: Int) {
        lastMin = minutes.coerceIn(5, 240)
        sp.edit().putInt("lastMin", lastMin).apply()
    }

    fun addTag(tag: String) {
        tags = (listOf(tag) + tags.filter { it != tag }).take(6)
        sp.edit().putString("tags", JSONArray(tags).toString()).apply()
    }

    fun start(tag: String?) {
        active = Active(System.currentTimeMillis(), lastMin * 60_000L, tag)
        saveActive()
    }

    fun cancel() {
        active = null
        saveActive()
    }

    fun end(failed: Boolean) {
        val a = active ?: return
        sessions = sessions + Session(a.start, System.currentTimeMillis(), a.minimum, a.tag, failed)
        active = null
        saveActive()
        saveSessions()
    }

    fun delete(s: Session) {
        sessions = sessions - s
        saveSessions()
    }

    fun editTag(s: Session, tag: String?) {
        sessions = sessions.map { if (it == s) it.copy(tag = tag) else it }
        saveSessions()
    }

    fun exportCsv() = buildString {
        append("start_iso,end_iso,duration_min,minimum_min,tag,status\n")
        sessions.forEach {
            append(Instant.ofEpochMilli(it.start)).append(',')
            append(Instant.ofEpochMilli(it.end)).append(',')
            append(it.duration / 60_000).append(',')
            append(it.minimum / 60_000).append(',')
            append(it.tag ?: "").append(',')
            append(if (it.failed) "failed" else "completed").append('\n')
        }
    }

    private fun loadSessions(): List<Session> {
        val arr = JSONArray(sp.getString("sessions", "[]"))
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Session(o.getLong("start"), o.getLong("end"), o.getLong("min"),
                o.optString("tag").ifEmpty { null }, o.getBoolean("failed"))
        }
    }

    private fun saveSessions() {
        val arr = JSONArray()
        sessions.forEach {
            arr.put(JSONObject()
                .put("start", it.start).put("end", it.end).put("min", it.minimum)
                .put("tag", it.tag ?: "").put("failed", it.failed))
        }
        sp.edit().putString("sessions", arr.toString()).apply()
    }

    private fun loadActive(): Active? = sp.getString("active", null)?.let {
        val o = JSONObject(it)
        Active(o.getLong("start"), o.getLong("min"), o.optString("tag").ifEmpty { null })
    }

    private fun saveActive() {
        sp.edit().putString("active", active?.let {
            JSONObject().put("start", it.start).put("min", it.minimum).put("tag", it.tag ?: "").toString()
        }).apply()
    }

    private fun loadTags(): List<String> {
        val arr = JSONArray(sp.getString("tags", "[]"))
        return List(arr.length()) { i -> arr.getString(i) }
    }
}

val zone: ZoneId get() = ZoneId.systemDefault()
const val DAY = 86_400_000L

fun dayStart(): Long = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
fun weekStart(): Long = dayStart() - (LocalDate.now().dayOfWeek.value - 1) * DAY
fun monthStart(): Long = LocalDate.now().withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
fun yearStart(): Long = LocalDate.now().withDayOfYear(1).atStartOfDay(zone).toInstant().toEpochMilli()

fun List<Session>.totalSince(t: Long) = filter { !it.failed && it.start >= t }.sumOf { it.duration }

/** "2h 40m" / "45m" */
fun fmtDur(ms: Long): String {
    val m = ((ms + 30_000) / 60_000).toInt()
    return if (m >= 60) "${m / 60}h ${"%02d".format(m % 60)}m" else "${m}m"
}

/** Minimum always in MM:SS, never rolling into hours: "90:00", "120:00" */
fun fmtMin(ms: Long) = "%02d:00".format(ms / 60_000)

/** Elapsed clock: "mm:ss", rolling to "h:mm:ss" past an hour */
fun fmtElapsed(ms: Long): String {
    val s = ms / 1000
    return if (s >= 3600) "%d:%02d:%02d".format(s / 3600, s / 60 % 60, s % 60)
    else "%02d:%02d".format(s / 60, s % 60)
}
