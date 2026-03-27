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
import kotlin.Function1

@AliucordPlugin(requiresRestart = false)
class SimpleMessageLogger : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        val deletedMessages = LongSparseArray<Long>()
        val editedMessages = LongSparseArray<ArrayList<Pair<String, Long>>>()

        val getMessageRenderContext = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getMessageRenderContext", Context::class.java, MessageEntry::class.java, Function1::class.java).apply { isAccessible = true }
        val getMessagePreprocessor = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getMessagePreprocessor", Long::class.java, Message::class.java, StoreMessageState.State::class.java).apply { isAccessible = true }
        val getSpoilerClickHandler = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getSpoilerClickHandler", Message::class.java).apply { isAccessible = true }

        patcher.patch(StoreMessages::class.java, "handleMessageDelete", arrayOf(Long::class.java, List::class.java), object : XC_MethodHook() {
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                if (!settings.getBool("logDeletedMessages", true)) {
                    return
                }

                param.args[1] = (param.args[1] as List<Long>).filter { msgId ->
                    val msg = (param.thisObject as StoreMessages).getMessage(param.args[0] as Long, msgId)
                    if (msg == null || shouldIgnoreMessage(msg.author.id, msg.author.e())) {
                        true
                    } else {
                        deletedMessages.append(msgId, System.currentTimeMillis())

                        val adapter = WidgetChatList.`access$getAdapter$p`(Utils.widgetChatList!!)
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
                val oldContent = (param.thisObject as StoreMessages).getMessage(msg.g(), msgId)?.content
                if (oldContent != null && oldContent != Message(msg).content) {
                    editedMessages[msgId] ?: ArrayList<Pair<String, Long>>().also { editedMessages.append(msgId, it) } += Pair(oldContent, System.currentTimeMillis())
                }
            }
        })

        patcher.patch(WidgetChatListAdapterItemMessage::class.java, "processMessageText", arrayOf(SimpleDraweeSpanTextView::class.java, MessageEntry::class.java), object : XC_MethodHook() {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                val msgEntry = param.args[1] as MessageEntry
                val msg = msgEntry.message
                val edits = editedMessages[msg.id]
                val deletedAt = deletedMessages[msg.id]

                if (edits == null && deletedAt == null) {
                    return
                }

                val textView = param.args[0] as SimpleDraweeSpanTextView
                val ctx = textView.context
                val mutedColor = ColorCompat.getThemedColor(ctx, R.b.colorTextMuted)
                val builder = DraweeSpanStringBuilder()

                if (edits != null) {
                    val _this = param.thisObject as WidgetChatListAdapterItemMessage
                    val renderCtx = getMessageRenderContext.invoke(_this, ctx, msgEntry, getSpoilerClickHandler.invoke(_this, msg)) as MessageRenderContext
                    val preprocessor = getMessagePreprocessor.invoke(_this, _this.adapter.data.userId, msg, msgEntry.messageState) as MessagePreprocessor

                    for (edit in edits) {
                        builder.append(DiscordParser.parseChannelMessage(
                            ctx,
                            edit.first,
                            renderCtx,
                            preprocessor,
                            if (msg.isWebhook()) DiscordParser.ParserOptions.ALLOW_MASKED_LINKS else DiscordParser.ParserOptions.DEFAULT,
                            false
                        ))

                        val tagStart = builder.length

                        builder.append(" (edited: ${edit.second.toRelativeTimestamp(ctx)})\n")
                        builder.setSpan(RelativeSizeSpan(0.75f), tagStart, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    builder.setSpan(ForegroundColorSpan(mutedColor), 0, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                val contentStart = builder.length
                builder.append(textView.text)

                if (deletedAt != null) {
                    val tagStart = builder.length

                    builder.append(" (deleted: ${deletedAt.toRelativeTimestamp(ctx)})")
                    builder.setSpan(RelativeSizeSpan(0.75f), tagStart, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(mutedColor), tagStart, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(ColorCompat.getThemedColor(ctx, R.b.colorTextDanger)), contentStart, tagStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                textView.setDraweeSpanStringBuilder(builder)
            }
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    fun shouldIgnoreMessage(userId: Long, isBot: Boolean?): Boolean {
        return settings.getBool("ignoreBots", false) && isBot == true || settings.getBool("ignoreSelf", false) && userId == StoreStream.getUsers().me.id
    }

    fun Long.toRelativeTimestamp(context: Context): CharSequence {
        return DateUtils.getRelativeDateTimeString(context, this, DateUtils.DAY_IN_MILLIS, DateUtils.DAY_IN_MILLIS * 2L, DateUtils.FORMAT_ABBREV_ALL)
    }
}