package com.github.razertexz

import android.content.Context

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.Constants
import com.aliucord.Utils

import com.discord.stores.StoreMessages
import com.discord.stores.StoreMessagesHolder
import com.discord.stores.StoreStream
import com.discord.widgets.home.WidgetHome
import com.discord.widgets.home.WidgetHomeModel

import de.robv.android.xposed.XC_MethodHook

import java.io.File
import java.text.SimpleDateFormat

@AliucordPlugin
class Yoink : Plugin() {
    init {
        settingsTab = SettingsTab(YoinkSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        val sdf = SimpleDateFormat("'['yyyy-MM-dd HH:mm z']' ")
        val holder = StoreMessages::class.java.getDeclaredField("holder").run {
            isAccessible = true
            get(StoreStream.getMessages()) as StoreMessagesHolder
        }

        patcher.patch(WidgetHome::class.java.getDeclaredMethod("configureUI", WidgetHomeModel::class.java), object : XC_MethodHook() {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                val model = param.args[0] as WidgetHomeModel
                val layout = (param.thisObject as WidgetHome).actionBarTitleLayout

                layout.setOnLongClickListener {
                    val msgs = holder.getMessagesForChannel(model.channelId) ?: return@setOnLongClickListener true
                    val path = "${Constants.BASE_PATH}/${StoreStream.getGuilds().getGuild(model.channel.i())?.name ?: "Direct Messages"} - ${layout.k.d.text} - ${System.currentTimeMillis()}.txt"

                    File(path).writeText(buildString {
                        for (msg in msgs.values) {
                            if (settings.getBool("includeTimestamps", true)) {
                                append(sdf.format(msg.timestamp.g()))
                            }

                            if (settings.getBool("includeMessageIds", false)) {
                                append('[').append(msg.id).append("] ")
                            }

                            if (settings.getBool("includeReplyIds", false) && msg.messageReference != null) {
                                append("[↩ ").append(msg.messageReference.c()).append("] ")
                            }

                            append(msg.author.username).append('\n')

                            if (msg.content.isNotEmpty()) {
                                append(msg.content).append('\n')
                            }

                            for (embed in msg.embeds) {
                                append("📄 ").append(embed.j()?.replace("\r\n", "⏎")?.replace('\n', '⏎') ?: "No Title").append("\n   ").append(embed.c()?.replace("\r\n", "⏎")?.replace('\n', '⏎') ?: "No Description").append('\n')

                                if (embed.d() != null) {
                                    for (field in embed.d()) {
                                        append("   • ").append(field.a().replace("\r\n", "⏎")?.replace('\n', '⏎')).append(": ").append(field.b().replace("\r\n", "⏎")?.replace('\n', '⏎')).append('\n')
                                    }
                                }
                            }

                            if (settings.getBool("includeAttachmentUrls", true)) {
                                for (attachment in msg.attachments) {
                                    append("📎 ").append(attachment.f()).append('\n')
                                }
                            }

                            append('\n')
                        }
                    })

                    Utils.showToast("${msgs.size} messages yoinked to $path")
                    true
                }
            }
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}