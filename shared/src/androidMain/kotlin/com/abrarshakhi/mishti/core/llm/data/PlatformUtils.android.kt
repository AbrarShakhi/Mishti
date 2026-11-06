package com.abrarshakhi.mishti.core.llm.data

internal actual fun platformTimeMillis(): Long = System.currentTimeMillis()

internal actual fun platformDefaultThreads(): Int = (Runtime.getRuntime().availableProcessors() / 2).coerceIn(2, 6)
