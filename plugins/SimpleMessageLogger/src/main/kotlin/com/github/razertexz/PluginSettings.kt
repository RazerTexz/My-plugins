package com.github.razertexz

import android.view.View

import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage

import com.discord.views.CheckedSetting

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Simple Message Logger")
        setActionBarSubtitle("Settings")

        val ctx = getContext()!!
        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "Log Deleted Messages", null).apply {
            isChecked = settings.getBool("logDeletedMessages", true)
            setOnCheckedListener { settings.setBool("logDeletedMessages", it) }
        })

        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "Log Edited Messages", null).apply {
            isChecked = settings.getBool("logEditedMessages", true)
            setOnCheckedListener { settings.setBool("logEditedMessages", it) }
        })

        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "Ignore Bots", "Ignore messages sent by bots").apply {
            isChecked = settings.getBool("ignoreBots", false)
            setOnCheckedListener { settings.setBool("ignoreBots", it) }
        })

        addView(Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, "Ignore Self", "Ignore messages sent by you").apply {
            isChecked = settings.getBool("ignoreSelf", false)
            setOnCheckedListener { settings.setBool("ignoreSelf", it) }
        })
    }
}