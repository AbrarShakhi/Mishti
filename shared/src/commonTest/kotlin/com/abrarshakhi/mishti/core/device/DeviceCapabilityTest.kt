package com.abrarshakhi.mishti.core.device

import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceCapabilityTest {
    @Test
    fun `unknown and low memory devices are classified without crashing`() {
        assertEquals(DeviceCapability.Unknown, deviceCapabilityFor(0))
        assertEquals(DeviceCapability.Unknown, deviceCapabilityFor(-1))
        assertEquals(DeviceCapability.UnsupportedLowMemory(MIN_RAM_BYTES - 1), deviceCapabilityFor(MIN_RAM_BYTES - 1))
        assertEquals(DeviceCapability.Supported(MIN_RAM_BYTES), deviceCapabilityFor(MIN_RAM_BYTES))
        assertEquals(DeviceCapability.Supported(8_000_000_000L), deviceCapabilityFor(8_000_000_000L))
    }
}
