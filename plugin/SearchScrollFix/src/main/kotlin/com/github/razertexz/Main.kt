package com.github.razertexz

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import android.content.Context

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*

import com.discord.widgets.search.results.WidgetSearchResults
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    override fun start(context: Context) {
        lateinit var layoutManager: LinearLayoutManager
        var lastPosition = 0

        patcher.after<WidgetSearchResults>("addThreadSpineItemDecoration", WidgetChatListAdapter::class.java) {
            val adapter = it.args[0] as WidgetChatListAdapter

            layoutManager = adapter.layoutManager

            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    layoutManager.scrollToPositionWithOffset(lastPosition, 0)
                    adapter.unregisterAdapterDataObserver(this)
                }
            })
        }

        patcher.before<WidgetSearchResults>("onPause") {
            lastPosition = layoutManager.findFirstVisibleItemPosition()
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
