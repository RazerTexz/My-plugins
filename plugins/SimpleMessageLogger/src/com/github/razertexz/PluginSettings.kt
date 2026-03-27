package com.github.razertexz

import android.content.Context
import android.view.View

import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.Utils

import com.discord.views.CheckedSetting

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        val ctx = view.context

        setActionBarTitle("Simple Message Logger")
        setActionBarSubtitle("Settings")

        addCheckedSetting(ctx, "Log Deleted Messages", null, "logDeletedMessages", true)
        addCheckedSetting(ctx, "Log Edited Messages", null, "logEditedMessages", true)
        addCheckedSetting(ctx, "Ignore Bots", "Ignore messages sent by bots", "ignoreBots", false)
        addCheckedSetting(ctx, "Ignore Self", "Ignore messages sent by you", "ignoreSelf", false)
    }

    private fun addCheckedSetting(context: Context, hint: CharSequence, description: String?, key: String, defaultValue: Boolean) {
        addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, hint, description).apply {
            isChecked = settings.getBool(key, defaultValue)
            setOnCheckedListener {
                settings.setBool(key, it)
            }
        })
    }
}