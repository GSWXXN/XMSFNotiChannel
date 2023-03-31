package com.gswxxn.xmsfnotichannel.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.gswxxn.xmsfnotichannel.BuildConfig
import com.gswxxn.xmsfnotichannel.R
import com.gswxxn.xmsfnotichannel.activity.MainActivity.Companion.defaultChannelName
import com.gswxxn.xmsfnotichannel.databinding.ActivityAboutPageBinding
import com.gswxxn.xmsfnotichannel.utils.RoundDegree
import com.gswxxn.xmsfnotichannel.utils.dp2px
import com.gswxxn.xmsfnotichannel.utils.drawable2Bitmap
import com.gswxxn.xmsfnotichannel.utils.roundBitmapByShader


class AboutPageActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutPageBinding

    override fun onCreate() {
        binding = ActivityAboutPageBinding.inflate(layoutInflater).apply { setContentView(root) }

        binding.apply {

            titleBackIcon.setOnClickListener { onBackPressed() }

            appIcon.setImageBitmap(roundBitmapByShader(
                getDrawable(R.mipmap.ic_launcher)?.let {
                    drawable2Bitmap(
                        it,
                        it.intrinsicHeight * 2
                    )
                }, RoundDegree.RoundCorner))

            var count = 0
            var lastClickTime: Long = 0
            appIcon.setOnClickListener {
                val now = System.currentTimeMillis()

                if (now - lastClickTime < 500) count++
                else count = 1

                lastClickTime = now

                if (count != 5) return@setOnClickListener
                count = 0

                AlertDialog.Builder(this@AboutPageActivity).apply {
                    val pref =  PreferenceManager.getDefaultSharedPreferences(this@AboutPageActivity)
                    val input = EditText(this@AboutPageActivity).apply {
                        setText(pref.getString("search_name", defaultChannelName))
                    }
                    setTitle(getString(R.string.searching_name))
                    setMessage(getString(R.string.searching_name_summary))
                    setView(input)
                    setPositiveButton(getString(R.string.alert_ok)) { _, _ ->
                        if (input.text.toString().isEmpty()) {
                            Toast.makeText(this@AboutPageActivity, getString(R.string.can_not_be_empty), Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        pref.edit().putString("search_name", input.text.toString()).apply()
                    }
                    setNegativeButton(getString(R.string.alert_cancel)) { dialog, _ ->
                        dialog.cancel()
                    }
                    setNeutralButton(getString(R.string.alert_reset)) { _, _ ->
                        pref.edit().putString("search_name", defaultChannelName).apply()
                    }
                }.create().apply {
                    setCanceledOnTouchOutside(false)
                }.show()

            }

            miluIcon.setImageBitmap(roundBitmapByShader(
                getDrawable(R.mipmap.img_developer)?.let {
                    drawable2Bitmap(
                        it,
                        it.intrinsicHeight
                    )
                }, RoundDegree.Circle
            )
            )

            version.text = getString(R.string.version, BuildConfig.VERSION_NAME)

            developerMilu.setOnClickListener {
                Toast.makeText(this@AboutPageActivity, getString(R.string.follow_me), Toast.LENGTH_SHORT).show()
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("coolmarket://u/1189245")))
                } catch (e: Exception) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://www.coolapk.com/u/1189245")
                        )
                    )
                }
            }

            githubRepo.setOnClickListener {
                Toast.makeText(this@AboutPageActivity, getString(R.string.star_project), Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/GSWXXN/XMSFNotiChannel")
                    )
                )
            }

            licenseLayout.apply {
                addLicenseV(
                    "MIUINativeNotifyIcon",
                    "fankes",
                    "https://github.com/fankes/MIUINativeNotifyIcon",
                    "GNU Affero General Public License v3.0"
                )
                addLicenseV(
                    "Hide-My-Applist",
                    "Dr-TSNG",
                    "https://github.com/Dr-TSNG/Hide-My-Applist",
                    "GNU Affero General Public License v3.0"
                )
                addLicenseV(
                    "YukiHookAPI",
                    "fankes",
                    "https://github.com/fankes/YukiHookAPI",
                    "MIT License"
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun LinearLayout.addLicenseV(
        projectName: String,
        author: String,
        url: String,
        licenseName: String
    ) {
        addView(LinearLayout(this@AboutPageActivity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    0,
                    dp2px(this@AboutPageActivity, 10F),
                    0,
                    dp2px(this@AboutPageActivity, 15F),
                )
                orientation = LinearLayout.VERTICAL
            }
            addView(
                TextView(this@AboutPageActivity).apply {
                    text = "$projectName - $author"
                    textSize = 15F
                    setTextColor(getColor(R.color.colorTextGray))
                }
            )
            addView(
                TextView(this@AboutPageActivity).apply {
                    text = "$url\n$licenseName"
                    textSize = 12F
                    setTextColor(getColor(R.color.colorTextDark))
                    setLineSpacing(dp2px(this@AboutPageActivity, 3F).toFloat(), 1F)
                }
            )
            setOnClickListener {
                Toast.makeText(this@AboutPageActivity, getString(R.string.thanks_to, author), Toast.LENGTH_SHORT).show()
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
        )
    }
}