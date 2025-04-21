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
import com.facebook.drawee.span.SimpleDraweeSpanTextView as FacebookSimpleDraweeSpanTextView
import com.discord.views.UsernameView

import com.lytefast.flexinput.widget.FlexEditText

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    private val messagesFontScale = settings.getFloat("messagesFontScale", 0.0f)
    private val chatBoxFontScale = settings.getFloat("chatBoxFontScale", 0.0f)
    private val userNameFontScale = settings.getFloat("userNameFontScale", 0.0f)
    private val aboutMeFontScale = settings.getFloat("aboutMeFontScale", 0.0f)
    private val gameStatusFontScale = settings.getFloat("gameStatusFontScale", 0.0f)
    private val profileStatusFontScale = settings.getFloat("profileStatusFontScale", 0.0f)

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
            logger.infoToast("$identifier: ${ view.getResources().getResourceName(view.getId()) }")
        }
    }*/

    private fun patchMessages() {
        if (messagesFontScale == 0.0f) return

        patcher.after<WidgetChatListAdapterItemMessage>(
            "processMessageText",
            SimpleDraweeSpanTextView::class.java,
            MessageEntry::class.java
        ) {
            val simpleDraweeSpanTextView = it.args[0] as SimpleDraweeSpanTextView
            simpleDraweeSpanTextView.setTextSize(simpleDraweeSpanTextView.getTextSizeUnit(), messagesFontScale)
        }
    }

    private fun patchChatbox() {
        if (chatBoxFontScale == 0.0f) return

        patcher.patch(
            WidgetChatInputEditText::class.java.getDeclaredConstructors()[0],
            Hook {
                val flexEditText = it.args[0] as FlexEditText
                flexEditText.setTextSize(flexEditText.getTextSizeUnit(), chatBoxFontScale)
            }
        )
    }

    private fun patchAboutMe() {
        if (aboutMeFontScale == 0.0f) return

        patcher.patch(
            WidgetUserSheetBinding::class.java.getDeclaredConstructors()[0],
            Hook {
                val linkifiedTextView = it.args[6] as LinkifiedTextView
                linkifiedTextView.setTextSize(linkifiedTextView.getTextSizeUnit(), aboutMeFontScale)
            }
        )
    }

    private fun patchUsernameAndGameStatus() {
        if (gameStatusFontScale == 0.0f && userNameFontScale == 0.0f) return

        patcher.patch(
            WidgetChannelMembersListItemUserBinding::class.java.getDeclaredConstructors()[0],
            Hook {
                if (gameStatusFontScale != 0.0f) {
                    val simpleDraweeSpanTextView = it.args[3] as SimpleDraweeSpanTextView
                    simpleDraweeSpanTextView.setTextSize(simpleDraweeSpanTextView.getTextSizeUnit(), gameStatusFontScale)
                }

                if (userNameFontScale != 0.0f) {
                    val userNameView = it.args[5] as UsernameView
                    val facebookSimpleDraweeSpanTextView = userNameView.j.c as FacebookSimpleDraweeSpanTextView
                    facebookSimpleDraweeSpanTextView.setTextSize(facebookSimpleDraweeSpanTextView.getTextSizeUnit(), userNameFontScale)
                }
            }
        )
    }

    private fun patchProfileStatus() {
        if (profileStatusFontScale == 0.0f) return

        patcher.patch(
            UserProfileHeaderViewBinding::class.java.getDeclaredConstructors()[0],
            Hook {
                val simpleDraweeSpanTextView = it.args[9] as SimpleDraweeSpanTextView
                simpleDraweeSpanTextView.setTextSize(simpleDraweeSpanTextView.getTextSizeUnit(), profileStatusFontScale)
            }
        )
    }
}