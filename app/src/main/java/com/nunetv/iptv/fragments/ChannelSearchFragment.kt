package com.nunetv.iptv.fragments

import android.os.Bundle
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.Presenter
import coil.ImageLoader
import com.nunetv.iptv.R
import com.nunetv.iptv.adapters.ChannelPresenter
import com.nunetv.iptv.model.Channel

class ChannelSearchFragment : SearchSupportFragment(), SearchResultProvider {

    private lateinit var resultsAdapter: ArrayObjectAdapter
    private lateinit var channelPresenter: Presenter
    private var channels: List<Channel> = emptyList()
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(getString(R.string.search_hint))
        brandColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val imageLoader = ImageLoader.Builder(requireContext()).build()
        channelPresenter = ChannelPresenter(imageLoader)
        resultsAdapter = ArrayObjectAdapter(ListRowPresenter())
        setSearchResultProvider(this)
        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Channel) {
                requireActivity().finish()
                val intent = Intent(requireContext(), com.nunetv.iptv.activities.PlayerActivity::class.java).apply {
                    putExtra(com.nunetv.iptv.activities.PlayerActivity.EXTRA_CHANNEL_ID, item.id)
                    putExtra(com.nunetv.iptv.activities.PlayerActivity.EXTRA_CHANNEL_NAME, item.name)
                    putExtra(com.nunetv.iptv.activities.PlayerActivity.EXTRA_CHANNEL_URL, item.url)
                }
                startActivity(intent)
            }
        }
    }

    override fun getResultsAdapter(): ArrayObjectAdapter = resultsAdapter

    override fun onQueryTextChange(newQuery: String?): Boolean {
        currentQuery = newQuery.orEmpty()
        updateResults(currentQuery)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        currentQuery = query.orEmpty()
        updateResults(currentQuery)
        return true
    }

    private fun updateResults(query: String?) {
        val filtered = if (query.isNullOrBlank()) emptyList() else channels.filter {
            it.name.contains(query, ignoreCase = true) || it.group.contains(query, ignoreCase = true)
        }
        val rowAdapter = ArrayObjectAdapter(channelPresenter)
        filtered.forEach { rowAdapter.add(it) }
        resultsAdapter.clear()
        if (filtered.isNotEmpty()) {
            resultsAdapter.add(ListRow(HeaderItem(0, getString(R.string.search_hint)), rowAdapter))
        }
    }

    fun setChannels(channels: List<Channel>) {
        this.channels = channels
        updateResults(currentQuery)
    }
}
