package com.gswxxn.xmsfnotichannel.application

import android.content.Context
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import me.weishu.reflection.Reflection

class GGApplication : ModuleApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }
}