package com.nunetv.iptv.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nunetv.iptv.data.ProviderStorage
import com.nunetv.iptv.model.Channel
import com.nunetv.iptv.model.ChannelGroup
import com.nunetv.iptv.model.ChannelType
import com.nunetv.iptv.model.EpgProgram
import com.nunetv.iptv.model.ProviderCredentials
import com.nunetv.iptv.repository.IptvContent
import com.nunetv.iptv.repository.IptvRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val repository: IptvRepository,
    private val providerStorage: ProviderStorage
) : ViewModel() {

    private val _uiState = MutableLiveData(MainUiState())
    val uiState: LiveData<MainUiState> = _uiState

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _activeProvider = MutableLiveData<ProviderCredentials?>(null)
    val activeProvider: LiveData<ProviderCredentials?> = _activeProvider

    private val _providers = MutableLiveData<List<ProviderCredentials>>(emptyList())
    val providers: LiveData<List<ProviderCredentials>> = _providers

    init {
        refreshFromStorage()
    }

    fun refreshFromStorage() {
        viewModelScope.launch {
            val providers = withContext(Dispatchers.IO) { providerStorage.loadProviders() }
            _providers.value = providers
            val provider = withContext(Dispatchers.IO) { providerStorage.loadActiveProvider() }
            _activeProvider.value = provider
            if (provider != null) {
                loadProvider(provider)
            } else {
                ensurePlaceholderContent(force = true)
            }
        }
    }

    fun setActiveProvider(provider: ProviderCredentials) {
        providerStorage.saveActiveProvider(provider.name)
        refreshFromStorage()
    }

    fun updateContent(content: IptvContent) {
        val currentFavorites = _uiState.value?.favorites.orEmpty()
        val newFavorites = currentFavorites.filter { fav ->
            content.liveChannels.any { group -> group.channels.any { it.id == fav.id } } ||
                content.movies.any { it.id == fav.id } ||
                content.series.any { it.id == fav.id }
        }
        val favoriteIds = newFavorites.map { it.id }.toSet()
        val liveGroups = content.liveChannels.map { group ->
            group.copy(channels = group.channels.map { channel -> channel.copy(isFavorite = favoriteIds.contains(channel.id)) })
        }
        val movies = content.movies.map { channel -> channel.copy(isFavorite = favoriteIds.contains(channel.id)) }
        val series = content.series.map { channel -> channel.copy(isFavorite = favoriteIds.contains(channel.id)) }
        val epgMap = content.epgPrograms.groupBy { it.channelId }
        _uiState.value = MainUiState(
            liveGroups = liveGroups,
            movies = movies,
            series = series,
            favorites = newFavorites,
            epg = epgMap
        )
    }

    fun setEpgPrograms(channelId: String, programs: List<EpgProgram>) {
        val current = _uiState.value ?: MainUiState()
        _uiState.value = current.copy(
            epg = current.epg + (channelId to programs)
        )
    }

    fun toggleFavorite(channel: Channel) {
        val current = _uiState.value ?: MainUiState()
        val isCurrentlyFavorite = current.favorites.any { it.id == channel.id }
        val updatedFavorites = if (isCurrentlyFavorite) {
            current.favorites.filterNot { it.id == channel.id }
        } else {
            current.favorites + channel.copy(isFavorite = true)
        }

        val updateChannel: (Channel) -> Channel = { existing ->
            if (existing.id == channel.id) {
                existing.copy(isFavorite = !isCurrentlyFavorite)
            } else {
                existing
            }
        }

        val updatedGroups = current.liveGroups.map { group ->
            group.copy(channels = group.channels.map(updateChannel))
        }
        val updatedMovies = current.movies.map(updateChannel)
        val updatedSeries = current.series.map(updateChannel)

        _uiState.value = current.copy(
            liveGroups = updatedGroups,
            movies = updatedMovies,
            series = updatedSeries,
            favorites = updatedFavorites
        )
    }

    fun search(query: String): List<Channel> {
        val state = _uiState.value ?: return emptyList()
        if (query.isBlank()) return emptyList()
        val lower = query.lowercase()
        return (state.liveGroups.flatMap { it.channels } + state.movies + state.series + state.favorites)
            .distinctBy { it.id }
            .filter { it.name.lowercase().contains(lower) }
    }

    fun ensurePlaceholderContent(force: Boolean = false) {
        if (!force && _uiState.value?.liveGroups?.isNotEmpty() == true) return
        viewModelScope.launch {
            val placeholder = withContext(Dispatchers.Default) { createPlaceholderContent() }
            _uiState.postValue(placeholder)
        }
    }

    private fun createPlaceholderContent(): MainUiState {
        val demoLive = listOf(
            Channel(id = "100", name = "Nune News", url = "https://example.com/live/news", type = ChannelType.LIVE, group = "News", logo = null, epgId = "nune.news"),
            Channel(id = "101", name = "Sports Plus", url = "https://example.com/live/sports", type = ChannelType.LIVE, group = "Sports", logo = null, epgId = "sports.plus"),
            Channel(id = "102", name = "Kids Zone", url = "https://example.com/live/kids", type = ChannelType.LIVE, group = "Kids", logo = null, epgId = "kids.zone")
        )
        val liveGroups = demoLive.groupBy { it.group }
            .map { ChannelGroup(it.key, it.value) }
        val demoMovies = listOf(
            Channel(id = "200", name = "Indie Film", url = "https://example.com/vod/indie", type = ChannelType.MOVIE, group = "Indie", logo = null, epgId = null),
            Channel(id = "201", name = "Blockbuster", url = "https://example.com/vod/blockbuster", type = ChannelType.MOVIE, group = "Premium", logo = null, epgId = null)
        )
        val demoSeries = listOf(
            Channel(id = "300", name = "Galaxy Quest", url = "https://example.com/series/galaxy", type = ChannelType.SERIES, group = "Sci-Fi", logo = null, epgId = null)
        )
        val now = System.currentTimeMillis()
        val epgPrograms = mapOf(
            "nune.news" to listOf(
                EpgProgram("nune.news", "Morning Update", "Breaking headlines and weather.", now - 30 * 60 * 1000, now + 30 * 60 * 1000),
                EpgProgram("nune.news", "Global Insights", "In-depth reporting from around the globe.", now + 30 * 60 * 1000, now + 90 * 60 * 1000)
            ),
            "sports.plus" to listOf(
                EpgProgram("sports.plus", "Championship Classics", "Iconic matches revisited.", now - 60 * 60 * 1000, now),
                EpgProgram("sports.plus", "Live: Grand Finals", "Top teams face off in the finals.", now, now + 120 * 60 * 1000)
            ),
            "kids.zone" to listOf(
                EpgProgram("kids.zone", "Cartoon Adventures", "Animated fun for the whole family.", now - 15 * 60 * 1000, now + 15 * 60 * 1000),
                EpgProgram("kids.zone", "STEM Stars", "Science experiments for curious minds.", now + 15 * 60 * 1000, now + 75 * 60 * 1000)
            )
        )
        return MainUiState(
            liveGroups = liveGroups,
            movies = demoMovies,
            series = demoSeries,
            favorites = emptyList(),
            epg = epgPrograms
        )
    }

    private fun mergeContent(
        content: IptvContent,
        playlistChannels: List<Channel>,
        epgPrograms: List<EpgProgram>
    ): IptvContent {
        if (playlistChannels.isEmpty()) {
            return content.copy(epgPrograms = epgPrograms)
        }
        val existingLive = content.liveChannels.flatMap { it.channels }
        val combined = (existingLive + playlistChannels)
            .distinctBy { it.id.ifBlank { it.name } }
            .groupBy { it.group.ifBlank { "Playlist" } }
            .map { ChannelGroup(it.key, it.value) }
        return content.copy(
            liveChannels = combined,
            epgPrograms = epgPrograms
        )
    }

    private fun loadProvider(provider: ProviderCredentials) {
        viewModelScope.launch {
            _loading.value = true
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val session = repository.authenticate(provider).getOrThrow()
                    val content = repository.loadAllContent(session).getOrThrow()
                    val playlist = provider.m3uUrl?.takeIf { it.isNotBlank() }?.let { repository.fetchPlaylist(it).getOrThrow() }.orEmpty()
                    val epg = provider.epgUrl?.takeIf { it.isNotBlank() }?.let { repository.fetchEpg(it).getOrThrow() }.orEmpty()
                    Triple(content, playlist, epg)
                }
            }
            result.onSuccess { (content, playlist, epg) ->
                val mergedContent = mergeContent(content, playlist, epg)
                updateContent(mergedContent)
                _error.value = null
            }.onFailure {
                _error.value = it.message ?: "Unable to load provider"
                ensurePlaceholderContent(force = true)
            }
            _loading.value = false
        }
    }
}

data class MainUiState(
    val liveGroups: List<ChannelGroup> = emptyList(),
    val movies: List<Channel> = emptyList(),
    val series: List<Channel> = emptyList(),
    val favorites: List<Channel> = emptyList(),
    val epg: Map<String, List<EpgProgram>> = emptyMap()
)
