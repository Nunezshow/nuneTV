package com.nunetv.iptv.data

import com.nunetv.iptv.model.Channel
import com.nunetv.iptv.model.ChannelType
import java.util.Locale
import java.util.regex.Pattern

class M3uParser {

    private val attributePattern = Pattern.compile("([a-zA-Z0-9\-]+)\s*=\s*\"([^\"]*)\"")

    fun parse(content: String): List<Channel> {
        if (content.isBlank()) return emptyList()
        val lines = content.lines()
        val channels = mutableListOf<Channel>()
        var currentName = ""
        var currentAttributes: Map<String, String> = emptyMap()
        lines.forEach { line ->
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    val split = line.split(",", limit = 2)
                    currentName = if (split.size > 1) split[1].trim() else "Unknown"
                    currentAttributes = parseAttributes(split.first())
                }
                line.startsWith("#EXTGRP", ignoreCase = true) -> {
                    val group = line.substringAfter(":").trim()
                    currentAttributes = currentAttributes + ("group-title" to group)
                }
                line.isNotBlank() && !line.startsWith("#") -> {
                    val group = currentAttributes["group-title"].orEmpty()
                    channels += Channel(
                        id = currentAttributes["tvg-id"].orEmpty().ifBlank { line.hashCode().toString() },
                        name = currentAttributes["tvg-name"].orEmpty().ifBlank { currentName },
                        url = line.trim(),
                        type = mapGroupToType(group),
                        group = group,
                        logo = currentAttributes["tvg-logo"],
                        epgId = currentAttributes["tvg-id"]
                    )
                    currentName = ""
                    currentAttributes = emptyMap()
                }
            }
        }
        return channels
    }

    private fun parseAttributes(header: String): Map<String, String> {
        val matcher = attributePattern.matcher(header)
        val map = mutableMapOf<String, String>()
        while (matcher.find()) {
            map[matcher.group(1)] = matcher.group(2)
        }
        return map
    }

    private fun mapGroupToType(group: String): ChannelType {
        val lower = group.lowercase(Locale.ROOT)
        return when {
            lower.contains("movie") || lower.contains("vod") -> ChannelType.MOVIE
            lower.contains("series") || lower.contains("show") -> ChannelType.SERIES
            else -> ChannelType.LIVE
        }
    }
}
