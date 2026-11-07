package com.abrarshakhi.mishti.chat.ui

import com.abrarshakhi.mishti.chat.domain.model.ChatSession
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalForeignApi::class)
class SessionGroupsIOSTest {
    @Test
    fun `a conversation just before midnight belongs to yesterday`() {
        val calendar = NSCalendar.currentCalendar
        val midnight = calendar.startOfDayForDate(NSDate())
        val before = requireNotNull(calendar.dateByAddingUnit(NSCalendarUnitMinute, -10, midnight, 0u))
        val after = requireNotNull(calendar.dateByAddingUnit(NSCalendarUnitMinute, 10, midnight, 0u))
        val updated = (before.timeIntervalSince1970 * 1000).toLong()
        val now = (after.timeIntervalSince1970 * 1000).toLong()
        val groups = groupSessionsByRecency(listOf(ChatSession("previous", "Previous", updated, updated)), now)
        assertEquals(Recency.Yesterday, groups.single().recency)
    }
}
