package com.nunetv.iptv.fragments

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.nunetv.iptv.R
import com.nunetv.iptv.model.Channel

class ChannelDetailsFragment : GuidedStepSupportFragment() {

    interface Callback {
        fun onPlayChannel(channel: Channel)
        fun onToggleFavorite(channel: Channel)
        fun onShowGuide(channel: Channel)
    }

    private var callback: Callback? = null
    private lateinit var channel: Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callback = activity as? Callback
        channel = requireArguments().getParcelable(ARG_CHANNEL) ?: error("Channel required")
    }

    override fun onDestroy() {
        callback = null
        super.onDestroy()
    }

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val description = getString(R.string.live_tv)
        return GuidanceStylist.Guidance(channel.name, channel.group, description, null)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions += GuidedAction.Builder(requireContext())
            .id(ACTION_PLAY)
            .title(getString(R.string.play_now))
            .build()

        val favoriteTitle = if (channel.isFavorite) {
            getString(R.string.remove_favorite)
        } else {
            getString(R.string.add_to_favorites)
        }
        actions += GuidedAction.Builder(requireContext())
            .id(ACTION_FAVORITE)
            .title(favoriteTitle)
            .build()

        actions += GuidedAction.Builder(requireContext())
            .id(ACTION_GUIDE)
            .title(getString(R.string.view_guide))
            .build()
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_PLAY -> callback?.onPlayChannel(channel)
            ACTION_FAVORITE -> callback?.onToggleFavorite(channel)
            ACTION_GUIDE -> callback?.onShowGuide(channel)
        }
        parentFragmentManager.popBackStack()
    }

    companion object {
        private const val ARG_CHANNEL = "arg_channel"
        private const val ACTION_PLAY = 1L
        private const val ACTION_FAVORITE = 2L
        private const val ACTION_GUIDE = 3L

        fun newInstance(channel: Channel): ChannelDetailsFragment {
            return ChannelDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CHANNEL, channel)
                }
            }
        }
    }
}
