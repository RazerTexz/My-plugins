package com.github.razertexz

import android.content.Context
import android.widget.TextView

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.ReflectUtils

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.views.UsernameView

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    override fun start(context: Context) {
        patcher.patch(
            WidgetChannelMembersListItemUserBinding::class.java.getDeclaredConstructors()[0],
            Hook {
                val userNameView = it.args[5] as UsernameView
                userNameView.j.c.apply {
                    isSingleLine = false
                    setHorizontallyScrolling(false)
                }
            }
        )
        
        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.java,
            ChatListEntry::class.java
        ) {
            (ReflectUtils.getField(this, "itemName") as TextView).apply {
                isSingleLine = false
                setHorizontallyScrolling(false)
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
