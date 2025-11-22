package com.github.razertexz

import com.facebook.drawee.span.DraweeSpanStringBuilder
import android.content.Context
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.LongSparseArray

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.Utils

import com.discord.utilities.textprocessing.DiscordParser
import com.discord.utilities.textprocessing.MessageRenderContext
import com.discord.utilities.textprocessing.MessagePreprocessor
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.time.TimeUtils
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.stores.StoreStream
import com.discord.stores.StoreMessages
import com.discord.stores.StoreMessageState
import com.discord.models.message.Message
import com.discord.api.message.Message as APIMessage

import com.lytefast.flexinput.R

import java.lang.System
import kotlin.jvm.functions.Function1

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    private class MessageRecord(
        var deletedTimestamp: Long = 0L,
        var edits: ArrayList<Edit>? = null
    ) {
        class Edit(val content: String, val timestamp: Long)
    }

    override fun start(ctx: Context) {
        val messageRecords = LongSparseArray<MessageRecord>()

        patcher.before<StoreMessages>("handleMessageDelete", Long::class.java, List::class.java) {
            if (!settings.getBool("logDeletedMessages", true))
                return@before

            it.args[1] = (it.args[1] as List<Long>).filter { messageId ->
                val message = StoreStream.getMessages().getMessage(it.args[0] as Long, messageId)
                if (message == null || settings.getBool("ignoreBots", false) && message.author.e() == true || settings.getBool("ignoreSelf", false) && message.author.id == StoreStream.getUsers().me.id) {
                    true
                } else {
                    messageRecords.getOrPut(messageId) { MessageRecord() }.deletedTimestamp = System.currentTimeMillis()

                    val adapter = WidgetChatList.`access$getAdapter$p`(Utils.widgetChatList!!)
                    val idx = adapter.internalData.indexOfFirst { it is MessageEntry && it.message.id == messageId }
                    if (idx != -1) adapter.notifyItemChanged(idx)

                    false
                }
            }
        }

        patcher.before<StoreMessages>("handleMessageUpdate", APIMessage::class.java) {
            if (!settings.getBool("logEditedMessages", true))
                return@before

            val newMessage = it.args[0] as APIMessage
            if (settings.getBool("ignoreBots", false) && newMessage.e().e() == true || settings.getBool("ignoreSelf", false) && newMessage.e().id == StoreStream.getUsers().me.id)
                return@before

            val messageId = newMessage.o()
            val channelId = newMessage.g()

            val oldContent = StoreStream.getMessages().getMessage(channelId, messageId)?.content ?: return@before
            val newContent = newMessage.i()
            if (oldContent != newContent) {
                val record = messageRecords.getOrPut(messageId) { MessageRecord() }
                if (record.edits == null) record.edits = ArrayList()

                record.edits!! += MessageRecord.Edit(oldContent, System.currentTimeMillis())
            }
        }

        val mDraweeStringBuilder = SimpleDraweeSpanTextView::class.java.getDeclaredField("mDraweeStringBuilder").apply { isAccessible = true }
        val getMessageRenderContext = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getMessageRenderContext", Context::class.java, MessageEntry::class.java, Function1::class.java).apply { isAccessible = true }
        val getMessagePreprocessor = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getMessagePreprocessor", Long::class.java, Message::class.java, StoreMessageState.State::class.java).apply { isAccessible = true }
        val getSpoilerClickHandler = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getSpoilerClickHandler", Message::class.java).apply { isAccessible = true }

        patcher.after<WidgetChatListAdapterItemMessage>("processMessageText", SimpleDraweeSpanTextView::class.java, MessageEntry::class.java) {
            val messageEntry = it.args[1] as MessageEntry

            val message = messageEntry.message
            val record = messageRecords[message.id] ?: return@after

            val textView = it.args[0] as SimpleDraweeSpanTextView
            val context = textView.context
            val builder = mDraweeStringBuilder[textView] as DraweeSpanStringBuilder

            if (record.deletedTimestamp > 0L) {
                val start = builder.length
                val timeStr = " (deleted: ${ TimeUtils.toReadableTimeString(context, record.deletedTimestamp, ClockFactory.get()) })"

                builder.setSpan(ForegroundColorSpan(0xFFF04747.toInt()), 0, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.append(timeStr)
                builder.setSpan(RelativeSizeSpan(0.75f), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.setSpan(ForegroundColorSpan(ColorCompat.getThemedColor(context, R.b.colorTextMuted)), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            if (record.edits != null) {
                val messageRenderContext = getMessageRenderContext.invoke(
                    this,
                    context,
                    messageEntry,
                    getSpoilerClickHandler.invoke(this, message)
                ) as MessageRenderContext

                val messagePreprocessor = getMessagePreprocessor.invoke(
                    this,
                    adapter.data.userId,
                    message,
                    messageEntry.messageState
                ) as MessagePreprocessor

                val editBuilder = DraweeSpanStringBuilder()
                for (edit in record.edits) {
                    val timeStr = " (edited: ${ TimeUtils.toReadableTimeString(context, edit.timestamp, ClockFactory.get()) })\n"
                    val parsed = DiscordParser.parseChannelMessage(
                        context,
                        "${ edit.content }$timeStr",
                        messageRenderContext,
                        messagePreprocessor,
                        if (message.isWebhook()) DiscordParser.ParserOptions.ALLOW_MASKED_LINKS else DiscordParser.ParserOptions.DEFAULT,
                        false
                    )
                    parsed.setSpan(RelativeSizeSpan(0.75f), parsed.length - timeStr.length, parsed.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    editBuilder.append(parsed)
                }

                editBuilder.setSpan(ForegroundColorSpan(ColorCompat.getThemedColor(context, R.b.colorTextMuted)), 0, editBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                editBuilder.append(builder)

                textView.setDraweeSpanStringBuilder(editBuilder)
            } else {
                textView.setDraweeSpanStringBuilder(builder)
            }
        }
    }

    private inline fun <T> LongSparseArray<T>.getOrPut(key: Long, defaultValue: () -> T): T {
        var value = get(key)
        if (value == null) {
            value = defaultValue()
            this.put(key, value)
        }

        return value
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}
