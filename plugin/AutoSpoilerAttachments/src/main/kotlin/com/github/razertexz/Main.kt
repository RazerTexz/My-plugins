package com.github.razertexz

import android.content.Context
import android.net.Uri

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*

import com.lytefast.flexinput.model.Attachment

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    override fun start(ctx: Context) {
        patcher.before<Attachment<*>>(Long::class.java, Uri::class.java, String::class.java, Object::class.java, Boolean::class.java) {
            it.args[4] = true
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}
