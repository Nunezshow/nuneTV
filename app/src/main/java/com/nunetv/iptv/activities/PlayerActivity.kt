package com.nunetv.iptv.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.exoplayer2.Player
import com.nunetv.iptv.R
import com.nunetv.iptv.databinding.ActivityPlayerBinding
import com.nunetv.iptv.utils.PlayerManager

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var playerManager: PlayerManager
    private var streamUrl: String? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            binding.playerLoading.isVisible = playbackState == Player.STATE_BUFFERING
        }

        override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {
            binding.playerLoading.isVisible = false
            Toast.makeText(this@PlayerActivity, error.localizedMessage ?: getString(R.string.connection_failed), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME).orEmpty()
        streamUrl = intent.getStringExtra(EXTRA_CHANNEL_URL)
        binding.channelTitle.text = channelName

        playerManager = PlayerManager(this)
        binding.playerView.player = playerManager.getPlayer()
        playerManager.addListener(playerListener)
    }

    override fun onStart() {
        super.onStart()
        startPlayback()
    }

    override fun onStop() {
        super.onStop()
        playerManager.stop()
    }

    override fun onDestroy() {
        binding.playerView.player = null
        playerManager.release()
        super.onDestroy()
    }

    private fun startPlayback() {
        val url = streamUrl
        if (url.isNullOrBlank()) {
            Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.playerLoading.isVisible = true
        playerManager.prepare(url)
        playerManager.play()
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        const val EXTRA_CHANNEL_URL = "extra_channel_url"
    }
}
