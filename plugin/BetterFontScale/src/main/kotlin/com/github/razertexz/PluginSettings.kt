package com.github.razertexz

import androidx.core.content.res.ResourcesCompat
import android.content.Context
import android.view.View
import android.text.TextWatcher
import android.text.InputType
import android.text.Editable
import android.widget.LinearLayout
import android.widget.TextView

import com.aliucord.utils.DimenUtils.defaultPadding
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.TextInput
import com.aliucord.api.SettingsAPI
import com.aliucord.Constants.Fonts
import com.aliucord.Utils

import com.lytefast.flexinput.R

class PluginSettings(val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        val ctx = requireContext()

        setActionBarTitle("Better Font Scale Settings")
        setPadding(0)

        addView(TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            text = "NOTE: 0 = Use Default Value"
            setTypeface(ResourcesCompat.getFont(ctx, Fonts.whitney_semibold))
        })

        addTextInput(ctx, "Messages Font Scale", "messagesFontScale")
        addTextInput(ctx, "Chatbox Font Scale", "chatBoxFontScale")
        addTextInput(ctx, "Username Font Scale", "userNameFontScale")
        addTextInput(ctx, "About Me Font Scale", "aboutMeFontScale")
        addTextInput(ctx, "Game Status Font Scale", "gameStatusFontScale")
        addTextInput(ctx, "Profile Status Font Scale", "profileStatusFontScale")
    }

    private fun addTextInput(ctx: Context, hint: CharSequence, settingName: String) {
        addView(TextInput(
            ctx,
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