package com.github.razertexz

import android.content.Context
import android.widget.TextView

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.Utils

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(ctx: Context) {
        patcher.before<TextView>("setRawTextSize", Float::class.java, Boolean::class.java) {
            val newTextSize = when (id) {
                getViewId("chat_list_adapter_item_text") -> settings.getFloat("messagesFontScale", 0.0f)
                getViewId("text_input") -> settings.getFloat("chatBoxFontScale", 0.0f)
                getViewId("username_text"), getViewId("chat_list_adapter_item_text_name") -> settings.getFloat("userNameFontScale", 0.0f)
                getViewId("about_me_text") -> settings.getFloat("aboutMeFontScale", 0.0f)
                getViewId("channel_members_list_item_game") -> settings.getFloat("gameStatusFontScale", 0.0f)
                getViewId("user_profile_header_custom_status") -> settings.getFloat("profileStatusFontScale", 0.0f)
                else -> return@before
            }

            it.args[0] = newTextSize * ctx.resources.displayMetrics.scaledDensity
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()

    private inline fun getViewId(name: String): Int = Utils.getResId(name, "id")
}