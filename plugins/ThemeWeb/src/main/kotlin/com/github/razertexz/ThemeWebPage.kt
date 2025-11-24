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

import com.aliucord.Constants
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.utils.GsonUtils
import com.aliucord.views.TextInput
import com.aliucord.fragments.SettingsPage

import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader

import java.io.File
import java.io.InputStreamReader

class ThemeWebPage() : SettingsPage() {
    private class ThemeData(
        val name: String,
        val version: String,
        val author: String,
        val url: String,
        val repoUrl: String,
        val filename: String
    )

    private class Adapter(private val originalData: List<ThemeData>) : ListAdapter<ThemeData, Adapter.ViewHolder>(DiffCallback()), Filterable {
        private class ViewHolder(val card: ThemeWebCard) : RecyclerView.ViewHolder(card)
        private class DiffCallback : DiffUtil.ItemCallback<ThemeData>() {
            override fun areItemsTheSame(oldItem: ThemeData, newItem: ThemeData): Boolean = oldItem.name == newItem.name
            override fun areContentsTheSame(oldItem: ThemeData, newItem: ThemeData): Boolean = true
        }

        private val themeDir = File(Constants.BASE_PATH, "themes")

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ThemeWebCard(parent.context)).apply {
                card.repoButton.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                        return@setOnClickListener

                    val item = getItem(bindingAdapterPosition)
                    Utils.launchUrl(item.repoUrl)
                }

                card.installButton.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                        return@setOnClickListener

                    Utils.threadPool.execute {
                        if (!themeDir.exists())
                            themeDir.mkdir()

                        val item = getItem(bindingAdapterPosition)
                        Http.simpleDownload(item.url, File(themeDir, item.filename))

                        Utils.mainThread.post {
                            Utils.promptRestart()
                            notifyItemChanged(bindingAdapterPosition)
                        }
                    }
                }

                card.uninstallButton.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                        return@setOnClickListener

                    val item = getItem(bindingAdapterPosition)
                    if (!File(themeDir, item.filename).delete())
                        return@setOnClickListener

                    Utils.promptRestart()
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            holder.card.titleView.text = "${item.name} v${item.version} by ${item.author}"
            
            if (File(themeDir, item.filename).exists()) {
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
                        val query = constraint?.trim()

                        values = if (query.isNullOrEmpty())
                            originalData
                        else
                            originalData.filter { it.name.contains(query, true) || it.author.contains(query, true) }
                    }
                }

                override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                    submitList(results!!.values as List<ThemeData>)
                }
            }
        }
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Theme Web")
        setActionBarSubtitle("A quick way to search for Aliucord themes")
        removeScrollView()

        Utils.threadPool.execute {
            val data = GsonUtils.gson.d<List<ThemeData>>(
                JsonReader(
                    InputStreamReader(
                        Http.Request("https://rautobot.github.io/themes-repo/data.json")
                            .execute()
                            .stream()
                    )
                ),
                object : TypeToken<List<ThemeData>>() {}.type
            )

            Utils.mainThread.post {
                val context = view.context
                val myAdapter = Adapter(data)
                myAdapter.submitList(data)

                addView(TextInput(context, "Search by Name or Author").apply {
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
