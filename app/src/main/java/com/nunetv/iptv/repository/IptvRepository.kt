package com.nunetv.iptv.repository

import com.nunetv.iptv.data.EpgParser
import com.nunetv.iptv.data.M3uParser
import com.nunetv.iptv.model.Channel
import com.nunetv.iptv.model.ChannelGroup
import com.nunetv.iptv.model.EpgProgram
import com.nunetv.iptv.model.ProviderCredentials
import com.nunetv.iptv.network.XStreamLoginResponse
import com.nunetv.iptv.network.XStreamCodesService
import com.nunetv.iptv.utils.IptvException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class IptvRepository(
    private val xStreamCodesService: XStreamCodesService,
    private val m3uParser: M3uParser,
    private val epgParser: EpgParser,
    private val httpClient: OkHttpClient = OkHttpClient()
) {

    suspend fun authenticate(credentials: ProviderCredentials): Result<XStreamLoginResponse> {
        return xStreamCodesService.login(credentials)
    }

    suspend fun loadAllContent(session: XStreamLoginResponse): Result<IptvContent> = withContext(Dispatchers.IO) {
        runCatching {
            val live = xStreamCodesService.fetchLiveStreams(session).getOrThrow()
            val movies = xStreamCodesService.fetchVodStreams(session).getOrThrow()
            val series = xStreamCodesService.fetchSeries(session).getOrThrow()
            IptvContent(
                liveChannels = groupByCategory(live),
                movies = movies,
                series = series
            )
        }
    }

    suspend fun fetchPlaylist(url: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
            val content = download(url)
            val channels = m3uParser.parse(content)
            if (channels.isEmpty()) {
                throw IptvException("Empty playlist")
            }
            channels
        }
    }

    suspend fun fetchEpg(url: String): Result<List<EpgProgram>> = withContext(Dispatchers.IO) {
        runCatching {
            val content = download(url)
            epgParser.parse(content)
        }
    }

    suspend fun testConnection(credentials: ProviderCredentials): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val session = authenticate(credentials).getOrThrow()
            xStreamCodesService.fetchLiveStreams(session).getOrThrow().isNotEmpty()
        }
    }

    private fun groupByCategory(channels: List<Channel>): List<ChannelGroup> {
        return channels.groupBy { it.group.ifBlank { "Ungrouped" } }
            .map { (group, items) -> ChannelGroup(name = group, channels = items) }
            .sortedBy { it.name }
    }

    private fun download(url: String): String {
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to download: ${response.code}")
            return response.body?.string().orEmpty()
        }
    }
}

data class IptvContent(
    val liveChannels: List<ChannelGroup>,
    val movies: List<Channel>,
    val series: List<Channel>,
    val epgPrograms: List<EpgProgram> = emptyList()
)
