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

import com.discord.widgets.guilds.contextmenu.GuildContextMenuViewModel;
import com.discord.widgets.guilds.contextmenu.WidgetGuildContextMenu;
import com.discord.databinding.WidgetGuildContextMenuBinding;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final SettingsAPI settings = this.settings;
    @Override
    public void start(Context context) throws Throwable {
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
                var guildIDAsString = guild.getId().toString();
                var isFavorited = settings.getBool(guildIDAsString, false);
                var viewID = View.generateViewId();
                if (lay.findViewById(viewID) == null) {
                    TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.ContextMenuTextOption);
                    tw.setId(viewID);
                    tw.setText(isFavorited ? "Unfavorite" : "Favorite");
                    lay.addView(tw, lay.getChildCount());
                    tw.setOnClickListener((v) -> {
                        settings.setBool(guildIDAsString, isFavorited ? false : true);
                        Utils.showToast(guildIDAsString + " " + isFavorited.toString());
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
