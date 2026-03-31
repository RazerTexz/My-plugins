package com.github.razertexz

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.utils.DimenUtils

import com.discord.utilities.color.ColorCompat
import com.discord.utilities.drawable.DrawableCompat

import com.lytefast.flexinput.fragment.FlexInputFragment
import com.lytefast.flexinput.R

import de.robv.android.xposed.XC_MethodHook

@AliucordPlugin(requiresRestart = false)
class MarkdownToolbar : Plugin() {
    override fun start(context: Context) {
        patcher.patch(FlexInputFragment::class.java, "onViewCreated", arrayOf(View::class.java, Bundle::class.java), object : XC_MethodHook() {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                val editText = (param.thisObject as FlexInputFragment).m
                val layout = param.args[0] as LinearLayout
                
                val ctx = layout.context
                val normalInteractiveColor = ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal)
                val highlightBgRes = DrawableCompat.getThemedDrawableRes(ctx, R.b.bg_pressed_highlight)

                fun LinearLayout.addButton(label: String, prefix: String, suffix: String) {
                    addView(TextView(ctx).apply {
                        text = label

                        setTextColor(normalInteractiveColor)
                        setBackgroundResource(highlightBgRes)

                        gravity = Gravity.CENTER

                        setOnClickListener {
                            val editable = editText.text ?: return@setOnClickListener

                            val rawStart = editText.selectionStart
                            val rawEnd = editText.selectionEnd
                            val start = minOf(rawStart, rawEnd)
                            val end = maxOf(rawStart, rawEnd)

                            editable.insert(end, suffix)
                            editable.insert(start, prefix)
                            editText.setSelection(start + prefix.length, end + prefix.length)
                        }
                    }, LinearLayout.LayoutParams(0, DimenUtils.dpToPx(32.0f), 1.0f))
                }

                layout.addView(View(ctx).apply {
                    setBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorPrimaryDivider))
                }, 2, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DimenUtils.dpToPx(1.0f)))

                layout.addView(LinearLayout(ctx).apply {
                    setBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondary))

                    addButton("B", "**", "**")
                    addButton("I", "*", "*")
                    addButton("U", "__", "__")
                    addButton("S", "~~", "~~")
                    addButton("||", "||", "||")
                    addButton("</>", "`", "`")
                }, 3)
            }
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}