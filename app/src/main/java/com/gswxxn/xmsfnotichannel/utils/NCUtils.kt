package com.gswxxn.xmsfnotichannel.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import java.lang.reflect.Method

@SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
class NCUtils(private val context : Context) {

    private val clazz: Class<*> by lazy {
        context.createPackageContext(
            "com.android.settings",
            Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
        ).classLoader.loadClass(
            "com.android.server.notification.NotificationManagerServiceCompat")
    }

    private val notificationChannelGroupsM = clazz.getDeclaredMethod(
        "getNotificationChannelGroupsForPackage",
        String::class.java,
        Int::class.java,
        Boolean::class.java) as Method

    private val updateNotificationChannelForPackageM = clazz.getDeclaredMethod(
        "updateNotificationChannelForPackage",
        String::class.java,
        Int::class.java,
        NotificationChannel::class.java) as Method

    private fun setOperationNotification(pkgName: String, importance: Int) {
        getNotificationChannelGroups(pkgName).forEach { group ->
            group.channels.forEach { channel ->
                if (channel.name == "运营消息") {
                    channel.importance = importance
                    setNotificationChannel(pkgName, channel)
                }
            }
        }
    }

    private fun getNotificationChannelGroups(pkgName: String) =
        notificationChannelGroupsM.invoke(null, pkgName, context.packageManager.getPackageUid(pkgName, 0), false).let {
            Class.forName("android.content.pm.ParceledListSlice").getDeclaredMethod("getList")
                .invoke(it) as List<NotificationChannelGroup>
        }

    private fun setNotificationChannel(pkgName: String, channel: NotificationChannel): Any? =
        updateNotificationChannelForPackageM.invoke(
            null, pkgName, context.packageManager.getPackageUid(pkgName, 0), channel
        )

    fun getNotificationChannelImportance(pkgName: String, channelName: String): Int {
        getNotificationChannelGroups(pkgName).forEach{ notificationChannelGroup ->
            notificationChannelGroup.channels.forEach {
                if (it.name == channelName) return it.importance
            }
        }
        return -1
    }

    fun disableOperationNotification(pkgName: String) =
        setOperationNotification(pkgName, NotificationManager.IMPORTANCE_NONE)

    fun enableOperationNotification(pkgName: String) =
        setOperationNotification(pkgName, NotificationManager.IMPORTANCE_DEFAULT)
}