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
import com.discord.widgets.chat.input.models.ApplicationCommandData
import com.discord.widgets.chat.input.`WidgetChatInput$configureSendListeners$2`
import com.discord.utilities.rest.RestAPI

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    val pattern = Pattern.compile("^\\s*(\\++):(.+):$")

    override fun start(context: Context) {
        val storeMessagesHolder = ReflectUtils.getField(StoreStream.getMessages(), "holder") as StoreMessagesHolder
        val storeChannelsSelected = StoreStream.getChannelsSelected()
        val unicodeEmojisNamesMap = StoreStream.getEmojis().unicodeEmojisNamesMap
        val api = RestAPI.api

        patcher.before<`WidgetChatInput$configureSendListeners$2`>("invoke", List::class.java, ApplicationCommandData::class.java, Function1::class.java) {
            val _this = it.thisObject as `WidgetChatInput$configureSendListeners$2`
            val inputText = _this.`$chatInput`.getText()

            val matcher = pattern.matcher(inputText)
            if (!matcher.find()) return@before

            val unicodeEmoji = unicodeEmojisNamesMap[matcher.group(2)] 
            if (unicodeEmoji == null) return@before

            val selectedChannelId = storeChannelsSelected.getId()
            val messages = storeMessagesHolder
                .getMessagesForChannel(selectedChannelId)!!
                .descendingMap()
                .values

            val plusAmount = matcher.group(1)!!.length
            if (plusAmount > messages.size) return@before

            val messageId = messages.elementAt(plusAmount - 1).id
            Utils.threadPool.execute {
                api.addReaction(selectedChannelId, messageId, unicodeEmoji.getReactionKey()).subscribe {}
            }

            (it.args[2] as Function1<in Boolean, out Unit>).invoke(true)
            it.setResult(null)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}