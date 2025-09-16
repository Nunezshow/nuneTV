package com.nunetv.iptv.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val type: ChannelType,
    val group: String,
    val logo: String?,
    val epgId: String?,
    val isFavorite: Boolean = false
) : Parcelable

enum class ChannelType {
    LIVE,
    MOVIE,
    SERIES
}
