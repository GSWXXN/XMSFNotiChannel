package com.gswxxn.xmsfnotichannel.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

class AppInfoHelper(private val context : Context) {
    private lateinit var appInfoList: MutableList<MyAppInfo>

    data class MyAppInfo(
        val appName: String,
        val packageName: String,
        val icon: Drawable,
        var status: Int,
        val isSystemApp: Boolean
    )

    fun getAppInfoList(): MutableList<MyAppInfo> {
        if (::appInfoList.isInitialized)
            return appInfoList.apply {
                sortBy { it.appName }
                sortByDescending {it.status }
            }.toMutableList()
        return getAppInfoListForNew()
    }

    fun getAppInfoListForNew(): MutableList<MyAppInfo> {
        appInfoList = mutableListOf()
        val pm = context.packageManager
        val ncUtils = NCUtils(context)
        for (appInfo in pm.getInstalledApplications(0)) {
            MyAppInfo(
                appInfo.loadLabel(pm).toString(),
                appInfo.packageName,
                appInfo.loadIcon(pm),
                ncUtils.getNotificationChannelImportance(appInfo.packageName, "运营消息"),
                appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
            ).also { appInfoList.add(it) }
        }
        return appInfoList.apply {
            sortBy { it.appName }
            sortByDescending { it.status }
        }.toMutableList()
    }

    fun setStatus(info : MyAppInfo, status : Int) {
        appInfoList[appInfoList.indexOf(info)].status = status
    }
}