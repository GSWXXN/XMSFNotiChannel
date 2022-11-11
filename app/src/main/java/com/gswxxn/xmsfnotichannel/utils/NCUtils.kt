package com.gswxxn.xmsfnotichannel.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.IBinder
import java.lang.reflect.Method

@SuppressLint("PrivateApi", "SoonBlockedPrivateApi", "DiscouragedPrivateApi")
class NCUtils(private val context : Context) {

    private val sINM by lazy {
        val notificationService = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService",
            String::class.java
        ).invoke(null, "notification")

        Class.forName("android.app.INotificationManager\$Stub").getDeclaredMethod("asInterface", IBinder::class.java)
            .invoke(null, notificationService)
    }

    private val notificationChannelGroupsM = sINM.javaClass.getDeclaredMethod(
        "getNotificationChannelGroupsForPackage",
        String::class.java,
        Int::class.java,
        Boolean::class.java) as Method

    private val updateNotificationChannelForPackageM = sINM.javaClass.getDeclaredMethod(
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
        notificationChannelGroupsM.invoke(sINM, pkgName, context.packageManager.getPackageUid(pkgName, 0), false).let {
            Class.forName("android.content.pm.ParceledListSlice").getDeclaredMethod("getList")
                .invoke(it) as List<NotificationChannelGroup>
        }

    private fun setNotificationChannel(pkgName: String, channel: NotificationChannel): Any? =
        updateNotificationChannelForPackageM.invoke(
            sINM, pkgName, context.packageManager.getPackageUid(pkgName, 0), channel
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