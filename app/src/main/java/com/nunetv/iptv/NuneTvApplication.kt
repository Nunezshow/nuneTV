package com.nunetv.iptv

import android.app.Application
import com.nunetv.iptv.data.EpgParser
import com.nunetv.iptv.data.M3uParser
import com.nunetv.iptv.data.ProviderStorage
import com.nunetv.iptv.network.XStreamCodesService
import com.nunetv.iptv.repository.IptvRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient

class NuneTvApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val m3uParser by lazy { M3uParser() }
    private val epgParser by lazy { EpgParser() }
    private val xStreamService by lazy { XStreamCodesService(okHttpClient) }

    val repository: IptvRepository by lazy {
        IptvRepository(xStreamService, m3uParser, epgParser, okHttpClient)
    }

    val providerStorage: ProviderStorage by lazy {
        ProviderStorage(this)
    }
}
