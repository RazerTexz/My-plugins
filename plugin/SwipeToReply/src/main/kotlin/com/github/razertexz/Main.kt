package com.github.razertexz

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils

import com.discord.widgets.chat.list.entries.*
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.databinding.WidgetChatListBinding
import com.discord.panels.OverlappingPanelsLayout
import com.discord.models.message.Message
import com.discord.stores.StoreStream

private const val DEFAULT_SCROLLING_SLOP_DP = 8.0f
private const val NEW_SCROLLING_SLOP_DP = DEFAULT_SCROLLING_SLOP_DP * 5.0f

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    override fun start(context: Context) {
        val endRecoverAnimation = ItemTouchHelper::class.java.getDeclaredMethod("endRecoverAnimation", RecyclerView.ViewHolder::class.java, Boolean::class.java).apply { isAccessible = true }
        val getBinding = WidgetChatList::class.java.getDeclaredMethod("getBinding").apply { isAccessible = true }
        val scrollingSlopPx = OverlappingPanelsLayout::class.java.getDeclaredField("scrollingSlopPx").apply { isAccessible = true }

        val widgetChatListActions = WidgetChatListActions()
        val storeChannels = StoreStream.getChannels()

        lateinit var itemTouchHelper: ItemTouchHelper
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            private lateinit var m_Message: Message

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val entry = (recyclerView.adapter as WidgetChatListAdapter).getData().getList()[viewHolder.adapterPosition]
                m_Message = when (entry) {
                    is MessageEntry -> entry.message
                    is AttachmentEntry -> entry.message
                    is StickerEntry -> entry.message
                    is EmbedEntry -> entry.message
                    else -> return 0
                }

                return makeMovementFlags(0, ItemTouchHelper.LEFT)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                endRecoverAnimation.invoke(itemTouchHelper, viewHolder, false)
                viewHolder.itemView.translationX = 0.0f

                WidgetChatListActions.`access$replyMessage`(widgetChatListActions, m_Message, storeChannels.getChannel(m_Message.channelId))
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
        })

        val newScrollingSlopPx = DimenUtils.dpToPx(NEW_SCROLLING_SLOP_DP).toFloat()
        patcher.after<OverlappingPanelsLayout>("initialize", AttributeSet::class.java) {
            scrollingSlopPx.set(this, newScrollingSlopPx)
        }

        patcher.after<WidgetChatList>("onViewBoundOrOnResume") {
            val recyclerView = (getBinding.invoke(this) as WidgetChatListBinding).b
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
