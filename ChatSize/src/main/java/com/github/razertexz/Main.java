package com.github.razertexz;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

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

import com.discord.widgets.chat.list.WidgetChatList;
import com.discord.widgets.chat.list.model.WidgetChatListModel;
import com.discord.databinding.WidgetChatListBinding;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final SettingsAPI settings = new SettingsAPI("ChatSize");
    var getBinding = WidgetChatList.class.getDeclaredMethod("getBinding");

    public Main() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @Override
    public void start(Context context) throws Throwable {
        getBinding.setAccessible(true);
        patches();
    }

    private void patches() throws Throwable {
        patcher.patch(WidgetChatList.class.getDeclaredMethod("configureUI", WidgetChatListModel.class),
            new Hook((param) -> {
                var binding = getBinding.invoke(param.thisObject);
                var view = (RecyclerView) binding.getRoot();
                var layoutManager = (LinearLayoutManager) view.getLayoutManager();
                for (int i = 0; i < layoutManager.getChildCount(); i++) {
                    var child = (View) layoutManager.getChildAt(i);
                    if (child instanceof ViewGroup) {
                        var textView = (TextView) child.findViewById(2131559038);
                        Utils.showToast(("" + textView.getTextSizeUnit()) + " - " + ("" + textView.getTextSize()));
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