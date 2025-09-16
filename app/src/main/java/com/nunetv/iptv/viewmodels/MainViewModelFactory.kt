package com.nunetv.iptv.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nunetv.iptv.data.ProviderStorage
import com.nunetv.iptv.repository.IptvRepository

class MainViewModelFactory(
    private val repository: IptvRepository,
    private val providerStorage: ProviderStorage
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, providerStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
