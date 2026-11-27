package com.nghialcvn.speakivra

import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class DailyCheckInTest {
    private val day = LocalDate.of(2026, 11, 27)
    @Test fun oneCheckInPerCalendarDayAndMissedDayBreaksStreak() {
        val old = setOf("2026-11-25", "2026-11-26")
        val before = DailyCheckIn(old, day)
        assertEquals(2, before.streak); assertFalse(before.checkedToday)
        val after = DailyCheckIn(before.checkIn(), day)
        assertEquals(3, after.streak); assertTrue(after.checkedToday)
        assertEquals(after.dates, after.checkIn())
        assertEquals(3, DailyCheckIn(after.dates, day.plusDays(1)).streak)
        assertEquals(0, DailyCheckIn(after.dates, day.plusDays(2)).streak)
        assertEquals(1, DailyCheckIn(DailyCheckIn(after.dates, day.plusDays(2)).checkIn(), day.plusDays(2)).streak)
    }
    @Test fun weekAndMonthBoundaryUseCalendarDatesAndIgnoreFutureStreaks() {
        val now = LocalDate.of(2027, 1, 1)
        assertEquals(3, DailyCheckIn(setOf("2026-12-30", "2026-12-31", "2027-01-01", "2027-01-05"), now).streak)
        assertEquals(LocalDate.of(2026, 12, 28), DailyCheckIn(emptySet(), now).week.first())
        assertEquals(7, DailyCheckIn(emptySet(), now).week.size)
        assertEquals(setOf("2026-11-27"), validCheckInDates(listOf("2026-11-27", "2026-02-30", "audio", "2026-1-1")))
    }
    @Test fun cloudMergePreservesDistinctDaysAcrossDevicesAndWhitelistsImports() {
        val local = JSONObject().put(CHECK_IN_DATES, JSONArray(listOf("2026-11-25", "2026-11-27")))
        val remote = JSONObject().put(CHECK_IN_DATES, JSONArray(listOf("2026-11-26", "2026-11-27", "password")))
        val merged = mergeCloudProgress(local, remote)
        val clean = sanitizedCloudProgress("a".repeat(64), merged)
        assertEquals(setOf("2026-11-25", "2026-11-26", "2026-11-27"), cloudProgressValues(clean)[CHECK_IN_DATES])
        assertEquals(clean.toString(), sanitizedCloudProgress("a".repeat(64), mergeCloudProgress(clean, remote)).toString())
    }
}
