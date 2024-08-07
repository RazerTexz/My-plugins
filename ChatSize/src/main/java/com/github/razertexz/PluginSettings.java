package com.github.razertexz;

import android.view.View;
import android.text.TextWatcher;

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

        setActionBarTitle("Chat Size Settings");
        setPadding(0);
        
        TextInput fontScaleInput = new TextInput(
            context,
            "Font scale",
            String.valueOf(settings.getFloat("fontScale", 34f),
            new TextWatcher() {
                public void afterTextChanged(Editable editable) {
                    var scale = editable.toString();
                    if (!scale.equals("")) {
                        settings.setFloat("fontScale", Float.parseFloat(scale));
                        Utils.promptRestart();
                    }
                }
            }
        );

        addView(fontScaleInput);
    }
}