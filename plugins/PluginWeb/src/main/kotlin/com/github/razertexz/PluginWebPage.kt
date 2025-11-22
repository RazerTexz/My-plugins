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

import com.discord.utilities.color.ColorCompat

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
        private class ViewHolder(val card: PluginWebCard) : RecyclerView.ViewHolder(card)
        private class DiffCallback : DiffUtil.ItemCallback<PluginData>() {
            override fun areItemsTheSame(oldItem: PluginData, newItem: PluginData): Boolean = oldItem.name == newItem.name
            override fun areContentsTheSame(oldItem: PluginData, newItem: PluginData): Boolean = true
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(PluginWebCard(parent.context)).apply {
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

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            holder.card.titleView.text = "${item.name} v${item.version} by ${item.authors[0]}"
            holder.card.descriptionView.text = MDUtils.render(item.description)
            holder.card.changelogButton.visibility = if (item.changelog != null) View.VISIBLE else View.GONE

            if (item.name in PluginManager.plugins) {
                holder.card.installButton.visibility = View.GONE
                holder.card.uninstallButton.visibility = View.VISIBLE
            } else {
                holder.card.installButton.visibility = View.VISIBLE
                holder.card.uninstallButton.visibility = View.GONE
            }
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    return FilterResults().apply {
                        values = if (constraint.isNullOrEmpty())
                            originalData
                        else
                            originalData.filter { it.name.contains(constraint, true) || it.description.contains(constraint, true) }
                    }
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
            val data = GsonUtils.gson.d<List<PluginData>>(JsonReader(InputStreamReader(Http.Request("https://plugins.aliucord.com/manifest.json").execute().stream())), object : TypeToken<List<PluginData>>() {}.type)

            Utils.mainThread.post {
                val context = view.context
                val myAdapter = Adapter(data)
                myAdapter.submitList(data)

                addView(TextInput(context, context.getString(R.h.search)).apply {
                    editText.setOnEditorActionListener { v, actionId, event ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            myAdapter.filter.filter(editText.text)
                            true
                        } else {
                            false
                        }
                    }
                })

                addView(RecyclerView(context).apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = myAdapter
                    setHasFixedSize(true)
                })
            }
        }
    }
}
