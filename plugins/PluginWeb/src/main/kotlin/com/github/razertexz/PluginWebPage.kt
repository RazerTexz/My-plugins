package com.github.razertexz

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Filterable
import android.widget.Filter

import com.aliucord.PluginManager
import com.aliucord.Constants
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.utils.MDUtils
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.ChangelogUtils
import com.aliucord.views.TextInput
import com.aliucord.fragments.SettingsPage

import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.lytefast.flexinput.R

import java.io.File
import java.io.InputStreamReader

class PluginWebPage() : SettingsPage() {
    private class PluginData(
        val name: String,
        val description: String,
        val version: String,
        val authors: List<String>,
        val url: String,
        val repoUrl: String,
        val changelog: String?
    )

    private class Adapter(private val originalData: List<PluginData>) : ListAdapter<PluginData, Adapter.ViewHolder>(DiffCallback()), Filterable {
        private inner class ViewHolder(val card: PluginWebCard) : RecyclerView.ViewHolder(card) {
            init {
                card.changelogButton.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                        return@setOnClickListener

                    val item = getItem(bindingAdapterPosition)
                    ChangelogUtils.show(it.context, "${item.name} v${item.version}", null, item.changelog!!, ChangelogUtils.FooterAction(R.e.ic_account_github_white_24dp, item.repoUrl))
                }

                card.installButton.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                        return@setOnClickListener

                    val item = getItem(bindingAdapterPosition)
                    val pluginFile = File(Constants.PLUGINS_PATH, "${item.name}.zip")

                    Utils.threadPool.execute {
                        Http.simpleDownload(item.url, pluginFile)

                        Utils.mainThread.post {
                            PluginManager.loadPlugin(Utils.appContext, pluginFile)
                            PluginManager.startPlugin(item.name)

                            if (PluginManager.plugins[item.name]!!.requiresRestart())
                                Utils.promptRestart()

                            notifyItemChanged(bindingAdapterPosition)
                        }
                    }
                }

                card.uninstallButton.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                        return@setOnClickListener

                    val pluginName = getItem(bindingAdapterPosition).name
                    if (!File(Constants.PLUGINS_PATH, "$pluginName.zip").delete())
                        return@setOnClickListener

                    if (PluginManager.plugins[pluginName]!!.requiresRestart())
                        Utils.promptRestart()

                    PluginManager.stopPlugin(pluginName)
                    PluginManager.unloadPlugin(pluginName)

                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }

        private class DiffCallback : DiffUtil.ItemCallback<PluginData>() {
            override fun areItemsTheSame(oldItem: PluginData, newItem: PluginData): Boolean = oldItem.name == newItem.name
            override fun areContentsTheSame(oldItem: PluginData, newItem: PluginData): Boolean = true
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(PluginWebCard(parent.context))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            val card = holder.card

            card.titleView.text = "${item.name} v${item.version} by ${item.authors.joinToString()}"
            card.descriptionView.text = MDUtils.render(item.description)
            card.changelogButton.visibility = if (item.changelog != null) View.VISIBLE else View.GONE

            if (item.name in PluginManager.plugins) {
                card.installButton.visibility = View.GONE
                card.uninstallButton.visibility = View.VISIBLE
            } else {
                card.installButton.visibility = View.VISIBLE
                card.uninstallButton.visibility = View.GONE
            }
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val query = constraint?.trim()
                    val results = FilterResults()

                    results.values = if (query.isNullOrEmpty())
                        originalData
                    else
                        originalData.filter { it.name.contains(query, true) || it.description.contains(query, true) || it.authors.any { it.contains(query, true) } }

                    return results
                }

                override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                    submitList(results!!.values as List<PluginData>)
                }
            }
        }
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Plugin Web")
        setActionBarSubtitle("A quick way to search for Aliucord plugins")
        removeScrollView()

        Utils.threadPool.execute {
            val reader = JsonReader(InputStreamReader(Http.Request("https://plugins.aliucord.com/manifest.json").execute().stream()))
            val data = try {
                GsonUtils.gson.d<List<PluginData>>(reader, object : TypeToken<List<PluginData>>() {}.type).filter { it.name !in PluginManager.plugins }
            } finally {
                reader.close()
            }

            Utils.mainThread.post {
                val myAdapter = Adapter(data)
                myAdapter.submitList(data)

                addView(TextInput(view.context, "Search by Name, Description or Author").apply {
                    editText.setOnEditorActionListener { v, actionId, event ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            myAdapter.filter.filter(editText.text)
                            true
                        } else {
                            false
                        }
                    }
                })

                addView(RecyclerView(view.context).apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = myAdapter
                    setHasFixedSize(true)
                })
            }
        }
    }
}
