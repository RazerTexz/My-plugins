package com.github.razertexz;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.CollectionUtils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.api.SettingsAPI;

import com.discord.widgets.voice.call.WidgetVoiceCallIncoming;
import com.discord.widgets.voice.model.CallModel;
import com.discord.stores.StoreVoiceParticipants;
import com.discord.stores.StoreCallsIncoming;
import com.discord.models.domain.ModelCall;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
public class Main extends Plugin {
    private final SettingsAPI settings = new SettingsAPI("AutoBlockCalls");

    @Override
    public void start(Context context) throws Throwable {
        /*patcher.patch(WidgetVoiceCallIncoming.class.getDeclaredMethod("configureUI", WidgetVoiceCallIncoming.Model.class),
            new Hook((param) -> {
                var state = (WidgetVoiceCallIncoming.Model) param.args[0];
                var callModel = (CallModel) state.component1();
                var dmRecipient = (StoreVoiceParticipants.VoiceUser) callModel.getDmRecipient();
                var name = (String) dmRecipient.getDisplayName();
                
                if (name != null && name.equalsIgnoreCase("XyuzalKiller")) {
                    //var thisClass = (WidgetVoiceCallIncoming) param.thisObject;
                    //thisClass.onEmptyCallModel();
                    Utils.showToast("WidgetVoiceCallIncoming " + name);
                }
            })
        );*/
        
        patcher.patch(StoreCallsIncoming.class.getDeclaredMethod("handleCallCreateOrUpdate", ModelCall.class),
            new Hook((param) -> {
                var state = (ModelCall) param.args[0];
                var channelId = "" + state.getChannelId();
                var thisClass = (StoreCallsIncoming) param.thisObject;
                thisClass.handleCallDelete(state);
                Utils.showToast("Voice Call from " + channelId);
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
