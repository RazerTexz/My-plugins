package com.github.razertexz

import com.google.android.material.card.MaterialCardView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Space
import android.view.Gravity
import android.content.Context

import com.aliucord.Constants
import com.aliucord.utils.DimenUtils
import com.aliucord.views.Divider
import com.aliucord.views.Button
import com.aliucord.views.DangerButton
import com.aliucord.views.ToolbarButton

import com.discord.utilities.color.ColorCompat

import com.lytefast.flexinput.R

class PluginWebCard(context: Context) : MaterialCardView(context) {
    private val p = DimenUtils.defaultPadding

    val titleView = TextView(context, null, 0, R.i.UiKit_TextView_Semibold).apply {
        textSize = 16.0f
        setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundSecondaryAlt))
        setPadding(p, p, p, p)
    }

    val descriptionView = TextView(context, null, 0, R.i.UiKit_Settings_Item_Addition).apply {
        setPadding(p, p, p, p)
    }

    val changelogButton = ToolbarButton(context).apply {
        setImageDrawable(context.getDrawable(R.e.ic_history_white_24dp))
    }

    val installButton = Button(context).apply {
        text = "Install"
    }

    val uninstallButton = DangerButton(context).apply {
        text = "Uninstall"
    }

    init {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = p }
        radius = DimenUtils.defaultCardRadius.toFloat()
        setCardBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundSecondary))

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            addView(titleView)
            addView(Divider(context))
            addView(descriptionView)
            addView(LinearLayout(context).apply {
                isBaselineAligned = false
                gravity = Gravity.CENTER_VERTICAL
                setPadding(p, 0, p, p / 2)

                addView(changelogButton)
                addView(Space(context), LinearLayout.LayoutParams(0, 0, 1.0f))
                addView(installButton)
                addView(uninstallButton)
            })
        })
    }
}