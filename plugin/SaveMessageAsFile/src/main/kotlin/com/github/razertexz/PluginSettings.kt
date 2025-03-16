package com.github.razertexz;

import android.content.Context;
import android.view.View;
import android.text.TextWatcher;
import android.text.Editable;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.TextInput;

import com.discord.views.CheckedSetting;

class PluginSettings(val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        val context: Context = view.context

        setActionBarTitle("[SaveMessageAsFile] Plugin Settings")

        addView(TextInput(context, "Default File Name", settings.getString("defaultFileName", "untitled"), object : TextWatcher {
            override fun afterTextChanged(s: Editable) = s.toString().let {
                settings.setString("defaultFileName", it)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }))

        addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Skip File Name Dialog", null).apply {
            isChecked = settings.getBool("skipFileNameDialog", false)
            setOnCheckedListener {
                settings.setBool("skipFileNameDialog", it)
            }
        })
    }
}
