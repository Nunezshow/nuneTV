package com.nunetv.iptv.activities

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.nunetv.iptv.databinding.ActivitySearchBinding
import com.nunetv.iptv.fragments.ChannelSearchFragment
import com.nunetv.iptv.model.Channel
import java.util.ArrayList

class SearchActivity : FragmentActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val channels: ArrayList<Channel> = intent.getParcelableArrayListExtra(EXTRA_CHANNELS) ?: arrayListOf()
        (supportFragmentManager.findFragmentById(binding.searchFragmentContainer.id) as? ChannelSearchFragment)?.setChannels(channels)
    }

    companion object {
        const val EXTRA_CHANNELS = "extra_channels"
    }
}
