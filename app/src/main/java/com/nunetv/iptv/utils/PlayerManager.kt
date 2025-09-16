package com.nunetv.iptv.utils

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class PlayerManager(context: Context) {

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setUserAgent("NuneTV/1.0")

    private val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .build()

    fun getPlayer(): ExoPlayer = exoPlayer

    fun prepare(url: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(null)
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun play() {
        exoPlayer.playWhenReady = true
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    fun release() {
        exoPlayer.release()
    }

    fun addListener(listener: Player.Listener) {
        exoPlayer.addListener(listener)
    }
}
