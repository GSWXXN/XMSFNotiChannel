package com.gswxxn.xmsfnotichannel.hook

import android.content.pm.PackageManager
import android.os.Binder
import com.gswxxn.xmsfnotichannel.BuildConfig
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.log.YukiHookLogger
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
    override fun onInit() {
        YukiHookAPI.Configs.isDebug = false
        YukiHookLogger.Configs.tag = "XMSF_GG"
    }

    override fun onHook() = YukiHookAPI.encase {
        loadSystem {
            dataChannel.wait("${BuildConfig.APPLICATION_ID}_send") {
                dataChannel.put("${BuildConfig.APPLICATION_ID}_rec")
            }

            "com.android.server.notification.NotificationManagerService".hook {
                injectMember {
                    method { name = "isCallerSystemOrPhone" }
                    beforeHook {
                        if (appContext!!.packageManager.getPackageUid(BuildConfig.APPLICATION_ID, PackageManager.MATCH_ALL) == Binder.getCallingUid()) {
                            resultTrue()
                        }
                    }
                }
            }
        }
    }
}