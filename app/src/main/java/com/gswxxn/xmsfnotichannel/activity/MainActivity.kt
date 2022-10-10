package com.gswxxn.xmsfnotichannel.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.gswxxn.xmsfnotichannel.BuildConfig
import com.gswxxn.xmsfnotichannel.R
import com.gswxxn.xmsfnotichannel.databinding.ActivityMainBinding
import com.gswxxn.xmsfnotichannel.databinding.AdapterConfigBinding
import com.gswxxn.xmsfnotichannel.utils.AppInfoHelper
import com.gswxxn.xmsfnotichannel.utils.NCUtils
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.highcapable.yukihookapi.hook.factory.hasClass
import kotlinx.coroutines.*

class MainActivity : BaseActivity(), CoroutineScope by MainScope() {
    private var androidRestartNeeded = true
    private val isMIUI = "android.miui.R".hasClass()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate() {
        var appInfoFilter = listOf<AppInfoHelper.MyAppInfo>()
        val appInfo = AppInfoHelper(this)
        var onRefreshList: (() -> Unit)? = null
        val nc by lazy { NCUtils(this) }
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(root) }


        binding.titleAboutPage.setOnClickListener {
            val intent = Intent(this, AboutPageActivity::class.java)
            startActivity(intent)
        }

        // 显示版本号
        binding.mainTextVersion.text = getString(R.string.module_version, BuildConfig.VERSION_NAME)

        // 开始检测
        binding.actionButton.setOnClickListener {
            if (!isMIUI || androidRestartNeeded) {
                Toast.makeText(this, getString(R.string.check_active_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showView(false, binding.actionButton)
            showView(true, binding.configListLoadingView)
            launch {
                appInfoFilter = withContext(Dispatchers.Default) { appInfo.getAppInfoList() }

                showView(false, binding.configListLoadingView)
                showView(true, binding.configListView, binding.afterActions)
            }
        }

        // 一键开启
        binding.enableAll.setOnClickListener {
            appInfoFilter.forEach {
                if (it.status == 0) {
                    nc.enableOperationNotification(it.packageName)
                    it.status = 3
                }
            }
            onRefreshList?.invoke()
        }

        // 一键关闭
        binding.disableAll.setOnClickListener {
            appInfoFilter.forEach {
                if (it.status > 0) {
                    nc.disableOperationNotification(it.packageName)
                    it.status = 0
                }
            }
            onRefreshList?.invoke()
        }

        // 重新检测
        binding.recheck.setOnClickListener {
            launch {
                showView(false, binding.configListView, binding.afterActions)
                showView(true, binding.configListLoadingView)
                appInfoFilter = withContext(Dispatchers.Default) { appInfo.getAppInfoListForNew() }
                delay(500)
                showView(true, binding.configListView, binding.afterActions)
                showView(false, binding.configListLoadingView)
                onRefreshList?.invoke()
            }
        }

        // 列表
        binding.configListView.apply {
            adapter = object : BaseAdapter() {
                override fun getCount() = appInfoFilter.size

                override fun getItem(position: Int) = appInfoFilter[position]

                override fun getItemId(position: Int) = position.toLong()

                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    var cView = convertView
                    val holder: AdapterConfigBinding
                    if (convertView == null) {
                        holder = AdapterConfigBinding.inflate(LayoutInflater.from(context))
                        cView = holder.root
                        cView.tag = holder
                    }else {
                        holder = cView?.tag as AdapterConfigBinding
                    }
                    getItem(position).also {
                        // 设置图标
                        holder.adpAppIcon.setImageDrawable(it.icon)
                        // 设置应用名
                        holder.adpAppName.text = it.appName
                        // 设置状态
                        holder.adpAppStatus.apply {
                            text = getString(when (it.status) {
                                -1 -> R.string.did_not_sent_operation_notification
                                0 -> R.string.disabled_operation_notification
                                else -> R.string.enabled_operation_notification
                            })
                            setTextColor(getColor(
                                when (it.status) {
                                    -1 -> R.color.colorTextGray
                                    0 -> R.color.green
                                    else -> R.color.colorTextRed
                                }
                            ))
                        }
                        // 设置LinearLayout
                        holder.adapterLayout.setOnClickListener { _ ->
                            when (it.status) {
                                -1 -> return@setOnClickListener
                                0 -> {
                                    nc.enableOperationNotification(it.packageName)
                                    holder.adpAppStatus.apply {
                                        text = getString(R.string.enabled_operation_notification)
                                        setTextColor(getColor(R.color.colorTextRed))
                                    }
                                    it.status = 3
                                }
                                else -> {
                                    nc.disableOperationNotification(it.packageName)
                                    holder.adpAppStatus.apply {
                                        text = getString(R.string.disabled_operation_notification)
                                        setTextColor(getColor(R.color.green))
                                    }
                                    it.status = 0
                                }
                            }
                        }
                    }
                    return cView
                }
            }.apply{ onRefreshList = { notifyDataSetChanged() } }
        }
    }

    private fun refreshState() {

        binding.mainStatus.setBackgroundResource(
            when {
                YukiHookAPI.Status.isXposedModuleActive && (!isMIUI || androidRestartNeeded) -> R.drawable.bg_yellow_round
                YukiHookAPI.Status.isXposedModuleActive -> R.drawable.bg_green_round
                else -> R.drawable.bg_dark_round
            }
        )
        binding.mainImgStatus.setImageResource(
            when {
                YukiHookAPI.Status.isXposedModuleActive && isMIUI && !androidRestartNeeded -> R.drawable.ic_success
                else -> R.drawable.ic_warn
            }
        )
        binding.mainTextStatus.text = getString(
            when {
                !isMIUI -> R.string.only_miui
                YukiHookAPI.Status.isXposedModuleActive && androidRestartNeeded -> R.string.try_reboot
                YukiHookAPI.Status.isXposedModuleActive -> R.string.module_is_active
                else -> R.string.module_is_not_active
            })
        showView(YukiHookAPI.Status.isXposedModuleActive, binding.mainTextApiWay)
        binding.mainTextApiWay.text =
            getString(R.string.xposed_framework_version,
                YukiHookAPI.Status.executorName,
                YukiHookAPI.Status.executorVersion)

        window.statusBarColor = getColor(
            when {
                YukiHookAPI.Status.isXposedModuleActive && (!isMIUI || androidRestartNeeded) -> R.color.yellow
                YukiHookAPI.Status.isXposedModuleActive -> R.color.green
                else -> R.color.gray
            }
        )
    }

    override fun onResume() {
        super.onResume()
        refreshState()
        dataChannel("android").checkingVersionEquals {
            androidRestartNeeded = !it
            refreshState()
        }
    }
}