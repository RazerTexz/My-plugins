package com.github.razertexz

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.widget.TextView
import android.widget.ImageView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.util.SparseArray

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

import org.xmlpull.v1.XmlPullParser

import de.robv.android.xposed.XC_MethodHook

@AliucordPlugin(requiresRestart = false)
class ASS : Plugin() {
    private val stack = ArrayDeque<View>()

    init {
        settingsTab = SettingsTab(ASSSettings::class.java).withArgs(settings)
    }

    override fun start(ctx: Context) {
        val currentStyle = settings.getString("currentStyle", "")
        if (currentStyle.isEmpty()) return

        val rules = ASSLoader.loadStyle(currentStyle)?.rules ?: return
        patcher.patch(LayoutInflater::class.java, "inflate", arrayOf(XmlPullParser::class.java, ViewGroup::class.java, Boolean::class.java), object : XC_MethodHook(10000) {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                traverse(param.result as View, rules)
            }
        })

        patcher.patch(RecyclerView.Adapter::class.java, "onBindViewHolder", arrayOf(RecyclerView.ViewHolder::class.java, Int::class.java, List::class.java), object : XC_MethodHook(10000) {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                traverse((param.args[0] as RecyclerView.ViewHolder).itemView, rules)
            }
        })
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()

    private fun traverse(view: View, rules: SparseArray<Rule>) {
        stack.addFirst(view)

        while (stack.isNotEmpty()) {
            val current = stack.removeFirst()
            if (current.id != View.NO_ID) {
                val rule = rules[current.id]
                if (rule != null) applyRule(current, rule)
            }

            if (current is ViewGroup) {
                for (i in 0 until current.childCount) {
                    stack.addFirst(current.getChildAt(i))
                }
            }
        }
    }

    private fun applyRule(view: View, rule: Rule) {
        val lp = view.layoutParams
        var lpChanged = false

        if (view is TextView) {
            if (rule.textSize != null) {
                view.textSize = rule.textSize!!
            }

            if (rule.textColor != null && view.currentTextColor != rule.textColor) {
                view.setTextColor(rule.textColor!!)
            }

            if (rule.typeface != null && view.typeface != rule.typeface) {
                view.typeface = rule.typeface
            }

            if (rule.compoundDrawableTint != null && view.compoundDrawableTintList != rule.compoundDrawableTint) {
                view.compoundDrawableTintList = rule.compoundDrawableTint
            }
        } else if (view is ImageView) {
            if (rule.drawableState != null && view.drawable?.constantState != rule.drawableState) {
                view.setImageDrawable(rule.drawableState!!.newDrawable().mutate())
            }

            if (rule.drawableTint != null) {
                view.setColorFilter(rule.drawableTint!!)
            }
        }

        if (rule.visibility != null && view.visibility != rule.visibility) {
            view.visibility = rule.visibility!!
        }

        if (rule.bgState != null && view.background?.constantState != rule.bgState) {
            view.setBackground(rule.bgState!!.newDrawable(view.resources).mutate())
        }

        if (rule.bgTint != null && view.backgroundTintList != rule.bgTint) {
            view.backgroundTintList = rule.bgTint
        }

        if (rule.width != null && lp.width != rule.width) {
            lp.width = rule.width!!
            lpChanged = true
        }

        if (rule.height != null && lp.height != rule.height) {
            lp.height = rule.height!!
            lpChanged = true
        }

        if (lp is ViewGroup.MarginLayoutParams) {
            if (rule.leftMargin != null && lp.leftMargin != rule.leftMargin) {
                lp.leftMargin = rule.leftMargin!!
                lpChanged = true
            }

            if (rule.topMargin != null && lp.topMargin != rule.topMargin) {
                lp.topMargin = rule.topMargin!!
                lpChanged = true
            }

            if (rule.rightMargin != null && lp.rightMargin != rule.rightMargin) {
                lp.rightMargin = rule.rightMargin!!
                lpChanged = true
            }

            if (rule.bottomMargin != null && lp.bottomMargin != rule.bottomMargin) {
                lp.bottomMargin = rule.bottomMargin!!
                lpChanged = true
            }
        }

        if (lpChanged) {
            view.layoutParams = lp
        }

        if (rule.paddingLeft != null || rule.paddingTop != null || rule.paddingRight != null || rule.paddingBottom != null) {
            view.setPadding(
                rule.paddingLeft ?: view.paddingLeft,
                rule.paddingTop ?: view.paddingTop,
                rule.paddingRight ?: view.paddingRight,
                rule.paddingBottom ?: view.paddingBottom
            )
        }

        for ((paths, value) in rule.customProperties.entries) {
            ReflectUtils.setValue(view, paths, value)
        }
    }
}