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

import java.util.*;

import com.discord.widgets.guilds.list.FolderItemDecoration;
import android.graphics.Canvas;
import androidx.recyclerview.widget.RecyclerView;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final SettingsAPI settings = new SettingsAPI("FavGuilds");
    List<Guild> list = new ArrayList<Guild>();

    @Override
    public void start(Context context) throws Throwable {
        patcher.patch(FolderItemDecoration.class.getDeclaredMethod("onDraw", Canvas.class, RecyclerView.class, RecyclerView.State.class),
            new Hook((param) -> {
                var recyclerView = (RecyclerView) param.args[1];
                var childAt = (View) recyclerView.getChildAt(0);
                var folderViewHolder = (GuildListViewHolder.FolderViewHolder) recyclerView.getChildViewHolder(childAt);
                folderViewHolder.configure(new GuildListItem.FolderItem(29183838, 0, "Favorites", false, list, false, false, false, 0, false, false));
            })
        );
        /*patcher.patch(GuildListViewHolder.FolderViewHolder.class.getDeclaredMethod("configure", GuildListItem.FolderItem.class),
            new PreHook((param) -> {
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
                list.add(guild);
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
