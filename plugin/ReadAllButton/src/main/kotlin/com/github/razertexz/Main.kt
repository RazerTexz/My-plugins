package com.github.razertexz

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.RelativeLayout

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.utils.DimenUtils

import com.discord.widgets.guilds.list.GuildListViewHolder
import com.discord.widgets.guilds.list.GuildListItem
import com.discord.utilities.color.ColorCompat
import com.discord.stores.StoreStream

import com.lytefast.flexinput.R

private class ReadStateAck(val channel_id: Long, val message_id: Long)
private class Payload(val read_states: List<ReadStateAck>)

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    override fun start(ctx: Context) {
        val storeReadStates = StoreStream.getReadStates()
        val storeMessagesMostRecent = StoreStream.getMessagesMostRecent()

        val viewId = View.generateViewId()
        val topMarginPx = DimenUtils.dpToPx(36.0f)
        val bottomMarginPx = DimenUtils.dpToPx(4.0f)

        patcher.after<GuildListViewHolder.FriendsViewHolder>("configure", GuildListItem.FriendsItem::class.java) {
            val layout = itemView as RelativeLayout

            for (i in 0 until layout.childCount) {
                if (layout.getChildAt(i).id == viewId) {
                    return@after
                }
            }

            val textView = TextView(layout.context, null, 0, R.i.UiKit_TextView_Semibold).apply {
                id = viewId
                text = "Read All"
                textSize = 14.0f
                setTextColor(ColorCompat.getThemedColor(context, R.b.colorChannelDefault))

                setOnClickListener {
                    // Z(int) is take(int)
                    storeReadStates.getUnreadChannelIds().Z(1).subscribe {
                        if (isEmpty()) return@subscribe

                        this@apply.visibility = View.GONE

                        Utils.threadPool.execute {
                            val mostRecentIds = storeMessagesMostRecent.getMostRecentIds()
                            val readStateAcks = map { channelId -> ReadStateAck(channelId, mostRecentIds[channelId] ?: 0) }

                            readStateAcks.chunked(100) { chunk ->
                                Http.Request.newDiscordRNRequest("https://discord.com/api/v9/read-states/ack-bulk", "POST").executeWithJson(Payload(chunk))
                            }

                            Utils.mainThread.post {
                                this@apply.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                addRule(RelativeLayout.BELOW, Utils.getResId("guilds_item_profile_avatar_wrap", "id"))
                topMargin = topMarginPx
                bottomMargin = bottomMarginPx
            }

            layout.layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT
            layout.addView(textView, params)
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}
