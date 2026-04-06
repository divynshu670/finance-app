package com.example.financecompanion

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financecompanion.navigation.AppNavHost
import com.example.financecompanion.notifications.DailyReminderScheduler
import com.example.financecompanion.security.BiometricAuthManager
import com.example.financecompanion.security.BiometricAvailability
import com.example.financecompanion.ui.FinanceCompanionTheme
import com.example.financecompanion.utils.CurrencyFormatState
import com.example.financecompanion.viewmodel.AppViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: AppViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            if (!uiState.isReady || uiState.startDestination == "loading") {
                Box(
                    modifier = Modifier.fillMaxSize()
                )
                return@setContent
            }

            CurrencyFormatState.currentCurrency = uiState.settings.currency

            LaunchedEffect(uiState.settings.notificationsEnabled) {
                if (uiState.settings.notificationsEnabled) {
                    DailyReminderScheduler.schedule(applicationContext)
                } else {
                    DailyReminderScheduler.cancel(applicationContext)
                }
            }

            FinanceCompanionTheme(
                darkTheme = uiState.settings.isDarkMode
            ) {
                key(uiState.settings.currency) {
                    BiometricGate(
                        activity = this@MainActivity,
                        enabled = uiState.settings.biometricLockEnabled
                    ) {
                        AppNavHost(
                            startDestination = uiState.startDestination,
                            onFinishOnboarding = {
                                viewModel.completeOnboarding()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BiometricGate(
    activity: FragmentActivity,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val availability = remember(enabled) {
        if (enabled) {
            BiometricAuthManager.getAvailability(activity)
        } else {
            BiometricAvailability.Available
        }
    }
    var isAuthenticated by rememberSaveable(enabled) {
        mutableStateOf(!enabled || availability is BiometricAvailability.Unavailable)
    }
    var errorMessage by remember(enabled) { mutableStateOf<String?>(null) }
    var promptTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(enabled, availability) {
        isAuthenticated = !enabled || availability is BiometricAvailability.Unavailable
        errorMessage = if (availability is BiometricAvailability.Unavailable && enabled) {
            availability.message
        } else {
            null
        }

        if (enabled && availability is BiometricAvailability.Available) {
            promptTrigger++
        }
    }

    LaunchedEffect(promptTrigger) {
        if (enabled && availability is BiometricAvailability.Available && !isAuthenticated) {
            BiometricAuthManager.prompt(
                activity = activity,
                onSuccess = {
                    isAuthenticated = true
                    errorMessage = null
                },
                onFailure = { message ->
                    errorMessage = message
                }
            )
        }
    }

    if (isAuthenticated) {
        content()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Unlock Finance Companion",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = errorMessage ?: "Authenticate to continue",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = { promptTrigger++ }) {
                Text("Try Again")
            }
        }
    }
}
