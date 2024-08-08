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
//import com.aliucord.Logger;

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    //private final Logger logger = new Logger("Logger");
    private final SettingsAPI settings = new SettingsAPI("ChatSize");
    private final float fontScale = settings.getFloat("fontScale", 0.0f);

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
                            if (settings.exists("fontScale") {
                                settings.setFloat("fontScale", textView.getTextSize());
                            }
                            textView.setTextSize(textView.getTextSizeUnit(), fontScale);
                        }
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