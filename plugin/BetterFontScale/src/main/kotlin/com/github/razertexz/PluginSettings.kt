package com.github.razertexz

import android.content.Context
import android.text.TextWatcher
import android.text.InputType
import android.text.Editable
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.core.content.res.ResourcesCompat

import com.aliucord.Utils
import com.aliucord.utils.DimenUtils.defaultPadding
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.TextInput
import com.aliucord.api.SettingsAPI
import com.aliucord.Constants.Fonts

import com.lytefast.flexinput.R

class PluginSettings(val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Better Font Scale Settings")
        setPadding(0)

        val context = requireContext()
        addView(TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            text = "NOTE: 0 = Use Default Value"
            setTypeface(ResourcesCompat.getFont(context, Fonts.whitney_semibold))
        })

        addTextInput(context, "Messages Font Scale", "messagesFontScale")
        addTextInput(context, "Chatbox Font Scale", "chatBoxFontScale")
        addTextInput(context, "Username Font Scale", "userNameFontScale")
        addTextInput(context, "About Me Font Scale", "aboutMeFontScale")
        addTextInput(context, "Game Status Font Scale", "gameStatusFontScale")
        addTextInput(context, "Profile Status Font Scale", "profileStatusFontScale")
    }

    private fun addTextInput(context: Context, hint: CharSequence, settingName: String) {
        addView(TextInput(
            context,
            hint,
            settings.getFloat(settingName, 0.0f).toString(),
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    s.toString().toFloatOrNull()?.let {
                        settings.setFloat(settingName, it)
                        Utils.promptRestart()
                    }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            }
        ).apply {
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(defaultPadding, 8, defaultPadding, 8)
            }
        })
    }
}