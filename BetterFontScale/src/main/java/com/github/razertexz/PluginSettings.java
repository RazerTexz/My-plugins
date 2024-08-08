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

        TextInput fontScaleInput = new TextInput(
            context,
            "Font Scale (0 to disable)",
            String.valueOf(settings.getFloat("fontScale", 0.0f)),
            new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    var scale = s.toString();
                    if (!scale.equals("")) {
                        settings.setFloat("fontScale", Float.parseFloat(scale));
                        Utils.promptRestart();
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            }
        );

        TextInput chatBoxFontScaleInput = new TextInput(
            context,
            "Chatbox Font Scale (0 to disable)",
            String.valueOf(settings.getFloat("chatBoxFontScale", 0.0f)),
            new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    var scale = s.toString();
                    if (!scale.equals("")) {
                        settings.setFloat("chatBoxFontScale", Float.parseFloat(scale));
                        Utils.promptRestart();
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            }
        );

        addView(fontScaleInput);
        addView(chatBoxFontScaleInput);
    }
}