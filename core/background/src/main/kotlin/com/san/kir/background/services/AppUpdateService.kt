package com.san.kir.background.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.san.kir.background.R
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.ID
import com.san.kir.core.utils.browse
import com.san.kir.core.utils.intentFor
import com.san.kir.core.utils.startService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
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

        fun start(ctx: Context) {
            startService<AppUpdateService>(ctx)
        }
    }

    lateinit var job: Job

    private var notificationId = ID.generate()
    private val actionCancelAll by lazy {
        val intent = intentFor<AppUpdateService>(this).setAction(ACTION_CANCEL_ALL)
        val cancelAll = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        NotificationCompat
            .Action
            .Builder(
                R.drawable.ic_notification_cancel,
                getString(R.string.cancel_all),
                cancelAll
            )
            .build()
    }

    private val actionGoToSite by lazy {
        val intent = intentFor<AppUpdateService>(this).setAction(ACTION_GO_TO_SITE)
        val cancelAll = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        NotificationCompat
            .Action
            .Builder(
                R.drawable.ic_action_search,
                getString(R.string.go_to_4pda),
                cancelAll
            )
            .build()
    }

    @Inject
    lateinit var connectManager: ConnectManager

    override fun onBind(intent: Intent?) = null

    private val notificationManager by lazy {
        NotificationManagerCompat.from(this)
    }

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
                        setContentTitle(getString(R.string.finding_app_updates))
                        setProgress(0, 0, true)
                        addAction(actionCancelAll)
                        startForeground(notificationId, build())
                    }

                    val doc = connectManager.getText(url)
                    val texts = doc.split("MANGa readER").last()
                    val matcher = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+")
                        .matcher(texts)

                    if (matcher.find()) {
                        val version = matcher.group()
                        Timber.v("version = $version")
                        val message = if (version != appVersion)
                            getString(
                                R.string.new_version_current_version_format,
                                version,
                                appVersion
                            )
                        else
                            getString(R.string.you_have_installed_actual_version)
                        stopForeground(false)
                        notificationManager.cancel(notificationId)
                        with(NotificationCompat.Builder(this@AppUpdateService, channelId)) {
                            setSmallIcon(R.drawable.ic_notification_update)
                            setContentTitle(getString(R.string.finding_app_updates))
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
                        setContentTitle(getString(R.string.finding_app_updates))
                        setContentText(getString(R.string.error_during_update_finding))
                        notificationManager.notify(notificationId, build())
                    }
                    notificationId = ID.generate()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val appVersion by lazy {
        kotlin.runCatching {
            applicationContext.packageManager.getPackageInfo(packageName, 0).versionName
        }.getOrNull() ?: ""
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
