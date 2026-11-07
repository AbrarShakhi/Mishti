package com.abrarshakhi.mishti.core.device

import android.app.ActivityManager
import android.content.Context

class AndroidDeviceCapabilityProvider(private val context: Context) : DeviceCapabilityProvider {
    override fun capability(): DeviceCapability {
        val activityManager =
            context.getSystemService(ActivityManager::class.java)
                ?: return DeviceCapability.Unknown

        val info = ActivityManager.MemoryInfo()
        runCatching {
            activityManager.getMemoryInfo(info)
        }.getOrElse { return DeviceCapability.Unknown }

        val total = info.totalMem
        if (total <= 0L) return DeviceCapability.Unknown

        return if (total < MIN_RAM_BYTES) {
            DeviceCapability.UnsupportedLowMemory(totalMemoryBytes = total)
        } else {
            DeviceCapability.Supported(totalMemoryBytes = total)
        }
    }
}
