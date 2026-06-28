package com.github.razertexz

import android.content.Context
import android.view.Menu

import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

import com.discord.api.channel.ChannelUtils
import com.discord.databinding.WidgetHomeBinding
import com.discord.stores.StoreMessages
import com.discord.stores.StoreMessagesHolder
import com.discord.stores.StoreStream
import com.discord.widgets.home.WidgetHome
import com.discord.widgets.home.WidgetHomeHeaderManager
import com.discord.widgets.home.WidgetHomeModel

import de.robv.android.xposed.XC_MethodHook

import java.io.File
import java.text.SimpleDateFormat

@AliucordPlugin(requiresRestart = false)
class Yoink : Plugin() {
    override fun start(context: Context) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm z '-' ")
        val storeMessagesHolder = with(StoreMessages::class.java.getDeclaredField("holder")) {
            isAccessible = true
            get(StoreStream.getMessages()) as StoreMessagesHolder
        }

        patcher.patch(WidgetHomeHeaderManager::class.java.getDeclaredMethod("configure", WidgetHome::class.java, WidgetHomeModel::class.java, WidgetHomeBinding::class.java), object : XC_MethodHook() {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                (param.args[0] as WidgetHome).toolbar.menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Yoink Messages").setOnMenuItemClickListener {
                    val channel = (param.args[1] as WidgetHomeModel).channel
                    val messages = storeMessagesHolder.getMessagesForChannel(channel.k())!!
                    val filePath = "${Constants.BASE_PATH}/${StoreStream.getGuilds().getGuild(channel.i())?.name ?: "Direct Messages"} - ${ChannelUtils.c(channel)} - ${System.currentTimeMillis()}.txt"

                    File(filePath).writeText(buildString {
                        for (message in messages.values) {
                            append(sdf.format(message.timestamp.g()))
                            append(message.author.username)
                            append('\n')

                            if (message.content.isNotEmpty()) {
                                append(message.content)
                                append('\n')
                            }

                            for (attachment in message.attachments) {
                                append("📎 ")
                                append(attachment.f())
                                append('\n')
                            }

                            append('\n')
                        }
                    })

                    Utils.showToast("${messages.size} messages yoinked to $filePath")
                    true
                }
            }
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}