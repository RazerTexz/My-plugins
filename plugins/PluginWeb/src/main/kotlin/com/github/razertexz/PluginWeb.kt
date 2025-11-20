package com.github.razertexz

import androidx.core.content.res.ResourcesCompat
import android.content.Context
import android.widget.TextView
import android.view.ViewGroup
import android.view.View

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.Utils
import com.aliucord.Constants

import com.discord.widgets.settings.WidgetSettings
import com.discord.utilities.color.ColorCompat

import com.lytefast.flexinput.R

@AliucordPlugin(requiresRestart = false)
class PluginWeb : Plugin() {
    override fun start(ctx: Context) {
        patcher.after<WidgetSettings>("onViewBound", View::class.java) {
            val layout = ((it.args[0] as ViewGroup).getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup
            var idx = layout.childCount - 1
            while (idx >= 0)
                if (layout.getChildAt(idx--).id == Utils.getResId("app_info_header", "id"))
                    break

            layout.addView(TextView(layout.context, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                text = "Open Plugin Web"
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)

                setCompoundDrawablesRelativeWithIntrinsicBounds(context.getDrawable(R.e.ic_upload_24dp)!!.mutate().apply {
                    setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal))
                }, null, null, null);

                setOnClickListener { Utils.openPageWithProxy(it.context, PluginWebPage()) }
            }, idx)
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}