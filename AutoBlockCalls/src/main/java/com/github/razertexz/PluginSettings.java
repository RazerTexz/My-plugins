package com.github.razertexz;

import android.view.View;

import com.aliucord.Utils.showToast;
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

        setActionBarTitle("Auto Block Calls");
        setPadding(0);
        
        addView(Button(context)
            .setText("Unblock all")
            .setOnClickListener(v -> {
                if (settings.resetSettings()) {
                    showToast("Successfully unblock all");
                } else {
                    showToast("Failed");
                }
            })
        );
    }
}