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

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter;

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
        patcher.patch(WidgetChatListAdapter.class.getSuperclass().getSuperclass().getDeclaredMethod("onBindViewHolder", RecyclerView.ViewHolder.class, int.class),
            new Hook((param) -> {
                var viewHolder = (RecyclerView.ViewHolder) param.args[0];

                var itemView = viewHolder.itemView;
                if (itemView instanceof ViewGroup) {
                    var rootView = (ViewGroup) itemView;
                    //String rootViewName = (rootView.getId() == View.NO_ID) ? "" : rootView.getResources().getResourceName(rootView.getId());
                    //logger.info(rootViewName);
                    var textView = (TextView) rootView.findViewById(0x7f0a0357);
                    if (textView != null && textView.getTextSize() != 20f) {
                        logger.info("Size Unit: " + textView.getTextSizeUnit());
                        logger.info("Size: " + textView.getTextSize());
                        //textView.setTextSize(textView.getTextSizeUnit(), 20f);
                    }
                    /*for (int i = 0; i < rootView.getChildCount(); i++) {
                        var view = (View) rootView.getChildAt(i);
                        String viewName = (view.getId() == View.NO_ID) ? "" : view.getResources().getResourceName(view.getId());
                        logger.info(viewName);*/
                        /*if (view instanceof ViewGroup) {
                            var viewGroup = (ViewGroup) view;
                            var textView = (TextView) viewGroup.findViewById(0x7f0a0357);
                            if (textView != null && textView.getTextSize() != 20f) {
                                logger.info("Size Unit: " + textView.getTextSizeUnit());
                                logger.info("Size: " + textView.getTextSize());
                                //textView.setTextSize(textView.getTextSizeUnit(), 20f);
                            }
                        }
                    }*/
                }
            })
        );

        /*var getBinding = WidgetChatList.class.getDeclaredMethod("getBinding");
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
                        if (textView != null && textView.getTextSize() != 20f) {
                            //logger.info("Size Unit: " + textView.getTextSizeUnit());
                            //logger.info("Size: " + textView.getTextSize());
                            textView.setTextSize(textView.getTextSizeUnit(), 20f);
                        }
                    }
                }
            })
        );*/
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}