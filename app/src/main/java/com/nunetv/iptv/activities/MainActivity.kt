package com.nunetv.iptv.activities

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.leanback.app.GuidedStepSupportFragment
import com.nunetv.iptv.NuneTvApplication
import com.nunetv.iptv.databinding.ActivityMainBinding
import com.nunetv.iptv.fragments.ChannelDetailsFragment
import com.nunetv.iptv.fragments.EpgGridFragment
import com.nunetv.iptv.fragments.MainBrowseFragment
import com.nunetv.iptv.model.Channel
import com.nunetv.iptv.model.EpgProgram
import com.nunetv.iptv.viewmodels.MainViewModel
import com.nunetv.iptv.viewmodels.MainViewModelFactory
import java.util.ArrayList

class MainActivity : FragmentActivity(), MainBrowseFragment.Callbacks, ChannelDetailsFragment.Callback {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        val app = application as NuneTvApplication
        MainViewModelFactory(app.repository, app.providerStorage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onChannelClicked(channel: Channel) {
        GuidedStepSupportFragment.add(supportFragmentManager, ChannelDetailsFragment.newInstance(channel))
    }

    override fun onChannelFocused(channel: Channel) {
        val channelKey = channel.epgId ?: channel.id
        val programs = viewModel.uiState.value?.epg?.get(channelKey).orEmpty()
        if (programs.isEmpty()) {
            hideEpg()
        } else {
            showEpg(programs)
        }
    }

    override fun onRequestSearch() {
        val state = viewModel.uiState.value
        val live = state?.liveGroups?.flatMap { it.channels }.orEmpty()
        val aggregated = (live + state?.movies.orEmpty() + state?.series.orEmpty() + state?.favorites.orEmpty())
            .distinctBy { it.id }
        val intent = Intent(this, SearchActivity::class.java).apply {
            putParcelableArrayListExtra(SearchActivity.EXTRA_CHANNELS, ArrayList(aggregated))
        }
        startActivity(intent)
    }

    override fun onPlayChannel(channel: Channel) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_CHANNEL_ID, channel.id)
            putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, channel.name)
            putExtra(PlayerActivity.EXTRA_CHANNEL_URL, channel.url)
        }
        startActivity(intent)
    }

    override fun onToggleFavorite(channel: Channel) {
        viewModel.toggleFavorite(channel)
    }

    override fun onShowGuide(channel: Channel) {
        onChannelFocused(channel)
    }

    private fun showEpg(programs: List<EpgProgram>) {
        val fragment = supportFragmentManager.findFragmentById(binding.epgContainer.id) as? EpgGridFragment
        if (fragment == null) {
            supportFragmentManager.commit {
                replace(binding.epgContainer.id, EpgGridFragment.newInstance(programs))
            }
        } else {
            fragment.updatePrograms(programs)
        }
        binding.epgContainer.isVisible = true
    }

    private fun hideEpg() {
        binding.epgContainer.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFromStorage()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }
}
