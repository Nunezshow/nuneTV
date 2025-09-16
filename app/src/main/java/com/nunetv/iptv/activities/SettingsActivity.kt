package com.nunetv.iptv.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nunetv.iptv.NuneTvApplication
import com.nunetv.iptv.R
import com.nunetv.iptv.adapters.ProviderAdapter
import com.nunetv.iptv.databinding.ActivitySettingsBinding
import com.nunetv.iptv.model.ProviderCredentials
import com.nunetv.iptv.viewmodels.SettingsViewModel
import com.nunetv.iptv.viewmodels.SettingsViewModelFactory
import com.nunetv.iptv.viewmodels.TestState

class SettingsActivity : FragmentActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var adapter: ProviderAdapter

    private val viewModel: SettingsViewModel by viewModels {
        val app = application as NuneTvApplication
        SettingsViewModelFactory(app.providerStorage, app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupButtons()
        observeViewModel()
    }

    private fun setupRecycler() {
        adapter = ProviderAdapter { provider ->
            fillForm(provider)
        }
        binding.providerList.layoutManager = LinearLayoutManager(this)
        binding.providerList.adapter = adapter
    }

    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            val credentials = readForm() ?: return@setOnClickListener
            viewModel.saveProvider(credentials)
            Toast.makeText(this, R.string.save_provider, Toast.LENGTH_SHORT).show()
        }
        binding.buttonTest.setOnClickListener {
            val credentials = readForm() ?: return@setOnClickListener
            viewModel.testProvider(credentials)
        }
        binding.buttonActivate.setOnClickListener {
            adapter.getSelected()?.let { provider ->
                viewModel.setActiveProvider(provider.name)
                Toast.makeText(this, R.string.switch_provider, Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonDelete.setOnClickListener {
            adapter.getSelected()?.let { provider ->
                viewModel.deleteProvider(provider.name)
                Toast.makeText(this, R.string.delete_provider, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.providers.observe(this) { providers ->
            adapter.submitList(providers, viewModel.activeProviderName.value)
            if (providers.isNotEmpty()) {
                val selected = adapter.getSelected() ?: providers.first()
                adapter.select(selected)
                fillForm(selected)
            } else {
                clearForm()
            }
        }
        viewModel.activeProviderName.observe(this) { name ->
            adapter.submitList(viewModel.providers.value.orEmpty(), name)
            binding.activeProviderLabel.text = name?.let {
                getString(R.string.active_provider) + ": " + it
            } ?: getString(R.string.no_active_provider)
        }
        viewModel.testState.observe(this) { state ->
            when (state) {
                TestState.Loading -> Toast.makeText(this, R.string.status_testing, Toast.LENGTH_SHORT).show()
                TestState.Success -> Toast.makeText(this, R.string.status_success, Toast.LENGTH_SHORT).show()
                is TestState.Error -> Toast.makeText(this, state.message ?: getString(R.string.status_failed), Toast.LENGTH_SHORT).show()
                TestState.Idle -> Unit
            }
        }
    }

    private fun readForm(): ProviderCredentials? {
        val name = binding.inputProviderName.text?.toString().orEmpty()
        val portal = binding.inputPortal.text?.toString().orEmpty()
        val username = binding.inputUsername.text?.toString().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()
        val m3u = binding.inputM3u.text?.toString()?.takeIf { it.isNotBlank() }
        val epg = binding.inputEpg.text?.toString()?.takeIf { it.isNotBlank() }
        if (name.isBlank() || portal.isBlank() || username.isBlank() || password.isBlank()) {
            Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show()
            return null
        }
        return ProviderCredentials(
            name = name,
            portalUrl = portal,
            username = username,
            password = password,
            m3uUrl = m3u,
            epgUrl = epg
        )
    }

    private fun fillForm(provider: ProviderCredentials) {
        binding.inputProviderName.setText(provider.name)
        binding.inputPortal.setText(provider.portalUrl)
        binding.inputUsername.setText(provider.username)
        binding.inputPassword.setText(provider.password)
        binding.inputM3u.setText(provider.m3uUrl.orEmpty())
        binding.inputEpg.setText(provider.epgUrl.orEmpty())
    }

    private fun clearForm() {
        binding.inputProviderName.setText("")
        binding.inputPortal.setText("")
        binding.inputUsername.setText("")
        binding.inputPassword.setText("")
        binding.inputM3u.setText("")
        binding.inputEpg.setText("")
    }
}
