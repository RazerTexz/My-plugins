package com.github.razertexz

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.view.View
import android.text.Spannable
import android.widget.ImageView

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils.dpToPx
import com.aliucord.Utils

import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.utilities.spans.BlockBackgroundSpan
import com.discord.utilities.color.ColorCompat

import com.lytefast.flexinput.R

@AliucordPlugin(requiresRestart = true)
class Main : Plugin() {
    override fun start(ctx: Context) {
        val copyButtonSize = dpToPx(16.0f)
        val copyButtonMargin = dpToPx(4.0f)
        val copyIcon = ctx.getDrawable(R.e.ic_copy_24dp)!!.mutate()

        patcher.after<WidgetChatListAdapterItemMessage>("processMessageText", SimpleDraweeSpanTextView::class.java, MessageEntry::class.java) {
            val textView = it.args[0] as SimpleDraweeSpanTextView
            val root = itemView as ConstraintLayout

            val buttonsPool = root.tag as? ArrayList<ImageView> ?: ArrayList<ImageView>().also {
                root.tag = it
            }
            buttonsPool.forEach { it.visibility = View.GONE }

            textView.post {
                val spannable = textView.text as Spannable

                spannable.getSpans(0, spannable.length, BlockBackgroundSpan::class.java).forEachIndexed { idx, codeBlockSpan ->
                    val copyButton = if (idx < buttonsPool.size) {
                        buttonsPool[idx]
                    } else {
                        ImageView(root.context).apply {
                            copyIcon.setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal))
                            setImageDrawable(copyIcon)

                            buttonsPool.add(this)
                            root.addView(this, ConstraintLayout.LayoutParams(copyButtonSize, copyButtonSize).apply {
                                topToTop = textView.id
                                endToEnd = textView.id
                                rightMargin = copyButtonMargin
                            })
                        }
                    }

                    copyButton.apply {
                        val layout = textView.layout
                        val start = spannable.getSpanStart(codeBlockSpan)
                        val line = layout.getLineForOffset(start)

                        translationY = (copyButtonMargin + layout.getLineTop(line)).toFloat()
                        visibility = View.VISIBLE

                        setOnClickListener {
                            Utils.setClipboard("", spannable.subSequence(start, spannable.getSpanEnd(codeBlockSpan)).trim())
                            Utils.showToast("Copied to clipboard!")
                        }
                    }
                }
            }
        }
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()
}
