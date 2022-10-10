package com.gswxxn.xmsfnotichannel.hook

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
            "com.android.server.notification.NotificationManagerService".hook {
                injectMember {
                    method { name = "isCallerSystemOrPhone" }
                    replaceToTrue()
                }
            }
        }
    }
}