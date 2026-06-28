package com.github.razertexz

import android.content.Context
import android.view.Menu

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.Constants
import com.aliucord.Utils

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

        patcher.patch(WidgetHomeHeaderManager::class.java.getDeclaredMethod("configure", WidgetHome::class.java, WidgetHomeModel::class.java, WidgetHomeBinding::class.java), object : XC_MethodHook() {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                (param.args[0] as WidgetHome).toolbar.menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Yoink Messages").setOnMenuItemClickListener {
                    val channel = (param.args[1] as WidgetHomeModel).channel
                    val msgs = holder.getMessagesForChannel(channel.k())!!

                    val filePath = "${Constants.BASE_PATH}/${StoreStream.getGuilds().getGuild(channel.i())?.name ?: "Direct Messages"} - ${ChannelUtils.c(channel)} - ${System.currentTimeMillis()}.txt"
                    File(filePath).writeText(buildString {
                        for (msg in msgs.values) {
                            if (settings.getBool("includeTimestamps", true)) {
                                append(sdf.format(msg.timestamp.g()))
                            }

                            if (settings.getBool("includeMessageIds", false)) {
                                append('[')
                                append(msg.id)
                                append("] ")
                            }

                            if (settings.getBool("includeReplyIds", false) && msg.messageReference != null) {
                                append("[↩️ ")
                                append(msg.messageReference.c())
                                append("] ")
                            }

                            append(msg.author.username)
                            append('\n')

                            if (msg.content.isNotEmpty()) {
                                append(msg.content)
                                append('\n')
                            }

                            for (attachment in msg.attachments) {
                                append("📎 ")
                                append(attachment.f())
                                append('\n')
                            }

                            append('\n')
                        }
                    })

                    Utils.showToast("${msgs.size} messages yoinked to $filePath")
                    true
                }
            }
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}