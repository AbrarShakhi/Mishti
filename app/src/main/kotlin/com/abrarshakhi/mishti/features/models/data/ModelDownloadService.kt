package com.abrarshakhi.mishti.features.models.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.abrarshakhi.mishti.R
import com.abrarshakhi.mishti.common.MainActivity
import com.abrarshakhi.mishti.features.models.domain.model.Transfer
import com.abrarshakhi.mishti.features.models.domain.model.TransferStatus
import com.abrarshakhi.mishti.features.models.domain.repository.ModelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class ModelDownloadService : Service() {
    private val repository: ModelRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observer: Job? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL_ALL) {
            cancelAll()
            return START_NOT_STICKY
        }

        startForegroundWith(
            buildNotification(
                title = getString(R.string.notification_preparing),
                progress = null,
            ),
        )
        observeDownloads()

        return START_NOT_STICKY
    }

    private fun observeDownloads() {
        if (observer?.isActive == true) return
        observer =
            repository.transfers
                .onEach { transfers -> render(transfers) }
                .launchIn(scope)
    }

    private fun render(transfers: List<Transfer>) {
        val active = transfers.filter { it.isActive }
        if (active.isEmpty()) {
            stopSelf()
            return
        }

        val transfer = active.first()
        val extra = active.size - 1

        val notification =
            when (val status = transfer.status) {
                is TransferStatus.Downloading -> {
                    buildNotification(
                        title = withMore(transfer.name, extra),
                        progress = (status.fraction * 100).toInt(),
                    )
                }

                is TransferStatus.Importing -> {
                    buildNotification(
                        title = withMore(
                            getString(R.string.notification_importing, transfer.name),
                            extra,
                        ),
                        progress = if (status.totalBytes >
                            0
                        ) {
                            (status.fraction * 100).toInt()
                        } else {
                            null
                        },
                    )
                }

                else -> {
                    buildNotification(
                        title = getString(R.string.notification_verifying, transfer.name),
                        progress = null,
                    )
                }
            }
        notificationManager()?.notify(NOTIFICATION_ID, notification)
    }

    private fun withMore(title: String, extra: Int): String = if (extra > 0) {
        resources.getQuantityString(
            R.plurals.notification_more,
            extra,
            title,
            extra,
        )
    } else {
        title
    }

    private fun cancelAll() {
        repository.cancelAll()
        stopSelf()
    }

    private fun startForegroundWith(notification: Notification) {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    private fun buildNotification(title: String, progress: Int?): Notification {
        ensureChannel()

        val open =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val cancel =
            PendingIntent.getService(
                this,
                1,
                Intent(this, ModelDownloadService::class.java).setAction(ACTION_CANCEL_ALL),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(
                if (progress == null) {
                    getString(R.string.notification_working)
                } else {
                    getString(R.string.notification_percent, progress)
                },
            ).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(open)
            .addAction(0, getString(R.string.action_cancel), cancel)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .apply {
                if (progress == null) setProgress(0, 0, true) else setProgress(100, progress, false)
            }.build()
    }

    private fun ensureChannel() {
        val manager = notificationManager() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_transfers),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) },
        )
    }

    private fun notificationManager(): NotificationManager? =
        ContextCompat.getSystemService(this, NotificationManager::class.java)

    override fun onDestroy() {
        observer?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "model_downloads"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_CANCEL_ALL = "com.abrarshakhi.mishti.CANCEL_DOWNLOADS"
    }
}
