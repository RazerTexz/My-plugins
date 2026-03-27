package com.github.razertexz

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.View

import com.aliucord.fragments.SettingsPage
import com.aliucord.api.SettingsAPI
import com.aliucord.Constants
import com.aliucord.Utils

import java.io.File

internal class ASSSettings(private val settings: SettingsAPI) : SettingsPage() {
    private class StyleItem(
        @JvmField val fileName: String,
        @JvmField val name: String,
        @JvmField val version: String,
        @JvmField val author: String
    )

    private inner class Adapter(private val data: ArrayList<StyleItem>) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        private inner class ViewHolder(val card: ASSSettingsCard) : RecyclerView.ViewHolder(card) {
            init {
                card.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos == RecyclerView.NO_POSITION)
                        return@setOnClickListener

                    val item = data[pos]
                    if (item.fileName == settings.getString("currentStyle", "")) {
                        Utils.showToast("This style is already active.")
                    } else {
                        settings.setString("currentStyle", item.fileName)
                        Utils.promptRestart("Style '${item.name}' selected. Restart now to apply?")
                    }
                }

                card.removeButton.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos == RecyclerView.NO_POSITION)
                        return@setOnClickListener

                    val fileName = data[pos].fileName
                    if (!File("${Constants.BASE_PATH}/styles/$fileName").delete())
                        return@setOnClickListener

                    if (settings.getString("currentStyle", "") == fileName)
                        settings.setString("currentStyle", "")

                    data.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ASSSettingsCard(parent.context))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.card.titleView.text = "${item.name} v${item.version} by ${item.author}"
        }

        override fun getItemCount(): Int = data.size
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("A.S.S")
        setActionBarSubtitle("Aliucord Style Sheets")
        removeScrollView()

        val stylesDir = File(Constants.BASE_PATH, "styles")
        if (!stylesDir.exists()) stylesDir.mkdir()

        val items = ArrayList<StyleItem>()
        val files = stylesDir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    val fileName = file.name
                    if (!fileName.endsWith(".json")) continue

                    val manifest = ASSLoader.loadStyle(fileName)?.manifest ?: continue
                    items += StyleItem(fileName, manifest.name, manifest.version, manifest.author)
                }
            }
        }

        if (items.isEmpty()) {
            Utils.showToast("No styles are currently installed", true)
            return
        }

        addView(RecyclerView(view.context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = Adapter(items)
            setHasFixedSize(true)
        })
    }
}