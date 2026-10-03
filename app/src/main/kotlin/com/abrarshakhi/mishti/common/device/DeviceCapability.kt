package com.abrarshakhi.mishti.common.device

const val MIN_RAM_BYTES: Long = 3_200_000_000L

sealed interface DeviceCapability {

    val totalMemoryBytes: Long?

    data class Supported(override val totalMemoryBytes: Long?) : DeviceCapability

    data class UnsupportedLowMemory(
        override val totalMemoryBytes: Long,
        val requiredMemoryBytes: Long = MIN_RAM_BYTES,
    ) : DeviceCapability

    data object Unknown : DeviceCapability {
        override val totalMemoryBytes: Long? = null
    }
}

val DeviceCapability.isSupported: Boolean
    get() = this !is DeviceCapability.UnsupportedLowMemory
