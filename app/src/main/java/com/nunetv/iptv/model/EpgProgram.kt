package com.nunetv.iptv.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EpgProgram(
    val channelId: String,
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long
) : Parcelable
