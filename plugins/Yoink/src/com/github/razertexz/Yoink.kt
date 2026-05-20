package com.github.razertexz

import android.content.Context
import android.view.Menu

import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

import com.discord.databinding.WidgetHomeBinding
import com.discord.stores.StoreMessages
import com.discord.stores.StoreMessagesHolder
import com.discord.stores.StoreStream
import com.discord.widgets.home.WidgetHome
import com.discord.widgets.home.WidgetHomeHeaderManager
import com.discord.widgets.home.WidgetHomeModel

import de.robv.android.xposed.XC_MethodHook

import java.io.BufferedWriter
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.Executors

@AliucordPlugin(requiresRestart = false)
class Yoink : Plugin() {
    override fun start(context: Context) {
        val executor = Executors.newSingleThreadExecutor()
        val utcFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val storeMessagesHolder = with(StoreMessages::class.java.getDeclaredField("holder")) {
            isAccessible = true
            get(StoreStream.getMessages()) as StoreMessagesHolder
        }

        patcher.patch(WidgetHomeHeaderManager::class.java.getDeclaredMethod("configure", WidgetHome::class.java, WidgetHomeModel::class.java, WidgetHomeBinding::class.java), object : XC_MethodHook() {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                (param.args[0] as WidgetHome).toolbar.menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Yoink Messages").setOnMenuItemClickListener {
                    val model = param.args[1] as WidgetHomeModel
                    val msgs = storeMessagesHolder.getMessagesForChannel(model.channelId)!!

                    executor.execute {
                        BufferedWriter(FileWriter("${Constants.BASE_PATH}/${model.channel.i()}_${model.channelId}_${System.currentTimeMillis()}.txt")).use {
                            it.write("UTCDateTime\u001FMessageID\u001FReferencedMessageID\u001FAuthorID\u001FAuthor\u001FContent\u001FAttachments\n")
    
                            for (msg in msgs.values) {
                                it.write(buildString {
                                    append(utcFormatter.format(Date(msg.timestamp.g())))
                                    append("\u001F")
                                    append(msg.id)
                                    append("\u001F")
                                    msg.messageReference?.c()?.let { append(it) }
                                    append("\u001F")
                                    append(msg.author.id)
                                    append("\u001F")
                                    append(StoreStream.getGuilds().getMember(model.channel.i(), msg.author.id)?.nick ?: msg.author.username)
                                    append("\u001F")
                                    append(msg.content.replace("\n", "\\n"))
                                    append("\u001F")
                                    append(msg.attachments.joinToString(";") { it.f() })
                                    append('\n')
                                })
                            }
                        }
    
                        Utils.showToast("Yoinked ${msgs.size} messages")
                    }

                    true
                }
            }
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}