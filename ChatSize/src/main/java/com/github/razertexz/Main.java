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

import com.lytefast.flexinput.widget.FlexEditText;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final Logger logger = new Logger("Logger");
    private final SettingsAPI settings = new SettingsAPI("ChatSize");
    private final float fontScale = settings.getFloat("fontScale", 0.0f);
    private final float chatBoxFontScale = settings.getFloat("chatBoxFontScale", 0.0f);

    public Main() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @Override
    public void start(Context context) throws Throwable {
        patches();
    }

    private void patches() throws Throwable {
        patcher.patch(WidgetChatListAdapter.class.getSuperclass().getSuperclass().getDeclaredMethod("onBindViewHolder", RecyclerView.ViewHolder.class, int.class),
            new Hook((param) -> {
                if (fontScale != 0.0f) {
                    var viewHolder = (RecyclerView.ViewHolder) param.args[0];

                    var itemView = viewHolder.itemView;
                    if (itemView instanceof ViewGroup) {
                        var rootView = (ViewGroup) itemView;
                        var textView = (TextView) rootView.findViewById(0x7f0a0357);
                        if (textView != null && textView.getTextSize() != fontScale) {
                            textView.setTextSize(textView.getTextSizeUnit(), fontScale);
                        }
                    }
                }
            })
        );

        var editText = WidgetChatInputEditText.class.getDeclaredField("editText");
        editText.setAccessible(true);
        patcher.patch(WidgetChatInput.class.getDeclaredMethod("configureUI", ChatInputViewModel.ViewState.class),
            new Hook((param) -> {
                if (chatBoxFontScale != 0.0f) {
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
                }
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}   