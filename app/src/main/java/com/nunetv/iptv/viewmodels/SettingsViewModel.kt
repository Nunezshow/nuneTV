package com.nunetv.iptv.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nunetv.iptv.data.ProviderStorage
import com.nunetv.iptv.model.ProviderCredentials
import com.nunetv.iptv.repository.IptvRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val providerStorage: ProviderStorage,
    private val repository: IptvRepository
) : ViewModel() {

    private val _providers = MutableLiveData<List<ProviderCredentials>>(emptyList())
    val providers: LiveData<List<ProviderCredentials>> = _providers

    private val _activeProviderName = MutableLiveData<String?>(null)
    val activeProviderName: LiveData<String?> = _activeProviderName

    private val _testState = MutableLiveData<TestState>(TestState.Idle)
    val testState: LiveData<TestState> = _testState

    init {
        loadProviders()
    }

    fun loadProviders() {
        val providers = providerStorage.loadProviders()
        _providers.value = providers
        _activeProviderName.value = providerStorage.loadActiveProviderName()
    }

    fun saveProvider(credentials: ProviderCredentials) {
        providerStorage.saveProvider(credentials)
        loadProviders()
    }

    fun deleteProvider(name: String) {
        providerStorage.deleteProvider(name)
        loadProviders()
    }

    fun setActiveProvider(name: String) {
        providerStorage.saveActiveProvider(name)
        loadProviders()
    }

    fun testProvider(credentials: ProviderCredentials) {
        viewModelScope.launch {
            _testState.value = TestState.Loading
            val result = withContext(Dispatchers.IO) {
                repository.testConnection(credentials)
            }
            _testState.value = result.fold(
                onSuccess = { success ->
                    if (success) TestState.Success else TestState.Error("No streams returned")
                },
                onFailure = { throwable ->
                    TestState.Error(throwable.message ?: "Unable to connect")
                }
            )
        }
    }
}

sealed class TestState {
    object Idle : TestState()
    object Loading : TestState()
    object Success : TestState()
    data class Error(val message: String) : TestState()
}
