package com.github.razertexz

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.text.Spannable
import android.widget.ImageView
import android.view.View

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils

import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.utilities.spans.BlockBackgroundSpan

import com.lytefast.flexinput.R

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    override fun start(ctx: Context) {
        val copyBtnSize = DimenUtils.defaultPadding
        val copyBtnMargin = DimenUtils.defaultPadding / 4

        val copyIcon = ctx.getDrawable(R.e.ic_copy_24dp)!!.mutate()
        Utils.tintToTheme(copyIcon)

        patcher.after<WidgetChatListAdapterItemMessage>("processMessageText", SimpleDraweeSpanTextView::class.java, MessageEntry::class.java) {
            val textView = it.args[0] as SimpleDraweeSpanTextView

            val root = itemView as ConstraintLayout
            val buttons = root.tag as? ArrayList<ImageView> ?: ArrayList<ImageView>().also { root.tag = it }
            for (btn in buttons) btn.visibility = View.GONE

            textView.post {
                val layout = textView.layout
                val spannable = textView.text as Spannable

                spannable.getSpans(0, spannable.length, BlockBackgroundSpan::class.java).forEachIndexed { i, span ->
                    val btn = buttons.getOrNull(i) ?: ImageView(root.context).apply {
                        setImageDrawable(copyIcon)

                        root.addView(this, ConstraintLayout.LayoutParams(copyBtnSize, copyBtnSize).apply {
                            topToTop = textView.id
                            endToEnd = textView.id
                            rightMargin = copyBtnMargin
                        })

                        buttons += this
                    }

                    btn.apply {
                        val start = spannable.getSpanStart(span)
                        val end = spannable.getSpanEnd(span)

                        translationY = (layout.getLineTop(layout.getLineForOffset(start)) + copyBtnMargin).toFloat()
                        visibility = View.VISIBLE

                        setOnClickListener {
                            Utils.setClipboard("", spannable.subSequence(start, end).trimEnd())
                            Utils.showToast("Copied to clipboard!")
                        }
                    }
                }
            }
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}