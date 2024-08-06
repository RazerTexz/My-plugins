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

import com.discord.R;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final SettingsAPI settings = new SettingsAPI("ChatSize");

    public Main() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @Override
    public void start(Context context) throws Throwable {
        patches();
    }

    private void patches() throws Throwable {
        patcher.patch(WidgetChatListAdapter.class.getDeclaredMethod("onBindViewHolder", RecyclerView.ViewHolder.class, int.class),
            new Hook((param) -> {
                var viewHolder = (RecyclerView.ViewHolder) param.args[0];
                var itemView = (View) viewHolder.itemView;
                if (itemView instanceof ViewGroup) {
                    var viewGroup = (ViewGroup) itemView;
                    for (int i = 0; i < viewGroup.getChildCount(); i++) {
                        var child = (View) viewGroup.getChildAt(i);
                        if (child instanceof ViewGroup) {
                            var textView = (TextView) child.findViewById(2131559038);
                            Utils.showToast(("" + textView.getTextSizeUnit()) + " - " + ("" + textView.getTextSize()));
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