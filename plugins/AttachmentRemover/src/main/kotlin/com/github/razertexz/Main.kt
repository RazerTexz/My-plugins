package com.github.razertexz

import androidx.core.widget.NestedScrollView
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.Utils
import com.aliucord.Http

import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemAttachment$configureUI$3`
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.api.message.attachment.MessageAttachment
import com.discord.stores.StoreStream

import com.lytefast.flexinput.R

import java.util.Collections

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    override fun start(ctx: Context) {
        var selectedAttachment: MessageAttachment? = null
        val storeUser = StoreStream.getUsers()

        val viewId = View.generateViewId()
        val icon = ctx.getDrawable(R.e.ic_attachment_white_24dp)!!.mutate()
        Utils.tintToTheme(icon)

        patcher.after<`WidgetChatListAdapterItemAttachment$configureUI$3`>("invoke", View::class.java) {
            if (`$data`.message.author.id == storeUser.me.id) {
                selectedAttachment = `$data`.attachment
            }
        }

        patcher.after<WidgetChatListActions>("configureUI", WidgetChatListActions.Model::class.java) { param ->
            val attachmentToRemove = selectedAttachment ?: return@after
            selectedAttachment = null

            val linearLayout = (getView()!! as NestedScrollView).getChildAt(0) as LinearLayout
            if (linearLayout.findViewById<TextView>(viewId) == null) {
                linearLayout.addView(TextView(linearLayout.context, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                    id = viewId
                    text = "Remove Attachment"
                    setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)

                    setOnClickListener {
                        Utils.threadPool.execute {
                            val msg = (param.args[0] as WidgetChatListActions.Model).message
                            Http.Request.newDiscordRequest("/channels/${msg.channelId}/messages/${msg.id}", "PATCH")
                                .executeWithJson(Collections.singletonMap("attachments", msg.attachments - attachmentToRemove))
                        }

                        this@after.dismiss()
                    }
                }, linearLayout.childCount)
            }
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}
