package com.abrarshakhi.mishti.core.llm.data

import platform.Foundation.NSDate
import platform.Foundation.NSProcessInfo
import platform.Foundation.timeIntervalSince1970

internal actual fun platformTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

internal actual fun platformDefaultThreads(): Int =
    (NSProcessInfo.processInfo.processorCount.toInt() / 2).coerceIn(2, 6)
