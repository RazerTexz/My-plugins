package com.github.razertexz

import android.widget.TextView
import android.content.Context

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.chat.input.WidgetChatInputEditText
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.databinding.UserProfileHeaderViewBinding
import com.discord.databinding.WidgetUserSheetBinding
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.utilities.view.text.LinkifiedTextView
import com.discord.views.UsernameView

import com.lytefast.flexinput.widget.FlexEditText

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        patchMessages()
        patchChatbox()
        patchAboutMe()
        patchUsernameAndGameStatus()
        patchProfileStatus()
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    /*private fun android.view.View.infoToastId(identifier: String) {
        if (this.getId() != 0xFFFFFFFF) {
            logger.infoToast("$identifier: ${ this.getResources().getResourceName(this.getId()) }")
        }
    }*/

    private fun patchMessages() {
        val messagesFontScale = settings.getFloat("messagesFontScale", 0.0f)
        if (messagesFontScale == 0.0f) return

        patcher.after<WidgetChatListAdapterItemMessage>(
            "processMessageText",
            SimpleDraweeSpanTextView::class.java,
            MessageEntry::class.java
        ) {
            (it.args[0] as SimpleDraweeSpanTextView).apply { setTextSize(getTextSizeUnit(), messagesFontScale) }
        }
    }

    private fun patchChatbox() {
        val chatBoxFontScale = settings.getFloat("chatBoxFontScale", 0.0f)
        if (chatBoxFontScale == 0.0f) return

        patcher.patch(
            WidgetChatInputEditText::class.java.getDeclaredConstructors()[0],
            Hook {
                (it.args[0] as FlexEditText).apply { setTextSize(getTextSizeUnit(), chatBoxFontScale) }
            }
        )
    }

    private fun patchAboutMe() {
        val aboutMeFontScale = settings.getFloat("aboutMeFontScale", 0.0f)
        if (aboutMeFontScale == 0.0f) return

        patcher.patch(
            WidgetUserSheetBinding::class.java.getDeclaredConstructors()[0],
            Hook {
                (it.args[6] as LinkifiedTextView).apply { setTextSize(getTextSizeUnit(), aboutMeFontScale) }
            }
        )
    }

    private fun patchUsernameAndGameStatus() {
        val gameStatusFontScale = settings.getFloat("gameStatusFontScale", 0.0f)
        val userNameFontScale = settings.getFloat("userNameFontScale", 0.0f)
        if (gameStatusFontScale == 0.0f && userNameFontScale == 0.0f) return

        patcher.patch(
            WidgetChannelMembersListItemUserBinding::class.java.getDeclaredConstructors()[0],
            Hook {
                if (gameStatusFontScale != 0.0f) {
                    (it.args[3] as SimpleDraweeSpanTextView).apply { setTextSize(getTextSizeUnit(), gameStatusFontScale) }
                }

                if (userNameFontScale != 0.0f) {
                    val userNameView = it.args[5] as UsernameView
                    userNameView.j.c.apply { setTextSize(getTextSizeUnit(), userNameFontScale) }
                }
            }
        )
    }

    private fun patchProfileStatus() {
        val profileStatusFontScale = settings.getFloat("profileStatusFontScale", 0.0f)
        if (profileStatusFontScale == 0.0f) return

        patcher.patch(
            UserProfileHeaderViewBinding::class.java.getDeclaredConstructors()[0],
            Hook {
                (it.args[9] as SimpleDraweeSpanTextView).apply { setTextSize(getTextSizeUnit(), profileStatusFontScale) }
            }
        )
    }
}
