package com.github.razertexz

import androidx.core.content.res.ResourcesCompat
import android.content.Context
import android.widget.TextView
import android.view.ViewGroup
import android.view.View

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.Constants
import com.aliucord.Utils

import com.discord.widgets.settings.WidgetSettings
import com.discord.utilities.color.ColorCompat

import com.lytefast.flexinput.R
import de.robv.android.xposed.XC_MethodHook

@AliucordPlugin(requiresRestart = false)
internal class PluginWeb : Plugin() {
    override fun start(ctx: Context) {
        patcher.patch(WidgetSettings::class.java, "onViewBound", arrayOf(View::class.java), object : XC_MethodHook(10000) {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                val layout = ((param.args[0] as ViewGroup).getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup
                var idx = layout.childCount - 1
                while (idx >= 0) {
                    val child = layout.getChildAt(--idx)
                    if (child is TextView && child.text == "Open Debug Log") {
                        idx += 1
                        break
                    }
                }

                layout.addView(TextView(layout.context, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                    text = "Open Plugin Web"
                    typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)

                    setCompoundDrawablesRelativeWithIntrinsicBounds(context.getDrawable(R.e.ic_upload_24dp)!!.mutate().apply {
                        setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal))
                    }, null, null, null);

                    setOnClickListener { Utils.openPageWithProxy(it.context, PluginWebPage()) }
                }, idx)
            }
        })
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}