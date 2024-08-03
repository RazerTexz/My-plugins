package com.github.razertexz;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.api.SettingsAPI;
import com.aliucord.wrappers.ChannelWrapper;

import com.discord.stores.StoreCallsIncoming;
import com.discord.models.domain.ModelCall;
import com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final SettingsAPI settings = new SettingsAPI("AutoBlockCalls");

    public Main() {
        settingsTab = new SettingsTab(Main.class).withArgs(settings);
    }

    @Override
    public void start(Context context) throws Throwable {
        patches();
        patchWidgetChannelsListItemChannelActions();
    }

    private void patches() throws Throwable {
        patcher.patch(StoreCallsIncoming.class.getDeclaredMethod("handleCallCreateOrUpdate", ModelCall.class),
            new PreHook((param) -> {
                var state = (ModelCall) param.args[0];
                var channelId = "" + state.getChannelId();
                if (settings.getBool(channelId, false) {
                    param.setResult(null);
                }
                //var thisClass = (StoreCallsIncoming) param.thisObject;
                //thisClass.handleCallDelete(state);
                //thisClass.removeIncomingCall(state.getChannelId());
                //Utils.showToast("Voice Call from " + channelId);
            })
        );
    }

    private void patchWidgetChannelsListItemChannelActions() throws Throwable {
        var context = Utils.getAppContext();

        patcher.patch(WidgetChannelsListItemChannelActions.class.getDeclaredMethod("configureUI", WidgetChannelsListItemChannelActions.Model.class),
            new Hook((cf) -> {
                var modal = (WidgetChannelsListItemChannelActions.Model) cf.args[0];
                var channel = modal.getChannel();
                var isDM = ChannelWrapper(channel).isDM();

                if (isDM) {
                    var channelId = "" + ChannelWrapper(channel).id;
                    var isBlocked = settings.getBool(channelID, false);

                    var viewID = View.generateViewId();
                    var actions = (WidgetChannelsListItemChannelActions) cf.thisObject;
                    var scrollView = (NestedScrollView) actions.getView();
                    var lay = (LinearLayout) scrollView.getChildAt(0);
                    if (lay.findViewById(viewID) == null) {
                        TextView tw = new TextView(lay.getContext(), null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Icon);
                        tw.setId(viewID);
                        tw.setText(isBlocked ? "Unblock calls" : "Block calls");
                        lay.addView(tw, lay.getChildCount());
                        tw.setOnClickListener((v) -> {
                            settings.setBool(channelID, isBlocked ? false : true);
                            ((WidgetChannelsListItemChannelActions) cf.thisObject).dismiss();
                        });
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
