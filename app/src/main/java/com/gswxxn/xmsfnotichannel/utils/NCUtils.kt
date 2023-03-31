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

    private fun getNotificationChannelGroups(pkgName: String) =
        notificationChannelGroupsM.invoke(sINM, pkgName, context.packageManager.getPackageUid(pkgName, 0), false).let {
            Class.forName("android.content.pm.ParceledListSlice").getDeclaredMethod("getList")
                .invoke(it) as List<NotificationChannelGroup>
        }

    private fun setNotificationChannel(pkgName: String, channel: NotificationChannel): Any? =
        updateNotificationChannelForPackageM.invoke(
            sINM, pkgName, context.packageManager.getPackageUid(pkgName, 0), channel
        )

    // 获取通知通道信息
    fun getNotificationChannelInfoByRegex(pkgName: String, channelNameRegex: String): List<AppInfoHelper.NCInfo> {
        val ncInfoList = mutableListOf<AppInfoHelper.NCInfo>()
        getNotificationChannelGroups(pkgName).forEach{ notificationChannelGroup ->
            notificationChannelGroup.channels.forEach {
                if (it.name.matches(Regex(channelNameRegex)))
                    ncInfoList.add(AppInfoHelper.NCInfo(notificationChannelGroup.name.toString(), it.name.toString(), it.importance))
            }
        }
        return ncInfoList
    }

    fun enableSpecificNotification(appInfo: AppInfoHelper.MyAppInfo) {
        getNotificationChannelGroups(appInfo.packageName).forEach { group ->
            group.channels.forEach { channel ->
                channel.importance = NotificationManager.IMPORTANCE_DEFAULT
                setNotificationChannel(appInfo.packageName, channel)
            }
        }
    }

    fun disableSpecificNotification(appInfo: AppInfoHelper.MyAppInfo) {
        getNotificationChannelGroups(appInfo.packageName).forEach { group ->
            group.channels.forEach { channel ->
                channel.importance = NotificationManager.IMPORTANCE_NONE
                setNotificationChannel(appInfo.packageName, channel)
            }
        }
    }
}