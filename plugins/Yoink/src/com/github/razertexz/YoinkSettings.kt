package com.github.razertexz

import android.view.View

import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.Utils

import com.discord.views.CheckedSetting

class YoinkSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Yoink")
        setActionBarSubtitle("Settings")

        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Include Timestamps", null).apply {
            isChecked = settings.getBool("includeTimestamps", true)
            setOnCheckedListener { settings.setBool("includeTimestamps", it) }
        })

        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Include Message IDs", null).apply {
            isChecked = settings.getBool("includeMessageIds", false)
            setOnCheckedListener { settings.setBool("includeMessageIds", it) }
        })

        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Include Reply IDs", null).apply {
            isChecked = settings.getBool("includeReplyIds", false)
            setOnCheckedListener { settings.setBool("includeReplyIds", it) }
        })

        addView(Utils.createCheckedSetting(view.context, CheckedSetting.ViewType.SWITCH, "Include Attachment URLs", null).apply {
            isChecked = settings.getBool("includeAttachmentUrls", true)
            setOnCheckedListener { settings.setBool("includeAttachmentUrls", it) }
        })
    }
}
