package com.github.razertexz

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.RelativeLayout

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.RxUtils
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.utils.DimenUtils
import com.aliucord.Utils

import com.discord.widgets.guilds.list.GuildListViewHolder
import com.discord.widgets.guilds.list.GuildListItem
import com.discord.stores.StoreStream
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.rx.ObservableExtensionsKt

import rx.Observable.H as merge
import rx.Observable.B as fromIterable

private const val GUILDS_ITEM_PROFILE_AVATAR_WRAP_ID = 0x7F0A0889

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    private var isReading = false

    override fun start(context: Context) {
        val storeReadStates = StoreStream.getReadStates()
        val api = RestAPI.api

        val viewId = View.generateViewId()
        val marginPx = DimenUtils.dpToPx(26.0f)

        patcher.after<GuildListViewHolder.FriendsViewHolder>("configure", GuildListItem.FriendsItem::class.java) {
            val layout = this.itemView as RelativeLayout

            if (layout.findViewById<TextView>(viewId) == null) {
                val textView = TextView(layout.context).apply {
                    id = viewId
                    text = "Read All"
                    textSize = 14.0f

                    layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                        addRule(RelativeLayout.CENTER_HORIZONTAL)
                        addRule(RelativeLayout.BELOW, GUILDS_ITEM_PROFILE_AVATAR_WRAP_ID)
                        topMargin = marginPx
                    }

                    setOnClickListener {
                        if (isReading) return@setOnClickListener

                        isReading = true
                        Utils.showToast("Reading all guilds...")

                        storeReadStates.getUnreadGuildIds().subscribe {
                            if (this.isEmpty()) {
                                isReading = false
                                return@subscribe
                            }

                            val observablesList = this.map { guildId ->
                                ObservableExtensionsKt.restSubscribeOn(api.ackGuild(guildId), false)
                            }

                            val mergedObservable = merge(fromIterable(observablesList))
                            ObservableExtensionsKt.ui(mergedObservable).subscribe(RxUtils.createActionSubscriber(
                                { },
                                { },
                                {
                                    isReading = false
                                    Utils.showToast("All guilds marked as read!")
                                }
                            ))
                        }
                    }
                }

                layout.layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT
                layout.addView(textView)
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
