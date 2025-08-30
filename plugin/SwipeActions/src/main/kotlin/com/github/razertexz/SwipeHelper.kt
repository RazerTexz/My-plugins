package com.github.razertexz

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.MotionEvent
import android.view.ViewConfiguration

import com.discord.widgets.chat.list.entries.*
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.models.message.Message
import com.discord.stores.StoreStream

import kotlin.math.abs
import kotlin.math.sign

class SwipeHelper {
    private lateinit var recyclerView: RecyclerView

    private var touchSlop = 0.0f
    private val swipeThreshold = 0.25f

    private val widgetChatListActions = WidgetChatListActions()
    private val storeChannels = StoreStream.getChannels()
    private val myId = StoreStream.getUsers().me.id

    private val onItemTouchListener = object : RecyclerView.OnItemTouchListener {
        private lateinit var message: Message

        private var selectedView: View? = null
        private var initialX = 0.0f
        private var initialY = 0.0f

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val child = rv.findChildViewUnder(e.x, e.y) ?: return false

                    val pos = rv.getChildViewHolder(child).adapterPosition.takeUnless { it == RecyclerView.NO_POSITION } ?: return false
                    val entry = (rv.adapter as WidgetChatListAdapter).getData().getList()[pos]
                    message = when (entry) {
                        is MessageEntry -> entry.message
                        is AttachmentEntry -> entry.message
                        is StickerEntry -> entry.message
                        is EmbedEntry -> entry.message
                        else -> return false
                    }

                    selectedView = child
                    initialX = e.x
                    initialY = e.y
                }

                MotionEvent.ACTION_MOVE -> {
                    if (selectedView != null && rv.getScrollState() != RecyclerView.SCROLL_STATE_DRAGGING) {
                        val diffX = abs(e.x - initialX)
                        val diffY = abs(e.y - initialY)
                        if (diffX > touchSlop && diffX > diffY * 3.0f) {
                            rv.requestDisallowInterceptTouchEvent(true)
                            return true
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    selectedView = null
                }
            }

            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            selectedView!!.let { view ->
                when (e.actionMasked) {
                    MotionEvent.ACTION_MOVE -> {
                        view.translationX = e.x - initialX
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (abs(view.translationX) > view.width * swipeThreshold)
                            onSwiped(view.translationX.sign)

                        view.translationX = 0.0f
                        selectedView = null
                    }
                }
            }
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        private fun onSwiped(dir: Float) {
            if (dir == -1.0f)
                WidgetChatListActions.`access$replyMessage`(widgetChatListActions, message, storeChannels.getChannel(message.channelId))
            else if (message.author.id == myId)
                WidgetChatListActions.`access$editMessage`(widgetChatListActions, message)
        }
    }

    fun attachToRecyclerView(rv: RecyclerView) {
        if (this::recyclerView.isInitialized) {
            if (recyclerView == rv)
                return

            recyclerView.removeOnItemTouchListener(onItemTouchListener)
        }

        touchSlop = ViewConfiguration.get(rv.context).scaledTouchSlop.toFloat()

        recyclerView = rv
        recyclerView.addOnItemTouchListener(onItemTouchListener)
    }
}