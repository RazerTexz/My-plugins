package com.github.razertexz;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;
import android.content.Context;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;

import com.discord.widgets.search.results.WidgetSearchResults;

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    lateinit var linearLayoutManager: LinearLayoutManager
    var lastPosition: Int = 0
    val searchResultsListId: Int = Utils.getResId("search_results_list", "id")

    override fun start(context: Context) {
        patcher.after<WidgetSearchResults>("onViewBound", View::class.java) {
            val recyclerView: RecyclerView = requireView().findViewById<RecyclerView>(searchResultsListId)

            linearLayoutManager = recyclerView.getLayoutManager() as LinearLayoutManager
            linearLayoutManager.scrollToPositionWithOffset(lastPosition, 0)

            recyclerView.postOnAnimation {
                if (linearLayoutManager.findFirstVisibleItemPosition() != lastPosition) {
                    linearLayoutManager.scrollToPositionWithOffset(lastPosition, 0)
                }
            }
        }

        patcher.before<WidgetSearchResults>("onPause") {
            lastPosition = linearLayoutManager.findFirstVisibleItemPosition()
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
