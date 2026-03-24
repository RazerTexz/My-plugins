package com.github.razertexz

import android.content.Context
import android.text.Spanned
import android.text.format.DateUtils
import android.text.style.RelativeSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.LongSparseArray

import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

import com.discord.models.message.Message
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.textprocessing.DiscordParser
import com.discord.utilities.textprocessing.MessagePreprocessor
import com.discord.utilities.textprocessing.MessageRenderContext
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.stores.StoreMessages
import com.discord.stores.StoreMessageState
import com.discord.stores.StoreStream
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.lytefast.flexinput.R

import de.robv.android.xposed.XC_MethodHook

import java.lang.System
import kotlin.jvm.functions.Function1

@AliucordPlugin(requiresRestart = false)
class SimpleMessageLogger : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        val deletedMessages = LongSparseArray<Long>()
        val editedMessages = LongSparseArray<ArrayList<Pair<String, Long>>>()

        patcher.patch(StoreMessages::class.java, "handleMessageDelete", arrayOf(Long::class.java, List::class.java), object : XC_MethodHook() {
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                if (!settings.getBool("logDeletedMessages", true)) {
                    return
                }

                val widgetChatList = Utils.widgetChatList
                val adapter = WidgetChatList.`access$getAdapter$p`(widgetChatList)

                param.args[1] = (param.args[1] as List<Long>).filter { msgId ->
                    val msg = StoreStream.getMessages().getMessage(param.args[0] as Long, msgId)
                    if (msg == null || shouldIgnoreMessage(msg.author.id, msg.author.e())) {
                        true
                    } else {
                        deletedMessages.append(msgId, System.currentTimeMillis())

                        val idx = adapter.internalData.indexOfFirst { it is MessageEntry && it.message.id == msgId }
                        if (idx != -1) {
                            adapter.notifyItemChanged(idx)
                        }

                        false
                    }
                }
            }
        })

        patcher.patch(StoreMessages::class.java, "handleMessageUpdate", arrayOf(com.discord.api.message.Message::class.java), object : XC_MethodHook() {
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                if (!settings.getBool("logEditedMessages", true)) {
                    return
                }

                val msg = param.args[0] as com.discord.api.message.Message
                if (shouldIgnoreMessage(msg.e().id, msg.e().e())) {
                    return
                }

                val msgId = msg.o()
                val oldContent = StoreStream.getMessages().getMessage(msg.g(), msgId)?.content ?: return
                val newContent = Message(msg).content

                if (oldContent != newContent) {
                    editedMessages[msgId] ?: ArrayList<Pair<String, Long>>().also { editedMessages.append(msgId, it) } += Pair(oldContent, System.currentTimeMillis())
                }
            }
        })

        val getMessageRenderContext = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getMessageRenderContext", Context::class.java, MessageEntry::class.java, Function1::class.java).apply { isAccessible = true }
        val getMessagePreprocessor = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getMessagePreprocessor", Long::class.java, Message::class.java, StoreMessageState.State::class.java).apply { isAccessible = true }
        val getSpoilerClickHandler = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getSpoilerClickHandler", Message::class.java).apply { isAccessible = true }

        patcher.patch(WidgetChatListAdapterItemMessage::class.java, "processMessageText", arrayOf(SimpleDraweeSpanTextView::class.java, MessageEntry::class.java), object : XC_MethodHook() {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                val msgEntry = param.args[1] as MessageEntry
                val msg = msgEntry.message
                val deletedAt = deletedMessages[msg.id]
                val edits = editedMessages[msg.id]

                if (deletedAt == null && edits == null) {
                    return
                }

                val textView = param.args[0] as SimpleDraweeSpanTextView
                val context = textView.context
                val builder = DraweeSpanStringBuilder()

                if (edits != null) {
                    val _this = param.thisObject as WidgetChatListAdapterItemMessage
                    val messageRenderContext = getMessageRenderContext.invoke(_this, context, msgEntry, getSpoilerClickHandler.invoke(_this, msg)) as MessageRenderContext
                    val messagePreprocessor = getMessagePreprocessor.invoke(_this, _this.adapter.data.userId, msg, msgEntry.messageState) as MessagePreprocessor

                    for (edit in edits) {
                        val timeStampStr = " (edited: ${ DateUtils.getRelativeDateTimeString(context, edit.second, DateUtils.DAY_IN_MILLIS, DateUtils.DAY_IN_MILLIS * 2L, DateUtils.FORMAT_ABBREV_ALL) })\n"

                        builder.append(DiscordParser.parseChannelMessage(
                            context,
                            "${ edit.first }$timeStampStr",
                            messageRenderContext,
                            messagePreprocessor,
                            if (msg.isWebhook()) DiscordParser.ParserOptions.ALLOW_MASKED_LINKS else DiscordParser.ParserOptions.DEFAULT,
                            false
                        ))

                        builder.setSpan(RelativeSizeSpan(0.75f), builder.length - timeStampStr.length, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    builder.setSpan(ForegroundColorSpan(ColorCompat.getThemedColor(context, R.b.colorTextMuted)), 0, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                val contentStart = builder.length
                builder.append(textView.text)

                if (deletedAt != null) {
                    builder.setSpan(ForegroundColorSpan(0xFFF04747.toInt()), contentStart, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    val timeStampStart = builder.length
                    val timeStamp = DateUtils.getRelativeDateTimeString(context, deletedAt, DateUtils.DAY_IN_MILLIS, DateUtils.DAY_IN_MILLIS * 2L, DateUtils.FORMAT_ABBREV_ALL)

                    builder.append(" (deleted: $timeStamp)")
                    builder.setSpan(RelativeSizeSpan(0.75f), timeStampStart, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(ColorCompat.getThemedColor(context, R.b.colorTextMuted)), timeStampStart, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                textView.setDraweeSpanStringBuilder(builder)
            }
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun shouldIgnoreMessage(userId: Long, isBot: Boolean?): Boolean {
        return settings.getBool("ignoreBots", false) && isBot == true || settings.getBool("ignoreSelf", false) && userId == StoreStream.getUsers().me.id
    }
}