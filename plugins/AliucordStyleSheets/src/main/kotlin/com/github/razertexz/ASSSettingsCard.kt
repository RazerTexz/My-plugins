package com.github.razertexz

import com.google.android.material.card.MaterialCardView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Space
import android.view.Gravity
import android.content.Context

import com.aliucord.utils.DimenUtils
import com.aliucord.views.Divider
import com.aliucord.views.DangerButton

import com.discord.utilities.color.ColorCompat

import com.lytefast.flexinput.R

internal class ASSSettingsCard(context: Context) : MaterialCardView(context) {
    private val p = DimenUtils.defaultPadding

    val titleView = TextView(context, null, 0, R.i.UiKit_TextView_Semibold).apply {
        textSize = 16.0f
        setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundSecondaryAlt))
        setPadding(p, p, p, p)
    }

    val removeButton = DangerButton(context).apply {
        text = "Remove"
    }

    init {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = p }
        radius = DimenUtils.defaultCardRadius.toFloat()
        setCardBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundSecondary))

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            addView(titleView)
            addView(Divider(context))
            addView(LinearLayout(context).apply {
                isBaselineAligned = false
                gravity = Gravity.CENTER_VERTICAL
                setPadding(p, 0, p, p / 2)

                addView(Space(context), LinearLayout.LayoutParams(0, 0, 1.0f))
                addView(removeButton)
            })
        })
    }
}