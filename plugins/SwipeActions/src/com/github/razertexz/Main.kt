package com.github.razertexz

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.util.AttributeSet

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils

import com.discord.databinding.WidgetChatListBinding
import com.discord.panels.OverlappingPanelsLayout

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    override fun start(ctx: Context) {
        val scrollingSlopPx = OverlappingPanelsLayout::class.java.getDeclaredField("scrollingSlopPx").apply { isAccessible = true }
        val newScrollingSlopPx = DimenUtils.dpToPx(8.0f * 3.5f).toFloat()
        patcher.after<OverlappingPanelsLayout>("initialize", AttributeSet::class.java) {
            scrollingSlopPx.set(this, newScrollingSlopPx)
        }

        val swipeHelper = SwipeHelper()
        patcher.after<WidgetChatListBinding>(RecyclerView::class.java, RecyclerView::class.java) {
            swipeHelper.attachToRecyclerView(it.args[0] as RecyclerView)
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}