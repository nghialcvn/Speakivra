package com.nghialcvn.speakivra

import java.time.LocalDate

internal const val CHECK_IN_DATES = "checkin.dates"
/** ISO calendar dates merge as a set, never as an incrementing counter across devices. */
internal fun validCheckInDates(values: Collection<String>): Set<String> = values.mapNotNull {
    runCatching { LocalDate.parse(it) }.getOrNull()?.takeIf { date -> date.toString() == it && date.year in 2000..9999 }
}.distinct().sorted().takeLast(3660).map { it.toString() }.toSet()

internal data class DailyCheckIn(val dates: Set<String>, val today: LocalDate) {
    val checkedToday get() = today.toString() in dates
    val streak: Int get() {
        var date = if (checkedToday) today else today.minusDays(1)
        var count = 0
        while (date.toString() in dates) { count++; date = date.minusDays(1) }
        return count
    }
    val week get() = (0L..6L).map { today.minusDays(today.dayOfWeek.value.toLong() - 1).plusDays(it) }
    fun checkIn() = validCheckInDates(dates + today.toString())
}
