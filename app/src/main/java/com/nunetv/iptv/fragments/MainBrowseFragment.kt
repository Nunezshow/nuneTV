package com.nunetv.iptv.fragments

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import com.nunetv.iptv.NuneTvApplication
import com.nunetv.iptv.R
import com.nunetv.iptv.adapters.ChannelPresenter
import com.nunetv.iptv.model.Channel
import com.nunetv.iptv.viewmodels.MainUiState
import com.nunetv.iptv.viewmodels.MainViewModel
import com.nunetv.iptv.viewmodels.MainViewModelFactory
import coil.ImageLoader

class MainBrowseFragment : BrowseSupportFragment() {

    interface Callbacks {
        fun onChannelClicked(channel: Channel)
        fun onChannelFocused(channel: Channel)
        fun onRequestSearch()
    }

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as NuneTvApplication
        MainViewModelFactory(app.repository, app.providerStorage)
    }

    private lateinit var channelPresenter: ChannelPresenter
    private lateinit var rowsAdapter: ArrayObjectAdapter
    private var callbacks: Callbacks? = null
    private var backgroundManager: BackgroundManager? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as? Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireContext(), R.color.primary)
        backgroundManager = BackgroundManager.getInstance(requireActivity()).apply { attach(requireActivity().window) }

        val imageLoader = ImageLoader.Builder(requireContext()).build()
        channelPresenter = ChannelPresenter(imageLoader)
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
        setupEventListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwnerLiveData.observe(this) { owner ->
            owner ?: return@observe
            viewModel.uiState.observe(owner) { state ->
                buildRows(state)
            }
            viewModel.error.observe(owner) { error ->
                if (!error.isNullOrBlank()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            callbacks?.onRequestSearch()
        }
        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Channel) {
                callbacks?.onChannelClicked(item)
            }
        }
        setOnItemViewSelectedListener { _, item, _, _ ->
            if (item is Channel) {
                callbacks?.onChannelFocused(item)
            }
        }
    }

    private fun buildRows(state: MainUiState) {
        rowsAdapter.clear()
        state.liveGroups.forEach { group ->
            val adapter = ArrayObjectAdapter(channelPresenter)
            group.channels.forEach { adapter.add(it) }
            rowsAdapter.add(ListRow(HeaderItem("Live • ${group.name}"), adapter))
        }
        if (state.movies.isNotEmpty()) {
            rowsAdapter.add(createRow(getString(R.string.movies), state.movies))
        }
        if (state.series.isNotEmpty()) {
            rowsAdapter.add(createRow(getString(R.string.series), state.series))
        }
        if (state.favorites.isNotEmpty()) {
            rowsAdapter.add(createRow(getString(R.string.favorites), state.favorites))
        }
    }

    private fun createRow(title: String, channels: List<Channel>): ListRow {
        val adapter = ArrayObjectAdapter(channelPresenter)
        channels.forEach { adapter.add(it) }
        return ListRow(HeaderItem(title), adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backgroundManager?.release()
        backgroundManager = null
    }
}
