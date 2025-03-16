package com.github.razertexz;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.aliucord.Constants;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.patcher.*;
import com.aliucord.fragments.InputDialog;

import com.lytefast.flexinput.R;

import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;

import java.io.File;
import java.io.IOException;

@AliucordPlugin(requiresRestart = false)
class Main : Plugin() {
    @JvmField
    val settings: SettingsAPI = SettingsAPI("SaveMessageAsFile")

    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        val icon: Drawable? = context.getDrawable(R.e.ic_upload_24dp)?.mutate()
        val viewID: Int = View.generateViewId()
        val fileNameInputDialog: InputDialog = InputDialog()
            .setInputType(1)
            .setTitle("Enter file name:")
            .setDescription("Please input the file name")

        patcher.after<WidgetChatListActions>("configureUI", WidgetChatListActions.Model::class.java) {
            val thisObject: WidgetChatListActions = it.thisObject as WidgetChatListActions
            val nestedScrollView: NestedScrollView = thisObject.getView() as NestedScrollView
            val linearLayout: LinearLayout = nestedScrollView.getChildAt(0) as LinearLayout

            if (linearLayout.findViewById<TextView>(viewID) == null) {
                val textView: TextView = TextView(linearLayout.getContext(), null, 0, R.i.UiKit_Settings_Item_Icon)
                val messageContent: String = "${ (it.args[0] as WidgetChatListActions.Model).getMessageContent() }"

                icon?.setTint(ColorCompat.getThemedColor(textView, R.b.colorInteractiveNormal))

                textView.setId(viewID)
                textView.setText("Save Message as File")
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
                textView.setOnClickListener {
                    fileNameInputDialog.setOnOkListener {
                        val input: String = fileNameInputDialog.getInput()
                        try {
                            val file: File = File(Constants.BASE_PATH, if (input.isNotEmpty()) input else settings.getString("defaultFileName", "untitled"))
                            file.writeText(messageContent)

                            Utils.showToast("Successfully saved message to $file")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        fileNameInputDialog.dismiss()
                    }
                    fileNameInputDialog.show(Utils.appActivity.getSupportFragmentManager(), "fileName")

                    thisObject.dismiss()
                }

                linearLayout.addView(textView, linearLayout.getChildCount())
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
