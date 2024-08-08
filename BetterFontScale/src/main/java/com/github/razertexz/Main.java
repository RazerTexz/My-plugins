package com.github.razertexz;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.CollectionUtils;
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

import com.lytefast.flexinput.widget.FlexEditText;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final Logger logger = new Logger("Logger");
    private final SettingsAPI settings = new SettingsAPI("BetterFontScale");
    private final float fontScale = settings.getFloat("fontScale", 0.0f);
    private final float chatBoxFontScale = settings.getFloat("chatBoxFontScale", 0.0f);
    private final float userNameFontScale = settings.getFloat("userNameFontScale", 0.0f);
    private final float tagFontScale = settings.getFloat("tagFontScale", 0.0f);
    private final float gameFontScale = settings.getFloat("gameFontScale", 0.0f);

    public Main() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @Override
    public void start(Context context) throws Throwable {
        patches();
    }

    private void patches() throws Throwable {
        if (fontScale != 0.0f) {
            patcher.patch(WidgetChatListAdapter.class.getSuperclass().getSuperclass().getDeclaredMethod("onBindViewHolder", RecyclerView.ViewHolder.class, int.class),
                new Hook((param) -> {
                    var viewHolder = (RecyclerView.ViewHolder) param.args[0];
    
                    var itemView = viewHolder.itemView;
                    if (itemView instanceof ViewGroup) {
                        var rootView = (ViewGroup) itemView;
                        var textView = (TextView) rootView.findViewById(0x7f0a0357);
                        if (textView != null && textView.getTextSize() != fontScale) {
                            textView.setTextSize(textView.getTextSizeUnit(), fontScale);
                        }
                    }
                })
            );
        }

        if (chatBoxFontScale != 0.0f) {
            var editText = WidgetChatInputEditText.class.getDeclaredField("editText");
            editText.setAccessible(true);
            patcher.patch(WidgetChatInput.class.getDeclaredMethod("configureUI", ChatInputViewModel.ViewState.class),
                new Hook((param) -> {
                    var thisObject = (WidgetChatInput) param.thisObject;
                    var widgetChatInputEditText = (WidgetChatInputEditText) WidgetChatInput.access$getChatInputEditTextHolder$p(thisObject);
                    try { 
                        var flexEditText = (FlexEditText) editText.get(widgetChatInputEditText);
                        if (flexEditText.getTextSize() != chatBoxFontScale) {
                            flexEditText.setTextSize(flexEditText.getTextSizeUnit(), chatBoxFontScale);
                        }
                    } catch (Throwable e) {
                        logger.error("Error", e);
                    }
                })
            );
        }

        patcher.patch(ChannelMembersListAdapter.class.getDeclaredMethod("onBindViewHolder", RecyclerView.ViewHolder.class, int.class),
            new Hook((param) -> {
                var viewHolder = (RecyclerView.ViewHolder) param.args[0];

                var itemView = viewHolder.itemView;
                if (itemView instanceof ViewGroup) {
                    var rootView = (ViewGroup) itemView;

                    var userName = (TextView) rootView.findViewById(0x7f0a10cc);
                    if (userNameFontScale != 0.0f && userName != null && userName.getTextSize() != userNameFontScale) {
                        userName.setTextSize(userName.getTextSizeUnit(), userNameFontScale);
                    }

                    var tag = (TextView) rootView.findViewById(0x7f0a10cb);
                    if (tagFontScale != 0.0f && tag != null && tag.getTextSize() != tagFontScale) {
                        tag.setTextSize(tag.getTextSizeUnit(), tagFontScale);
                    }

                    var game = (TextView) rootView.findViewById(0x7f0a0243);
                    if (gameFontScale != 0.0f && game != null && game.getTextSize() != gameFontScale) {
                        game.setTextSize(game.getTextSizeUnit(), gameFontScale);
                    }
                }
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}   