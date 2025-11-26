package com.github.razertexz

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.Utils
import com.aliucord.Http

import com.discord.stores.StoreStream
import com.discord.stores.StoreNotifications
import com.discord.utilities.rest.SendUtils
import com.discord.utilities.messagesend.`MessageQueue$doSend$2`
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.api.message.allowedmentions.MessageAllowedMentions
import com.discord.api.message.attachment.MessageAttachment
import com.discord.api.message.MessageReference
import com.discord.api.message.Message as ApiMessage
import com.discord.models.message.Message as ModelMessage

import com.lytefast.flexinput.R

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    private class MessagePayload(
        val content: String,
        val nonce: String,
        val allowed_mentions: MessageAllowedMentions?,
        val attachments: List<MessageAttachment>,
        val message_reference: MessageReference?,
        val flags: Int
    )

    override fun start(ctx: Context) {
        val viewId = View.generateViewId()
        val icon = ctx.getDrawable(R.e.ic_notifications_off_grey_24dp)!!.mutate()
        Utils.tintToTheme(icon)

        patcher.before<`MessageQueue$doSend$2`<*, *>>("call", SendUtils.SendPayload.ReadyToSend::class.java) {
            val message = `$message`
            if (message.content.startsWith("@silent ")) {
                Utils.threadPool.execute {
                    Http.Request.newDiscordRNRequest("https://discord.com/api/v10/channels/${message.channelId}/messages", "POST").executeWithJson(MessagePayload(message.content.substring(7).trimStart(), message.nonce, message.allowedMentions, message.attachments, message.messageReference, 4096))
                }

                it.result = null
            }
        }

        patcher.before<StoreNotifications>("handleMessageCreate", ApiMessage::class.java) {
            val message = it.args[0] as ApiMessage
            if (message.g() != StoreStream.getChannelsSelected().getId() && (message.l() and 4096L) != 0L)
                it.result = null
        }

        patcher.after<WidgetChatListAdapterItemMessage>("configureThreadSpine", ModelMessage::class.java, Boolean::class.java) {
            val message = it.args[0] as ModelMessage
            if (!message.isLoading) {
                val layout = (itemView as ViewGroup).getChildById<ConstraintLayout>(Utils.getResId("chat_list_adapter_item_text_header", "id")) ?: return@after
                val indicator = layout.getChildById<ImageView>(viewId) ?: ImageView(itemView.context).apply {
                    id = viewId
                    scaleX = 0.75f
                    scaleY = 0.75f
                    setImageDrawable(icon)

                    layout.addView(this, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                        val id = Utils.getResId("chat_list_adapter_item_text_timestamp", "id")

                        startToEnd = id
                        topToTop = id
                        bottomToBottom = id
                    })
                }

                indicator.visibility = if ((message.flags and 4096L) != 0L) View.VISIBLE else View.GONE
            }
        }
    }

    private inline fun <reified T : View> ViewGroup.getChildById(id: Int): T? {
        var idx = childCount - 1

        while (idx >= 0) {
            val child = getChildAt(idx)
            if (child is T && child.id == id)
                return child as T

            idx--
        }

        return null
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}