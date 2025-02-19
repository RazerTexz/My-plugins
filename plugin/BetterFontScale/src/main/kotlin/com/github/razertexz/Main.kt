package com.github.razertexz;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.api.SettingsAPI;
import com.aliucord.Logger;

import com.discord.widgets.chat.input.WidgetChatInputEditText;
import com.discord.widgets.chat.input.ChatInputViewModel;
import com.discord.widgets.chat.input.WidgetChatInput;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter;
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel;
import com.discord.widgets.user.profile.UserProfileHeaderViewModel;
import com.discord.widgets.user.profile.UserProfileHeaderView;
import com.discord.databinding.WidgetUserSheetBinding;
import com.discord.databinding.UserProfileHeaderViewBinding;

import com.lytefast.flexinput.widget.FlexEditText;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    private val logger = Logger("Logger")
    private val settings = SettingsAPI("BetterFontScale")
    private val fontScale = settings.getFloat("fontScale", 0.0f)
    private val chatBoxFontScale = settings.getFloat("chatBoxFontScale", 0.0f)
    private val aboutMeFontScale = settings.getFloat("aboutMeFontScale", 0.0f)
    private val userNameFontScale = settings.getFloat("userNameFontScale", 0.0f)
    private val tagFontScale = settings.getFloat("tagFontScale", 0.0f)
    private val gameFontScale = settings.getFloat("gameFontScale", 0.0f)
    private val profileStatusFontScale = settings.getFloat("profileStatusFontScale", 0.0f)

    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        patches()
    }
    
    private fun patches() {
        if (fontScale != 0.0f) {
            WidgetChatListAdapter::class.java.superclass?.superclass?.getDeclaredMethod("onBindViewHolder", RecyclerView.ViewHolder::class.java, Int::class.java)?.let { method ->
                patcher.patch(method, Hook {
                    val viewHolder = it.args[0] as RecyclerView.ViewHolder

                    val itemView = viewHolder.itemView
                    if (itemView is ViewGroup) {
                        val textView = itemView.findViewById<TextView?>(0x7f0a0357)
                        if (textView != null && textView.getTextSize() != fontScale) {
                            textView.setTextSize(textView.getTextSizeUnit(), fontScale)
                        }
                    }
                })
            }
        }

        if (chatBoxFontScale != 0.0f) {
            val editText = WidgetChatInputEditText::class.java.getDeclaredField("editText").apply {
                isAccessible = true
            }

            patcher.after<WidgetChatInput>("configureUI", ChatInputViewModel.ViewState::class.java) {
                val thisObject = it.thisObject as WidgetChatInput
                val widgetChatInputEditText = WidgetChatInput.`access$getChatInputEditTextHolder$p`(thisObject) as WidgetChatInputEditText
                try { 
                    val flexEditText = editText.get(widgetChatInputEditText) as FlexEditText
                    if (flexEditText.getTextSize() != chatBoxFontScale) {
                        flexEditText.setTextSize(flexEditText.getTextSizeUnit(), chatBoxFontScale)
                    }
                } catch (e: Throwable) {
                    logger.error("Error", e)
                }
            }
        }

        patcher.after<ChannelMembersListAdapter>("onBindViewHolder", RecyclerView.ViewHolder::class.java, Int::class.java) {
            val viewHolder = it.args[0] as RecyclerView.ViewHolder

            val itemView = viewHolder.itemView
            if (itemView is ViewGroup) {
                val userName = itemView.findViewById<TextView?>(0x7f0a10cc)
                if (userNameFontScale != 0.0f && userName != null && userName.getTextSize() != userNameFontScale) {
                    userName.setTextSize(userName.getTextSizeUnit(), userNameFontScale)
                }

                val tag = itemView.findViewById<TextView?>(0x7f0a10cb)
                if (tagFontScale != 0.0f && tag != null && tag.getTextSize() != tagFontScale) {
                    tag.setTextSize(tag.getTextSizeUnit(), tagFontScale)
                }

                val game = itemView.findViewById<TextView?>(0x7f0a0243)
                if (gameFontScale != 0.0f && game != null && game.getTextSize() != gameFontScale) {
                    game.setTextSize(game.getTextSizeUnit(), gameFontScale)
                }
            }
        }

        if (aboutMeFontScale != 0.0f) {
            patcher.after<WidgetUserSheet>("configureUI", WidgetUserSheetViewModel.ViewState::class.java) {
                val thisObject = it.thisObject as WidgetUserSheet
                val binding = WidgetUserSheet.`access$getBinding$p`(thisObject) as WidgetUserSheetBinding
                val aboutMeText = binding.g
                if (aboutMeText.getTextSize() != aboutMeFontScale) {
                    aboutMeText.setTextSize(aboutMeText.getTextSizeUnit(), aboutMeFontScale)
                } 
            }
        }

        if (profileStatusFontScale != 0.0f) {
            patcher.after<UserProfileHeaderView>("updateViewState", UserProfileHeaderViewModel.ViewState.Loaded::class.java) {
                val thisObject = it.thisObject as UserProfileHeaderView
                val binding = UserProfileHeaderView.`access$getBinding$p`(thisObject) as UserProfileHeaderViewBinding
                val statusText = binding.i
                if (statusText.getTextSize() != profileStatusFontScale) {
                    statusText.setTextSize(statusText.getTextSizeUnit(), profileStatusFontScale)
                }
            }
        }
    }
    
    /*private fun getId(view: View, identifier: String) {
        if (view != null && view.getId() != View.NO_ID) {
            logger.info(identifier + ": " + view.getResources().getResourceName(view.getId()))
        }
    }*/

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
