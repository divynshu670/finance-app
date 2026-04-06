package com.example.financecompanion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.financecompanion.data.local.prefrences.PreferenceManager
import com.example.financecompanion.domain.model.AppSettings
import com.example.financecompanion.navigation.AppRoutes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val startDestination: String = "loading",
    val settings: AppSettings = AppSettings(),
    val isReady: Boolean = false
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val pref = PreferenceManager(application)

    val uiState: StateFlow<AppUiState> = combine(
        pref.isOnboardingDone,
        pref.appSettings
    ) { onboardingDone, settings ->
        AppUiState(
            startDestination = if (onboardingDone) AppRoutes.MAIN else AppRoutes.ONBOARDING,
            settings = settings,
            isReady = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState()
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            pref.setOnboardingDone()
        }
    }
}
