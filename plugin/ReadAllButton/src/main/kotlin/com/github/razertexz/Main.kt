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
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat

import com.lytefast.flexinput.R

private const val GUILDS_ITEM_PROFILE_AVATAR_WRAP_ID = 0x7F0A0889
private const val READ_ALL_TEXT = "Read All"

private class ReadStateAck(val channel_id: Long, val message_id: Long)
private class Payload(val read_states: List<ReadStateAck>)

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    private var isReading = false

    override fun start(context: Context) {
        val storeReadStates = StoreStream.getReadStates()
        val storeChannels = StoreStream.getChannels()

        val viewId = View.generateViewId()
        val topMarginPx = DimenUtils.dpToPx(36.0f)
        val bottomMarginPx = DimenUtils.dpToPx(4.0f)

        patcher.after<GuildListViewHolder.FriendsViewHolder>("configure", GuildListItem.FriendsItem::class.java) {
            val layout = this.itemView as RelativeLayout

            if (layout.findViewById<View>(viewId) == null) {
                val textView = TextView(layout.context, null, 0, R.i.UiKit_TextView_Semibold).apply {
                    id = viewId
                    text = READ_ALL_TEXT
                    textSize = 14.0f
                    setTextColor(ColorCompat.getThemedColor(this.context, R.b.colorChannelDefault))

                    layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                        addRule(RelativeLayout.CENTER_HORIZONTAL)
                        addRule(RelativeLayout.BELOW, GUILDS_ITEM_PROFILE_AVATAR_WRAP_ID)
                        topMargin = topMarginPx
                        bottomMargin = bottomMarginPx
                    }

                    setOnClickListener {
                        if (isReading) return@setOnClickListener

                        // Z(int) is take(int)
                        isReading = true
                        storeReadStates.getUnreadChannelIds().Z(1).subscribe {
                            if (this.isEmpty()) {
                                isReading = false
                                return@subscribe
                            }

                            this@apply.text = "Wait..."

                            Utils.threadPool.execute {
                                val readStates = this.map { channelId -> ReadStateAck(channelId, storeChannels.getChannel(channelId).l()) }
                                readStates.chunked(100) { chunk ->
                                    Http.Request.newDiscordRNRequest("https://discord.com/api/v9/read-states/ack-bulk", "POST").executeWithJson(Payload(chunk))
                                }

                                Utils.mainThread.post {
                                    this@apply.text = READ_ALL_TEXT
                                    isReading = false
                                    Utils.promptRestart("ReadAllButton wants you to restart :)")
                                }
                            }
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
