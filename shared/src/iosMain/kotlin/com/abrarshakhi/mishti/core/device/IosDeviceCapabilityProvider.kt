package com.abrarshakhi.mishti.core.device

import platform.Foundation.NSProcessInfo

class IosDeviceCapabilityProvider : DeviceCapabilityProvider {
    override fun capability(): DeviceCapability = deviceCapabilityFor(
        NSProcessInfo.processInfo.physicalMemory.coerceAtMost(Long.MAX_VALUE.toULong()).toLong(),
    )
}
