package com.github.razertexz

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.widget.TextView
import android.widget.ImageView
import android.view.ViewGroup
import android.view.View

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

import de.robv.android.xposed.XC_MethodHook

@AliucordPlugin(requiresRestart = false)
class ASS : Plugin() {
    init {
        settingsTab = SettingsTab(ASSSettings::class.java).withArgs(settings)
    }

    override fun start(ctx: Context) {
        val style = ASSLoader.loadStyle(settings.getString("currentStyle", "")) ?: return
        val stack = ArrayDeque<View>()

        patcher.patch(ViewGroup::class.java, "onViewAdded", arrayOf(View::class.java), object : XC_MethodHook(10000) {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                val child = param.args[0] as View
                if (child.id != View.NO_ID) {
                    val rule = style.rules[child.id]
                    if (rule != null) applyRule(child, rule)
                }
            }
        })

        patcher.patch(RecyclerView.Adapter::class.java, "onBindViewHolder", arrayOf(RecyclerView.ViewHolder::class.java, Int::class.java, List::class.java), object : XC_MethodHook(10000) {
            override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                stack.addFirst((param.args[0] as RecyclerView.ViewHolder).itemView)

                while (stack.isNotEmpty()) {
                    val current = stack.removeFirst()
                    if (current.id != View.NO_ID) {
                        val rule = style.rules[current.id]
                        if (rule != null) applyRule(current, rule)
                    }

                    if (current is ViewGroup) {
                        for (i in 0 until current.childCount) {
                            stack.addFirst(current.getChildAt(i))
                        }
                    }
                }
            }
        })
    }

    override fun stop(ctx: Context) = patcher.unpatchAll()

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

            if (rule.drawableTint != null && view.compoundDrawableTintList != rule.drawableTint) {
                view.compoundDrawableTintList = rule.drawableTint
            }
        } else if (view is ImageView && rule.drawableTint != null && view.imageTintList != rule.drawableTint) {
            view.imageTintList = rule.drawableTint
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