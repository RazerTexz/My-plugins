package com.github.razertexz;

import android.content.Context;
import android.view.View;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.TextInput;

class PluginSettings(val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        val context: Context = requireContext()

        setActionBarTitle("Better Font Scale Settings")
        setPadding(0)

        createTextInput(context, "Font Scale (0 to disable)", "fontScale")
        createTextInput(context, "Chatbox Font Scale (0 to disable)", "chatBoxFontScale")
        createTextInput(context, "About Me Font Scale (0 to disable)", "aboutMeFontScale")
        createTextInput(context, "Username Font Scale (0 to disable)", "userNameFontScale")
        createTextInput(context, "Tag Font Scale (0 to disable)", "tagFontScale")
        createTextInput(context, "Game / Status Font Scale (0 to disable)", "gameFontScale")
        createTextInput(context, "Profile Status Font Scale (0 to disable)", "profileStatusFontScale")
    }

    private fun createTextInput(context: Context, name: CharSequence, settingName: String) {
        addView(TextInput(context, name, settings.getFloat(settingName, 0.0f).toString(), object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val newValue: Float? = s.toString().toFloatOrNull()
                if (newValue != null) {
                    settings.setFloat(settingName, newValue)
                    Utils.promptRestart()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 8, 0, 8) }
        })
    }
}
