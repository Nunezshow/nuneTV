package com.nunetv.iptv.model

data class ProviderCredentials(
    val name: String,
    val portalUrl: String,
    val username: String,
    val password: String,
    val m3uUrl: String? = null,
    val epgUrl: String? = null
)
