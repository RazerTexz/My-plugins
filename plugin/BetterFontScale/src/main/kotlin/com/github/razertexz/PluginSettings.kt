package com.github.razertexz

import android.text.TextWatcher
import android.text.InputType
import android.text.Editable
import android.widget.LinearLayout
import android.content.Context
import android.view.View

import com.aliucord.Utils.promptRestart
import com.aliucord.utils.DimenUtils.defaultPadding
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.TextInput
import com.aliucord.api.SettingsAPI

class PluginSettings(val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        val context = requireContext()

        setActionBarTitle("Better Font Scale Settings")
        setPadding(0)

        createTextInput(context, "Messages Font Scale (0 to disable)", "messagesFontScale")
        createTextInput(context, "Chatbox Font Scale (0 to disable)", "chatBoxFontScale")
        createTextInput(context, "Username Font Scale (0 to disable)", "userNameFontScale")
        createTextInput(context, "About Me Font Scale (0 to disable)", "aboutMeFontScale")
        createTextInput(context, "Game Status Font Scale (0 to disable)", "gameStatusFontScale")
        createTextInput(context, "Profile Status Font Scale (0 to disable)", "profileStatusFontScale")
    }

    private fun createTextInput(context: Context, hint: CharSequence, settingName: String) {
        addView(TextInput(
            context,
            hint,
            settings.getFloat(settingName, 0.0f).toString(),
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    val input = s.toString()
                    if (input != "") {
                        settings.setFloat(settingName, input.toFloat())
                        promptRestart()
                    }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            }
        ).apply {
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                defaultPadding.let { setMargins(it, 8, it, 8) }
            }
        })
    }
}