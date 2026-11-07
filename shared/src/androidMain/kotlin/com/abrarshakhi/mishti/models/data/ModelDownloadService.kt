package com.abrarshakhi.mishti.models.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import com.abrarshakhi.mishti.models.domain.model.Transfer
import com.abrarshakhi.mishti.models.domain.model.TransferStatus
import com.abrarshakhi.mishti.models.domain.repository.ModelRepository
import com.abrarshakhi.mishti.shared.resources.Res
import com.abrarshakhi.mishti.shared.resources.action_cancel
import com.abrarshakhi.mishti.shared.resources.notification_channel_transfers
import com.abrarshakhi.mishti.shared.resources.notification_importing
import com.abrarshakhi.mishti.shared.resources.notification_percent
import com.abrarshakhi.mishti.shared.resources.notification_verifying
import com.abrarshakhi.mishti.shared.resources.notification_working
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.android.ext.android.inject

class ModelDownloadService : Service() {
    private val repository: ModelRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observer: Job? = null
    private val manager get() = getSystemService(NotificationManager::class.java)

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL_ALL) {
            repository.cancelAll()
            stopSelf()
            return START_NOT_STICKY
        }
        // Enter the foreground immediately; localized progress replaces this initial notification.
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Model transfers", NotificationManager.IMPORTANCE_LOW),
        )
        startForeground(
            NOTIFICATION_ID,
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true).build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        if (observer?.isActive != true) {
            observer = scope.launch {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        getString(Res.string.notification_channel_transfers),
                        NotificationManager.IMPORTANCE_LOW,
                    ),
                )
                repository.transfers.collect { transfers -> render(transfers) }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun render(transfers: List<Transfer>) {
        val active = transfers.filter { it.isActive }
        if (active.isEmpty()) {
            stopSelf()
            return
        }
        val transfer = active.first()
        val progress = when (val status = transfer.status) {
            is TransferStatus.Downloading -> (status.fraction * 100).toInt()
            is TransferStatus.Importing -> if (status.totalBytes > 0) (status.fraction * 100).toInt() else null
            else -> null
        }
        val title = when (transfer.status) {
            is TransferStatus.Importing -> getString(Res.string.notification_importing, transfer.name)
            TransferStatus.Verifying -> getString(Res.string.notification_verifying, transfer.name)
            else -> transfer.name
        }
        val cancel = PendingIntent.getService(
            this,
            1,
            Intent(this, ModelDownloadService::class.java).setAction(ACTION_CANCEL_ALL),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(
                if (progress == null) {
                    getString(Res.string.notification_working)
                } else {
                    getString(Res.string.notification_percent, progress)
                },
            )
            .setProgress(100, progress ?: 0, progress == null)
            .setOnlyAlertOnce(true).setOngoing(true)
            .addAction(Notification.Action.Builder(null, getString(Res.string.action_cancel), cancel).build())
        packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
            notification.setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    launch,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }
        manager.notify(NOTIFICATION_ID, notification.build())
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        repository.cancelAll()
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val CHANNEL_ID = "model_downloads"
        const val NOTIFICATION_ID = 1001
        const val ACTION_CANCEL_ALL = "com.abrarshakhi.mishti.CANCEL_DOWNLOADS"
    }
}
