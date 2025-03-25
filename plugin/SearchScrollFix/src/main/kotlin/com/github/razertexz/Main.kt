package com.github.razertexz;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;
import android.content.Context;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;

import com.discord.widgets.search.results.WidgetSearchResults;

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    lateinit var linearLayoutManager: LinearLayoutManager
    var lastPosition: Int = 0

    override fun start(context: Context) {
        patcher.after<WidgetSearchResults>("onViewBound", View::class.java) {
            val recyclerView: RecyclerView = it.args[0] as RecyclerView

            linearLayoutManager = recyclerView.getLayoutManager() as LinearLayoutManager

            recyclerView.getAdapter()!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    linearLayoutManager.scrollToPositionWithOffset(lastPosition, 0)
                    recyclerView.getAdapter()!!.unregisterAdapterDataObserver(this)
                }
            })
        }

        patcher.before<WidgetSearchResults>("onPause") {
            lastPosition = linearLayoutManager.findFirstVisibleItemPosition()
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
