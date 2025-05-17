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

import com.discord.widgets.guilds.list.GuildListViewHolder
import com.discord.widgets.guilds.list.GuildListItem
import com.discord.stores.StoreStream
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.rx.ObservableExtensionsKt
import com.discord.utilities.color.ColorCompat

import rx.Observable.H as merge
import rx.Observable.B as fromIterable
import com.lytefast.flexinput.R

private const val GUILDS_ITEM_PROFILE_AVATAR_WRAP_ID = 0x7F0A0889

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    private var isReading = false

    override fun start(context: Context) {
        val storeReadStates = StoreStream.getReadStates()
        val api = RestAPI.api

        val viewId = View.generateViewId()
        val topMarginPx = DimenUtils.dpToPx(32.0f)
        val bottomMarginPx = DimenUtils.dpToPx(4.0f)

        patcher.after<GuildListViewHolder.FriendsViewHolder>("configure", GuildListItem.FriendsItem::class.java) {
            val layout = this.itemView as RelativeLayout

            if (layout.findViewById<TextView>(viewId) == null) {
                val textView = TextView(layout.context, null, 0, R.i.UiKit_TextView_Semibold).apply {
                    id = viewId
                    text = "Read All"
                    textSize = 14.0f
                    setTextColor(ColorCompat.getThemedColor(this.context, R.b.colorChannelDefault))

                    layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                        addRule(RelativeLayout.CENTER_HORIZONTAL)
                        addRule(RelativeLayout.BELOW, GUILDS_ITEM_PROFILE_AVATAR_WRAP_ID)
                        topMargin = topMarginPx
                        bottomMargin = bottomMarginPx
                    }
                }
                textView.setOnClickListener {
                    if (isReading) return@setOnClickListener

                    isReading = true
                    storeReadStates.getUnreadGuildIds().subscribe {
                        if (this.isEmpty()) {
                            isReading = false
                            return@subscribe
                        }

                        textView.text = "Wait..."

                        val observablesList = this.map { guildId ->
                            ObservableExtensionsKt.restSubscribeOn(api.ackGuild(guildId), false)
                        }

                        val mergedObservable = merge(fromIterable(observablesList))
                        ObservableExtensionsKt.ui(mergedObservable).subscribe(RxUtils.createActionSubscriber(
                            { },
                            { error ->
                                isReading = false
                                error.printStackTrace()
                                textView.text = "Read All"
                            },
                            {
                                isReading = false
                                textView.text = "Read All"
                            }
                        ))
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
