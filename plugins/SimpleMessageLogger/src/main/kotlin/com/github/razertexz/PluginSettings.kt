package com.github.razertexz

import android.content.Context
import android.view.View

import com.aliucord.fragments.SettingsPage
import com.aliucord.api.SettingsAPI
import com.aliucord.Utils

import com.discord.views.CheckedSetting

internal class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Simple Message Logger")
        setActionBarSubtitle("Settings")

        with(getContext()!!) {
            addCheckedSetting("Log Deleted Messages", null, "logDeletedMessages", true)
            addCheckedSetting("Log Edited Messages", null, "logEditedMessages", true)
            addCheckedSetting("Ignore Bots", "Ignore messages sent by bots", "ignoreBots", false)
            addCheckedSetting("Ignore Self", "Ignore messages sent by you", "ignoreSelf", false)
        }
    }

    private fun Context.addCheckedSetting(hint: CharSequence, description: String?, key: String, defaultValue: Boolean) {
        addView(Utils.createCheckedSetting(this, CheckedSetting.ViewType.SWITCH, hint, description).apply {
            isChecked = settings.getBool(key, defaultValue)
            setOnCheckedListener { settings.setBool(key, it) }
        })
    }
}