package com.github.razertexz;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.api.SettingsAPI;

import com.discord.widgets.guilds.list.GuildListViewHolder;
import com.discord.widgets.guilds.list.GuildListItem;
import com.discord.widgets.guilds.contextmenu.GuildContextMenuViewModel;
import com.discord.widgets.guilds.contextmenu.WidgetGuildContextMenu;
import com.discord.databinding.WidgetGuildContextMenuBinding;
import com.discord.models.guild.Guild;
import com.discord.widgets.guilds.list.WidgetGuildListAdapter;

import com.discord.widgets.guilds.list.WidgetGuildsListViewModel;
import com.discord.stores.StoreGuildsSorted;

import java.util.*;

import de.robv.android.xposed.XposedBridge;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final SettingsAPI settings = new SettingsAPI("FavGuilds");
    private List<StoreGuildsSorted.Entry> list = new ArrayList<StoreGuildsSorted.Entry>();

    @Override
    public void start(Context context) throws Throwable {
        /*patcher.patch(GuildListViewHolder.FolderViewHolder.class.getDeclaredMethod("configure", GuildListItem.FolderItem.class),
            new PreHook((param) -> {
                var thisClass = (GuildListViewHolder.FolderViewHolder) param.thisObject;
                thisClass.configure(folder);
            })
        );*/

        patcher.patch(WidgetGuildsListViewModel.StoreState.class.getDeclaredMethod("getSortedGuilds"),
            new PreHook((param) -> {
                /*var field = WidgetGuildsListViewModel.StoreState.class.getDeclaredField("fieldName");
                field.setAccessible(true);*/
                var value = (List<StoreGuildsSorted.Entry>) XposedBridge.invokeOriginalMethod(param.method, this, null); // field.get(param.thisObject);
                var guildName = ((StoreGuildsSorted.Entry.SingletonGuild) value.get(0)).getGuild().getName();
                Utils.showToast(guildName);

                /*for (StoreGuildsSorted.Entry entry : value) {
                    if (entry instanceof StoreGuildsSorted.Entry.SingletonGuild) {
                        var guild = ((StoreGuildsSorted.Entry.SingletonGuild) entry).getGuild().getName();
                        list.add(guild);
                    }
                }*/

                param.setResult(list);
            })
        );

        /*patcher.patch(WidgetGuildsListViewModel.class.getDeclaredMethod("access$handleStoreState", WidgetGuildsListViewModel.StoreState.class),
            new Hook((param) -> {
                var state = (WidgetGuildsListViewModel.StoreState) param.args[0];
                for (StoreGuildsSorted.Entry entry : state.getSortedGuilds()) {
                    if (entry instanceof StoreGuildsSorted.Entry.SingletonGuild) {
                        var guild = ((StoreGuildsSorted.Entry.SingletonGuild) entry).getGuild().getName();
                        Utils.showToast(guild);
                        break;
                    }
                }
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
                var singletonGuild = new StoreGuildsSorted.Entry.SingletonGuild(guild);
                list.add(singletonGuild);
                var guildIDAsString = "" + guild.getId();
                var isFavorited = settings.getBool(guildIDAsString, false);
                var viewID = View.generateViewId();
                if (lay.findViewById(viewID) == null) {
                    TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.ContextMenuTextOption);
                    tw.setId(viewID);
                    tw.setText(isFavorited ? "Unfavorite" : "Favorite");
                    lay.addView(tw, lay.getChildCount());
                    tw.setOnClickListener((v) -> {
                        settings.setBool(guildIDAsString, isFavorited ? false : true);
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
