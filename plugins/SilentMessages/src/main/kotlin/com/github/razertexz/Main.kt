package com.github.razertexz

import android.content.Context

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.Utils
import com.aliucord.Http

import com.discord.api.message.allowedmentions.MessageAllowedMentions
import com.discord.api.message.attachment.MessageAttachment
import com.discord.api.message.MessageReference
import com.discord.api.message.Message

import com.discord.stores.StoreStream
import com.discord.stores.StoreNotifications
import com.discord.utilities.rest.SendUtils
import com.discord.utilities.messagesend.`MessageQueue$doSend$2`

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
        patcher.before<`MessageQueue$doSend$2`<*, *>>("call", SendUtils.SendPayload.ReadyToSend::class.java) {
            val message = `$message`
            if (message.content.startsWith("@silent ")) {
                Utils.threadPool.execute {
                    Http.Request.newDiscordRNRequest("https://discord.com/api/v10/channels/${message.channelId}/messages", "POST").executeWithJson(MessagePayload(message.content.substring(7).trimStart(), message.nonce, message.allowedMentions, message.attachments, message.messageReference, 4096))
                }

                it.result = null
            }
        }

        patcher.before<StoreNotifications>("handleMessageCreate", Message::class.java) {
            val message = it.args[0] as Message
            if (message.g() != StoreStream.getChannelsSelected().getId() && (message.l() and 4096L) != 0L) {
                it.result = null
            }
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}