package com.nunetv.iptv.network

import com.nunetv.iptv.model.Channel
import com.nunetv.iptv.model.ChannelType
import com.nunetv.iptv.model.ProviderCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class XStreamCodesService(
    private val client: OkHttpClient = OkHttpClient()
) {

    suspend fun login(credentials: ProviderCredentials): Result<XStreamLoginResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val url = buildUrl(credentials.portalUrl, credentials.username, credentials.password, null)
            val request = Request.Builder().url(url).build()
            val body = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Login failed with code ${response.code}")
                }
                response.body?.string().orEmpty()
            }
            val json = JSONObject(body)
            val userInfo = json.optJSONObject("user_info")
                ?: throw IOException("Invalid response")
            val status = userInfo.optString("status", "false")
            if (status != "Active") {
                throw IOException(userInfo.optString("message", "Inactive account"))
            }
            val serverInfo = json.optJSONObject("server_info")
            val streamBase = serverInfo?.optString("url", credentials.portalUrl)?.let {
                if (it.startsWith("http")) it else "http://$it"
            } ?: credentials.portalUrl
            XStreamLoginResponse(
                credentials = credentials,
                serverUrl = streamBase,
                authUsername = credentials.username,
                authPassword = credentials.password
            )
        }
    }

    suspend fun fetchLiveStreams(session: XStreamLoginResponse): Result<List<Channel>> =
        fetchChannels(session, "get_live_streams", ChannelType.LIVE)

    suspend fun fetchVodStreams(session: XStreamLoginResponse): Result<List<Channel>> =
        fetchChannels(session, "get_vod_streams", ChannelType.MOVIE)

    suspend fun fetchSeries(session: XStreamLoginResponse): Result<List<Channel>> =
        fetchChannels(session, "get_series", ChannelType.SERIES)

    private suspend fun fetchChannels(
        session: XStreamLoginResponse,
        action: String,
        type: ChannelType
    ): Result<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = buildUrl(session.serverUrl, session.authUsername, session.authPassword, action)
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed request ${response.code}")
                val body = response.body?.string().orEmpty()
                val jsonArray = JSONArray(body)
                buildList {
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.optJSONObject(i) ?: continue
                        add(
                            Channel(
                                id = item.optString("stream_id", item.optString("series_id", "")),
                                name = item.optString("name", "Unnamed"),
                                url = buildStreamUrl(session, item),
                                type = type,
                                group = item.optString("category_name", ""),
                                logo = item.optString("stream_icon", null),
                                epgId = item.optString("epg_channel_id", null)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun buildStreamUrl(session: XStreamLoginResponse, item: JSONObject): String {
        val streamId = item.optString("stream_id", item.optString("series_id", ""))
        val containerExt = item.optString("container_extension", "m3u8")
        val base = session.serverUrl.trimEnd('/')
        return "$base/live/${session.authUsername}/${session.authPassword}/$streamId.$containerExt"
    }

    private fun buildUrl(
        baseUrl: String,
        username: String,
        password: String,
        action: String?
    ): String {
        val normalized = if (baseUrl.endsWith("/player_api.php")) baseUrl else baseUrl.trimEnd('/') + "/player_api.php"
        val builder = StringBuilder(normalized)
        builder.append("?username=")
            .append(URLEncoder.encode(username, "UTF-8"))
            .append("&password=")
            .append(URLEncoder.encode(password, "UTF-8"))
        if (!action.isNullOrBlank()) {
            builder.append("&action=").append(action)
        }
        return builder.toString()
    }
}

data class XStreamLoginResponse(
    val credentials: ProviderCredentials,
    val serverUrl: String,
    val authUsername: String,
    val authPassword: String
)
