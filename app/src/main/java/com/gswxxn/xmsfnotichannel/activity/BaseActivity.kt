package com.gswxxn.xmsfnotichannel.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.gswxxn.xmsfnotichannel.R

abstract class BaseActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        window.apply {
            statusBarColor = getColor(R.color.colorThemeBackground)
            navigationBarColor = getColor(R.color.colorThemeBackground)
        }
        onCreate()
    }

    abstract fun onCreate()

    open fun showView(isShow: Boolean = true, vararg views: View?) {
        for (element in views) {
            element?.visibility = if (isShow) View.VISIBLE else View.GONE
        }
    }
}