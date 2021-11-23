package com.san.kir.manger.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.san.kir.ankofork.browse
import com.san.kir.ankofork.intentFor
import com.san.kir.ankofork.sdk28.notificationManager
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ConnectManager
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class AppUpdateService : Service(), CoroutineScope {
    companion object {
        const val ACTION_CANCEL_ALL = "kir.san.manger.AppUpdateService.CANCEL_ALL"
        const val ACTION_GO_TO_SITE = "kir.san.manger.AppUpdateService.GO_TO_SITE"

        private const val channelId = "AppUpdaterId"
        private const val name = "AppUpdaterServiceName"
        private const val descriptions = "AppUpdaterServiceDescription"

        private const val url = "http://4pda.to/forum/index.php?showtopic=772886&st=0#entry53336845"
    }

    lateinit var job: Job

    private var notificationId = ID.generate()
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

    @Inject
    lateinit var connectManager: ConnectManager

    override fun onBind(intent: Intent?) = null

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()

        job = Job()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && notificationManager.getNotificationChannel(channelId) == null
        ) {

            val importance = NotificationManager.IMPORTANCE_LOW

            NotificationChannel(channelId, name, importance).apply {
                description = descriptions
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_CANCEL_ALL -> {
                stopForeground(true)
                notificationManager.cancel(notificationId)
                stopSelf()
            }
            ACTION_GO_TO_SITE -> {
                browse(url)
                stopForeground(true)
                notificationManager.cancel(notificationId)
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

                    val doc = connectManager.getDocument(url)
                    val texts = doc.body().wholeText().split("MANGa readER").last()
                    val matcher = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+")
                        .matcher(texts)

                    if (matcher.find()) {
                        val version = matcher.group()
                        log("version = $version")
                        val message = if (version != BuildConfig.VERSION_NAME)
                            getString(
                                R.string.main_check_app_ver_find,
                                version,
                                BuildConfig.VERSION_NAME
                            )
                        else
                            getString(R.string.main_check_app_ver_no_find)
                        stopForeground(false)
                        notificationManager.cancel(notificationId)
                        with(NotificationCompat.Builder(this@AppUpdateService, channelId)) {
                            setSmallIcon(R.drawable.ic_notification_update)
                            setContentTitle(getString(R.string.app_update_service_title))
                            setContentText(message)
                            addAction(actionGoToSite)
                            notificationManager.notify(notificationId, build())
                        }
                        notificationId = ID.generate()
                    } else {
                        throw Throwable("not find in $matcher")
                    }
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    stopForeground(false)
                    notificationManager.cancel(notificationId)
                    with(NotificationCompat.Builder(this@AppUpdateService, channelId)) {
                        setSmallIcon(R.drawable.ic_notification_update)
                        setContentTitle(getString(R.string.app_update_service_title))
                        setContentText(getString(R.string.main_check_app_ver_error))
                        notificationManager.notify(notificationId, build())
                    }
                    notificationId = ID.generate()
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
