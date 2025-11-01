package com.github.razertexz

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import android.content.Context

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*

import com.discord.widgets.search.results.WidgetSearchResults

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    override fun start(ctx: Context) {
        var lastPos = -1

        patcher.after<WidgetSearchResults>("configureUI", WidgetSearchResults.Model::class.java) {
            if (lastPos >= 0) {
                ((view as RecyclerView).layoutManager as LinearLayoutManager).scrollToPositionWithOffset(lastPos, 0)
                lastPos = -1
            }
        }

        patcher.before<WidgetSearchResults>("onPause") {
            lastPos = ((view as RecyclerView).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}