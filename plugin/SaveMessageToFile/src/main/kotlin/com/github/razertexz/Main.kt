package com.github.razertexz

import android.view.View
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView

import com.aliucord.Utils
import com.aliucord.Constants
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.fragments.InputDialog

import com.lytefast.flexinput.R

import com.discord.utilities.color.ColorCompat
import com.discord.widgets.chat.list.actions.WidgetChatListActions

import java.io.File
import java.io.IOException

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    private fun writeToFile(fileName: String, content: String) {
        try {
            val file = File(Constants.BASE_PATH, fileName.takeIf { it.trim().isNotEmpty() } ?: settings.getString("defaultFileName", "untitled"))
            file.writeText(content)

            Utils.showToast("Successfully saved message to $file")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun start(context: Context) {
        val icon = context.getDrawable(R.e.ic_upload_24dp)!!.mutate()
        val viewID = View.generateViewId()
        val fileNameInputDialog = InputDialog()
            .setInputType(1)
            .setTitle("Enter file name:")
            .setDescription("Please input the file name")

        patcher.after<WidgetChatListActions>("configureUI", WidgetChatListActions.Model::class.java) {
            val nestedScrollView = this.getView() as NestedScrollView
            val linearLayout = nestedScrollView.getChildAt(0) as LinearLayout

            if (linearLayout.findViewById<TextView>(viewID) == null) {
                val textView = TextView(linearLayout.getContext(), null, 0, R.i.UiKit_Settings_Item_Icon)
                val messageContent = "${ (it.args[0] as WidgetChatListActions.Model).getMessageContent() }"

                icon.setTint(ColorCompat.getThemedColor(textView, R.b.colorInteractiveNormal))

                textView.setId(viewID)
                textView.setText("Save Message as File")
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
                textView.setOnClickListener {
                    if (settings.getBool("skipFileNameDialog", false)) {
                        writeToFile("", messageContent)
                    } else {
                        fileNameInputDialog.setOnOkListener {
                            writeToFile(fileNameInputDialog.getInput(), messageContent)
                            fileNameInputDialog.dismiss()
                        }
                        fileNameInputDialog.show(this.parentFragmentManager, "fileName")
                    }

                    this.dismiss()
                }

                linearLayout.addView(textView, linearLayout.getChildCount())
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
