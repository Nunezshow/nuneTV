package com.nunetv.iptv.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nunetv.iptv.data.ProviderStorage
import com.nunetv.iptv.repository.IptvRepository

class SettingsViewModelFactory(
    private val providerStorage: ProviderStorage,
    private val repository: IptvRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(providerStorage, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
