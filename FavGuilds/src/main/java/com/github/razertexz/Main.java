package com.github.razertexz;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliucord.Utils.*;
import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.wrappers.embeds.MessageEmbedWrapper;

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.discord.widgets.guilds.contextmenu.GuildContextMenuViewModel;
import com.discord.widgets.guilds.contextmenu.WidgetGuildContextMenu;

import java.util.*;

// Aliucord Plugin annotation. Must be present on the main class of your plugin
@AliucordPlugin(requiresRestart = false /* Whether your plugin requires a restart after being installed/updated */)
// Plugin class. Must extend Plugin and override start and stop
// Learn more: https://github.com/Aliucord/documentation/blob/main/plugin-dev/1_introduction.md#basic-plugin-structure
public class Main extends Plugin {
    @Override
    public void start(Context context) throws Throwable {
        // Patch that adds an embed with message statistics to each message
        // Patched method is WidgetChatListAdapterItemMessage.onConfigure(int type, ChatListEntry entry)
        //patcher.patch(/* see https://docs.oracle.com/javase/tutorial/reflect/class/classNew.html */WidgetChatListAdapterItemMessage.class.getDeclaredMethod("onConfigure", int.class, ChatListEntry.class),
            /*new Hook(param -> { // see https://api.xposed.info/reference/de/robv/android/xposed/XC_MethodHook.MethodHookParam.html
                // Obtain the second argument passed to the method, so the ChatListEntry
                // Because this is a Message item, it will always be a MessageEntry, so cast it to that
                var entry = (MessageEntry) param.args[1];

                // You need to be careful when messing with messages, because they may be loading
                // (user sent a message, and it is currently sending)
                if (entry.getMessage().isLoading()) return;

                // Now add an embed with the statistics

                // This method may be called multiple times per message, e.g. if it is edited,
                // so first remove existing embeds
                CollectionUtils.removeIf(entry.getMessage().getEmbeds(), e -> {
                    // MessageEmbed is an obfuscated class. However, Aliucord provides wrappers for commonly used
                    // obfuscated classes, the MessageEmbedWrapper in this case.
                    return "Message Statistics".equals(MessageEmbedWrapper.getTitle(e));
                });

                // Creating embeds is a pain, so Aliucord provides a convenient builder
                var embed = new MessageEmbedBuilder().
                        setTitle("Message Statistics")
                        .addField("Length", entry.getMessage().getContent() != null ? Integer.toString(entry.getMessage().getContent().length()) : "0", false)
                        .addField("ID", Long.toString(entry.getMessage().getId()), false).build();

                entry.getMessage().getEmbeds().add(embed);
            })
        );*/
        patchWidgetGuildContextMenu();
    }

    private void patchWidgetGuildContextMenu() throws Throwable {
        var getBinding = WidgetGuildContextMenu.class.getDeclaredMethod("getBinding");
        getBinding.setAccessible(true);
        patcher.patch(WidgetGuildContextMenu.class.getDeclaredMethod("configureUI", GuildContextMenuViewModel.ViewState.class),
            new Hook((cf) -> {
                var state = (GuildContextMenuViewModel.ViewState.Valid) cf.args[0];
                WidgetGuildContextMenuBinding binding = null;
                try {
                    binding = (WidgetGuildContextMenuBinding) getBinding.invoke(cf.thisObject);
                } catch (Throwable e) {
                }
                var lay = (LinearLayout) binding.e.getParent();
                var guild = state.getGuild();
                //var guildId = guild.getId();
                var isFavorited = false; // Temp
                var viewID = View.generateViewId();
                if (lay.findViewById(viewID) == null) {
                    TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.ContextMenuTextOption);
                    tw.setId(viewID);
                    tw.setText(isFavorited ? "Unfavorite" : "Favorite");
                    lay.addView(tw, lay.getChildCount());
                    tw.setOnClickListener((v) -> {
                        if (isFavorited) {
                            Utils.showToast(guild.getName());
                        } else {
                            Utils.showToast(guild.getName());
                        }
                        lay.setVisibility(View.GONE);
                    });
                }
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
