package com.abrarshakhi.mishti.chat.ui

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatMessageTime(millis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
    val calendar = NSCalendar.currentCalendar
    val isToday = calendar.isDateInToday(date)
    val formatter = NSDateFormatter().apply {
        dateStyle = if (isToday) NSDateFormatterNoStyle else NSDateFormatterShortStyle
        timeStyle = NSDateFormatterShortStyle
    }
    return formatter.stringFromDate(date)
}
