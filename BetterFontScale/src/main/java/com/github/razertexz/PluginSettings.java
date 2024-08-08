package com.github.razertexz;

import android.view.View;
import android.text.TextWatcher;
import android.text.Editable;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.TextInput;

public class PluginSettings extends SettingsPage {
    private final SettingsAPI settings;
    
    public PluginSettings(SettingsAPI settings) {
        this.settings = settings;
    }
    
    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        var context = getContext();

        setActionBarTitle("Better Font Scale Settings");
        setPadding(0);

        createTextInput(context, "Font Scale (0 to disable)", "fontScale");
        createTextInput(context, "Chatbox Font Scale (0 to disable)", "chatBoxFontScale");
        createTextInput(context, "Username Font Scale (0 to disable)", "userNameFontScale");
        createTextInput(context, "Tag Font Scale (0 to disable)", "tagFontScale");
        createTextInput(context, "Game/Status Font Scale (0 to disable)", "gameFontScale");
    }

    private void createTextInput(Context context, CharSequence name, String settingName) {
        TextInput input = new TextInput(
            context,
            name,
            String.valueOf(settings.getFloat(settingName, 0.0f)),
            new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    var scale = s.toString();
                    if (!scale.equals("")) {
                        settings.setFloat(settingName, Float.parseFloat(scale));
                        Utils.promptRestart();
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            }
        );

        addView(input);
    }
}