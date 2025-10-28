package com.github.razertexz;

import android.view.View;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Button;

public class PluginSettings extends SettingsPage {
    private final SettingsAPI settings;
    
    public PluginSettings(SettingsAPI settings) {
        this.settings = settings;
    }
    
    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        var context = getContext();

        setActionBarTitle("Block Them Calls");
        setPadding(0);
        
        Button unblockButton = new Button(context);
        unblockButton.setText("Unblock all user(s)");
        unblockButton.setOnClickListener(v -> { 
            if (settings.resetSettings()) {
                Utils.showToast("Successfully unblocked all user(s)");
            } else {
                Utils.showToast("Failed to unblock");
            }
        });

        addView(unblockButton);
    }
}