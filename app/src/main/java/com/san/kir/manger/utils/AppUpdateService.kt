package com.san.kir.manger.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.anko.browse
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

class AppUpdateService : Service(), CoroutineScope {
    companion object {
        const val ACTION_CANCEL_ALL = "kir.san.manger.AppUpdateService.CANCEL_ALL"
        const val ACTION_GO_TO_SITE = "kir.san.manger.AppUpdateService.GO_TO_SITE"

        private const val channelId = "AppUpdaterId"
        private const val name = "AppUpdaterServiceName"
        private const val description = "AppUpdaterServiceDescription"

        private const val url = "http://4pda.ru/forum/index.php?showtopic=772886&st=0#entry53336845"
    }

    lateinit var job: Job

    private val notificationId = ID.generate()
    private val actionCancelAll by lazy {
        val intent = intentFor<AppUpdateService>().setAction(ACTION_CANCEL_ALL)
        val cancelAll = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
            .Action
            .Builder(
                R.drawable.ic_notification_cancel,
                getString(R.string.app_update_service_action_cancel_all),
                cancelAll
            )
            .build()
    }

    private val actionGoToSite by lazy {
        val intent = intentFor<AppUpdateService>().setAction(ACTION_GO_TO_SITE)
        val cancelAll = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
            .Action
            .Builder(
                R.drawable.ic_action_search,
                getString(R.string.main_check_app_ver_go_to),
                cancelAll
            )
            .build()
    }

    override fun onBind(intent: Intent?) = null

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()

        job = Job()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                NotificationChannel(channelId, name, importance).apply {
                    description = AppUpdateService.description
                    notificationManager.createNotificationChannel(this)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when {
            intent.action == ACTION_CANCEL_ALL -> {
                stopForeground(true)
                stopSelf()
            }
            intent.action == ACTION_GO_TO_SITE -> {
                browse(url)
                stopForeground(true)
            }
            else -> launch(coroutineContext) {
                try {
                    with(NotificationCompat.Builder(this@AppUpdateService, channelId)) {
                        setSmallIcon(R.drawable.ic_notification_update)
                        setContentTitle(getString(R.string.app_update_service_title))
                        setProgress(0, 0, true)
                        addAction(actionCancelAll)
                        startForeground(notificationId, build())
                    }

                    val doc = ManageSites.getDocument(url)
                    val matcher = Pattern.compile("[0-9]\\.[0-9]\\.[0-9]")
                        .matcher(doc.select("#post-53336845 span > b").text())

                    if (matcher.find()) {
                        val version = matcher.group()
                        val message = if (version != BuildConfig.VERSION_NAME)
                            getString(
                                R.string.main_check_app_ver_find,
                                version,
                                BuildConfig.VERSION_NAME
                            )
                        else
                            getString(R.string.main_check_app_ver_no_find)

                        stopForeground(false)

                        with(NotificationCompat.Builder(this@AppUpdateService, channelId)) {
                            setSmallIcon(R.drawable.ic_notification_update)
                            setContentTitle(getString(R.string.app_update_service_title))
                            setContentText(message)
                            addAction(actionGoToSite)
                            notificationManager.notify(notificationId, build())
                        }

                    } else {

                    }
                } catch (ex: Throwable) {
                    stopForeground(false)

                    with(NotificationCompat.Builder(this@AppUpdateService, channelId)) {
                        setSmallIcon(R.drawable.ic_notification_update)
                        setContentTitle(getString(R.string.app_update_service_title))
                        setContentText(getString(R.string.main_check_app_ver_error))
                        notificationManager.notify(notificationId, build())
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
