package com.abrarshakhi.mishti.features.models.domain.model

import com.abrarshakhi.mishti.common.device.MIN_RAM_BYTES
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToLong

enum class MemoryFit { Fits, Tight, TooBig, Unknown }

private const val RUNTIME_BYTES = 2_800_000_000L
private const val WEIGHTS_FACTOR = 1.5
private const val COMFORT_FACTOR = 1.15

fun estimatedRamBytes(sizeBytes: Long): Long =
    max(MIN_RAM_BYTES, (sizeBytes * WEIGHTS_FACTOR).roundToLong() + RUNTIME_BYTES)

fun memoryFit(requiredBytes: Long, deviceBytes: Long?): MemoryFit = when {
    deviceBytes == null -> MemoryFit.Unknown
    deviceBytes >= requiredBytes * COMFORT_FACTOR -> MemoryFit.Fits
    deviceBytes >= requiredBytes -> MemoryFit.Tight
    else -> MemoryFit.TooBig
}

fun parametersLabel(parameters: Long?): String? {
    if (parameters == null || parameters <= 0) return null
    val billions = parameters / 1_000_000_000.0
    return if (billions >= 0.95) {
        String.format(Locale.US, "%.1fB", billions).replace(".0B", "B")
    } else {
        "${(parameters / 1_000_000.0).roundToLong()}M"
    }
}
