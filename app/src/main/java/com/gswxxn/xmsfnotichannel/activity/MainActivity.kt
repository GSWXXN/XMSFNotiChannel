package com.gswxxn.xmsfnotichannel.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.gswxxn.xmsfnotichannel.BuildConfig
import com.gswxxn.xmsfnotichannel.R
import com.gswxxn.xmsfnotichannel.databinding.ActivityMainBinding
import com.gswxxn.xmsfnotichannel.databinding.AdapterConfigBinding
import com.gswxxn.xmsfnotichannel.utils.AppInfoHelper
import com.gswxxn.xmsfnotichannel.utils.NCUtils
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.factory.dataChannel
import kotlinx.coroutines.*

class MainActivity : BaseActivity(), CoroutineScope by MainScope() {

    companion object {
        const val defaultChannelName = "运营消息"
    }

    private var androidRestartNeeded = true
    private lateinit var binding: ActivityMainBinding
    private var channelName = defaultChannelName

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
            if (androidRestartNeeded) {
                Toast.makeText(this, getString(R.string.check_active_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showView(false, binding.actionButton)
            showView(true, binding.configListLoadingView)
            appInfo.pattern = channelName
            launch {
                appInfoFilter = withContext(Dispatchers.Default) { appInfo.getAppInfoList() }

                showView(false, binding.configListLoadingView)
                showView(true, binding.configListView, binding.afterActions)
            }
        }

        // 一键开启
        binding.enableAll.setOnClickListener {
            appInfoFilter.forEach {
                if (it.ncInfo.importance == 0) {
                    nc.enableSpecificNotification(it)
                    it.ncInfo.importance = 3
                }
            }
            onRefreshList?.invoke()
        }

        // 一键关闭
        binding.disableAll.setOnClickListener {
            appInfoFilter.forEach {
                if (it.ncInfo.importance > 0) {
                    nc.disableSpecificNotification(it)
                    it.ncInfo.importance = 0
                }
            }
            onRefreshList?.invoke()
        }

        // 重新检测
        binding.recheck.setOnClickListener {
            appInfo.pattern = channelName
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
                        val openHint: String
                        val closeHint: String
                        if (channelName == defaultChannelName) {
                            holder.adpChannelInfo.visibility = View.GONE
                            openHint = getString(R.string.enabled_operation_notification)
                            closeHint = getString(R.string.disabled_operation_notification)
                        } else {
                            holder.adpChannelInfo.visibility = View.VISIBLE
                            openHint = getString(R.string.enabled_notification_other)
                            closeHint = getString(R.string.disabled_notification_other)
                        }

                        // 设置图标
                        holder.adpAppIcon.setImageDrawable(it.icon)
                        // 设置应用名
                        holder.adpAppName.text = it.appName
                        // 设置组名
                        holder.adpChannelGroup.text = it.ncInfo.channelGroupName.let { name -> if (name == "") getString(R.string.no_group) else name }
                        // 设置通道名
                        holder.adpChannelName.text = it.ncInfo.channelName
                        // 设置状态
                        holder.adpAppStatus.apply {
                            text = when (it.ncInfo.importance) {
                                0 -> closeHint
                                else -> openHint
                            }
                            setTextColor(getColor(
                                when (it.ncInfo.importance) {
                                    -1 -> R.color.colorTextGray
                                    0 -> R.color.colorTextRed
                                    else -> R.color.green
                                }
                            ))
                        }
                        // 设置LinearLayout
                        holder.adapterLayout.setOnClickListener { _ ->
                            when (it.ncInfo.importance) {
                                -1 -> return@setOnClickListener
                                0 -> {
                                    nc.enableSpecificNotification(it)
                                    holder.adpAppStatus.apply {
                                        text = openHint
                                        setTextColor(getColor(R.color.green))
                                    }
                                    it.ncInfo.importance = 3
                                }
                                else -> {
                                    nc.disableSpecificNotification(it)
                                    holder.adpAppStatus.apply {
                                        text = closeHint
                                        setTextColor(getColor(R.color.colorTextRed))
                                    }
                                    it.ncInfo.importance = 0
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
                YukiHookAPI.Status.isXposedModuleActive && androidRestartNeeded -> R.drawable.bg_yellow_round
                YukiHookAPI.Status.isXposedModuleActive -> R.drawable.bg_green_round
                else -> R.drawable.bg_dark_round
            }
        )
        binding.mainImgStatus.setImageResource(
            when {
                YukiHookAPI.Status.isXposedModuleActive && !androidRestartNeeded -> R.drawable.ic_success
                else -> R.drawable.ic_warn
            }
        )
        binding.mainTextStatus.text = getString(
            when {
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
                YukiHookAPI.Status.isXposedModuleActive &&  androidRestartNeeded -> R.color.yellow
                YukiHookAPI.Status.isXposedModuleActive -> R.color.green
                else -> R.color.gray
            }
        )
    }

    override fun onResume() {
        super.onResume()
        refreshState()
        dataChannel("android").wait("${BuildConfig.APPLICATION_ID}_rec") {
            androidRestartNeeded = false
            refreshState()
        }
        dataChannel("android").put("${BuildConfig.APPLICATION_ID}_send")
        channelName = PreferenceManager.getDefaultSharedPreferences(this).getString("search_name", defaultChannelName).toString()
    }
}