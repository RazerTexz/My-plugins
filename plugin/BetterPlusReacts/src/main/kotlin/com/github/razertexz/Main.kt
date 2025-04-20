package com.github.razertexz

import android.content.Context

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.utils.ReflectUtils
import com.aliucord.Utils

import java.util.regex.Pattern
import java.util.List
import kotlin.jvm.functions.Function1

import com.discord.stores.StoreMessagesHolder
import com.discord.stores.StoreChannelsSelected
import com.discord.stores.StoreStream
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.chat.MessageManager
import com.discord.widgets.chat.input.ChatInputViewModel
import com.discord.utilities.rest.RestAPI

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    val pattern = Pattern.compile("^\\s*(\\++)(<a?:\\w+:(\\d{19})>|.)$")

    override fun start(context: Context) {
        val storeMessagesHolder = ReflectUtils.getField(StoreStream.getMessages(), "holder") as StoreMessagesHolder
        val storeChannelsSelected = StoreStream.getChannelsSelected()
        val storeEmoji = StoreStream.getEmojis()
        val api = RestAPI.api

        val unicodeEmojis = storeEmoji.unicodeEmojiSurrogateMap.keys

        patcher.before<ChatInputViewModel>("sendMessage", Context::class.java, MessageManager::class.java, MessageContent::class.java, List::class.java, Boolean::class.java, Function1::class.java) {
            val textContent = (it.args[2] as MessageContent).textContent

            val matcher = pattern.matcher(textContent)
            if (!matcher.find()) return@before

            var emoji = matcher.group(2)
            if (!unicodeEmojis.contains(emoji))  {
                val customEmojiId = matcher.group(3)?.toLong() ?: return@before
                emoji = storeEmoji.getCustomEmojiInternal(customEmojiId)?.getReactionKey() ?: return@before
            }

            val selectedChannelId = storeChannelsSelected.getId()
            val messages = storeMessagesHolder
                .getMessagesForChannel(selectedChannelId)!!
                .values

            val plusAmount = matcher.group(1)!!.length
            if (plusAmount > messages.size) return@before

            val messageId = messages.elementAt(messages.size - plusAmount).id
            Utils.threadPool.execute {
                api.addReaction(selectedChannelId, messageId, emoji).subscribe {}
            }

            (it.args[5] as Function1<in Boolean, out Unit>).invoke(true)
            it.setResult(null)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
