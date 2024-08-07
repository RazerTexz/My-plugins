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
import com.aliucord.Logger;

import com.discord.widgets.chat.list.WidgetChatList;
import com.discord.widgets.chat.list.model.WidgetChatListModel;
import com.discord.databinding.WidgetChatListBinding;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final Logger logger = new Logger("Logger");
    private final SettingsAPI settings = new SettingsAPI("ChatSize");

    public Main() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @Override
    public void start(Context context) throws Throwable {
        patches();
    }

    private void patches() throws Throwable {
        var getBinding = WidgetChatList.class.getDeclaredMethod("getBinding");
        getBinding.setAccessible(true);
        patcher.patch(WidgetChatList.class.getDeclaredMethod("configureUI", WidgetChatListModel.class),
            new Hook((param) -> {
                WidgetChatListBinding binding = null;
                try {
                    binding = (WidgetChatListBinding) getBinding.invoke(param.thisObject);
                } catch (Throwable e) {
                    logger.error("Failed to get binding", e);
                }

                var recyclerView = (RecyclerView) binding.a;
                var layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                for (int i = 0; i < layoutManager.getChildCount(); i++) {
                    var view = (View) layoutManager.getChildAt(i);
                    if (view instanceof ViewGroup) {
                        var viewGroup = (ViewGroup) view;
                        var textView = (TextView) viewGroup.findViewById(0x7f0a0357);
                        if (textView != null) {
                            logger.info("Found TextView with id 0x7f0a0357");
                            logger.info("Size Unit: " + textView.getTextSizeUnit());
                            logger.info("Size: " + textView.getTextSize());
                            textView.setTextSize(textView.getTextSizeUnit(), 74f);
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