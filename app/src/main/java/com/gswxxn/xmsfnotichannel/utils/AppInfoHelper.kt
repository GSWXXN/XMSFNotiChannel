package com.gswxxn.xmsfnotichannel.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

class AppInfoHelper(private val context : Context) {
    private lateinit var appInfoList: MutableList<MyAppInfo>
    var pattern: String = ""

    data class MyAppInfo(
        val appName: String,
        val packageName: String,
        val icon: Drawable,
        var ncInfo: NCInfo,
        val isSystemApp: Boolean
    )

    data class NCInfo(
        val channelGroupName: String,
        val channelName: String,
        var importance: Int
    )

    fun getAppInfoList(): MutableList<MyAppInfo> {
        if (::appInfoList.isInitialized)
            return appInfoList.apply {
                sortBy { it.appName }
                sortByDescending {it.ncInfo.importance }
            }.toMutableList()
        return getAppInfoListForNew()
    }

    fun getAppInfoListForNew(): MutableList<MyAppInfo> {
        appInfoList = mutableListOf()
        val pm = context.packageManager
        val ncUtils = NCUtils(context)
        for (appInfo in pm.getInstalledApplications(0)) {
            ncUtils.getNotificationChannelInfoByRegex(appInfo.packageName, pattern).forEach {
                appInfoList.add(MyAppInfo(
                    appInfo.loadLabel(pm).toString(),
                    appInfo.packageName,
                    appInfo.loadIcon(pm),
                    it,
                    appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                ))
            }
        }
        return appInfoList.apply {
            sortBy { it.appName }
            sortByDescending { it.ncInfo.importance }
        }.toMutableList()
    }
}