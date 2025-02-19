package com.github.razertexz;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;
import android.content.Context;
import android.view.ViewTreeObserver;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;

import com.discord.widgets.search.results.WidgetSearchResults;

import java.util.*;

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    private val searchResultsListId: Int = Utils.getResId("search_results_list", "id")

    override fun start(context: Context) {
        lateinit var linearLayoutManager: LinearLayoutManager
        var lastPosition: Int = 0

        patcher.after<WidgetSearchResults>("onViewBound", View::class.java) {
            val recyclerView: RecyclerView = requireView().findViewById<RecyclerView>(searchResultsListId)
            linearLayoutManager = recyclerView.getLayoutManager() as LinearLayoutManager

            if (lastPosition != 0) {
                recyclerView.getViewTreeObserver().addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        recyclerView.post {
                            linearLayoutManager.scrollToPositionWithOffset(lastPosition, 0)
                        }

                        recyclerView.getViewTreeObserver().removeOnPreDrawListener(this)
                        return true
                    }
                })
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
