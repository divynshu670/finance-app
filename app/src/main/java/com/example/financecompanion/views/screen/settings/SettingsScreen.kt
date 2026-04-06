package com.example.financecompanion.views.screen.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financecompanion.R
import com.example.financecompanion.domain.model.AppCurrency
import com.example.financecompanion.domain.model.AppSettings
import com.example.financecompanion.security.BiometricAuthManager
import com.example.financecompanion.security.BiometricAvailability
import com.example.financecompanion.viewmodel.SettingsEvent
import com.example.financecompanion.viewmodel.SettingsViewModel
import com.example.financecompanion.views.components.common.ResponsiveScreenContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(
            context.applicationContext as android.app.Application
        )
    )
    val settings by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val biometricAvailability = remember(context) {
        BiometricAuthManager.getAvailability(context)
    }
    val screenHorizontalPadding = dimensionResource(R.dimen.screen_horizontal_padding)
    val screenVerticalPadding = dimensionResource(R.dimen.screen_vertical_padding)
    val sectionSpacing = dimensionResource(R.dimen.screen_section_spacing)

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setNotificationsEnabled(true)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Notification permission denied")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Message -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is SettingsEvent.ShareExport -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        putExtra(Intent.EXTRA_SUBJECT, event.fileName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(shareIntent, "Share export")
                    )
                }
            }
        }
    }

    LaunchedEffect(biometricAvailability, settings.biometricLockEnabled) {
        if (biometricAvailability is BiometricAvailability.Unavailable &&
            settings.biometricLockEnabled
        ) {
            viewModel.setBiometricEnabled(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        ResponsiveScreenContainer(
            modifier = Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = screenHorizontalPadding,
                    end = screenHorizontalPadding,
                    top = screenVerticalPadding - 4.dp,
                    bottom = screenVerticalPadding + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing + 2.dp)
            ) {
                item {
                    SettingsSection(
                        title = "Appearance",
                        rows = {
                            SettingsToggleRow(
                                icon = Icons.Outlined.DarkMode,
                                title = "Dark Mode",
                                subtitle = "Switch between light and dark theme",
                                checked = settings.isDarkMode,
                                onCheckedChange = viewModel::setDarkMode
                            )
                        }
                    )
                }

                item {
                    SettingsSection(
                        title = "Preferences",
                        rows = {
                            SettingsActionRow(
                                icon = Icons.Outlined.Payments,
                                title = "Currency",
                                subtitle = "${settings.currency.displayName} (${settings.currency.code})",
                                onClick = { showCurrencyDialog = true }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            SettingsToggleRow(
                                icon = Icons.Outlined.Notifications,
                                title = "Daily Reminders",
                                subtitle = "Send one reminder every day at a random time",
                                checked = settings.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (!enabled) {
                                        viewModel.setNotificationsEnabled(false)
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        viewModel.setNotificationsEnabled(true)
                                    }
                                }
                            )
                        }
                    )
                }

                item {
                    SettingsSection(
                        title = "Security",
                        rows = {
                            SettingsToggleRow(
                                icon = Icons.Outlined.Fingerprint,
                                title = "Biometric Lock",
                                subtitle = biometricSubtitle(
                                    availability = biometricAvailability,
                                    enabled = settings.biometricLockEnabled
                                ),
                                checked = settings.biometricLockEnabled,
                                enabled = biometricAvailability is BiometricAvailability.Available,
                                onCheckedChange = { enabled ->
                                    when {
                                        biometricAvailability is BiometricAvailability.Available -> {
                                            viewModel.setBiometricEnabled(enabled)
                                        }

                                        biometricAvailability is BiometricAvailability.Unavailable -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    biometricAvailability.message
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    )
                }

                item {
                    SettingsSection(
                        title = "Data",
                        rows = {
                            SettingsActionRow(
                                icon = Icons.Outlined.FileDownload,
                                title = "Export Data",
                                subtitle = "Export transactions as CSV and share the file",
                                onClick = viewModel::exportData
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            SettingsActionRow(
                                icon = Icons.Outlined.DeleteSweep,
                                title = "Clear All Data",
                                subtitle = "Delete transactions, goals, and saved preferences",
                                onClick = { showClearDataDialog = true },
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            selectedCurrency = settings.currency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                viewModel.setCurrency(it)
                showCurrencyDialog = false
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = {
                Text("This will permanently remove your transactions, goals, and saved settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    rows: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = rows
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsLeadingIcon(icon = icon, tint = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        })

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsLeadingIcon(icon = icon, tint = tint)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsLeadingIcon(
    icon: ImageVector,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = tint.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
    }
}

@Composable
private fun CurrencySelectionDialog(
    selectedCurrency: AppCurrency,
    onDismiss: () -> Unit,
    onSelect: (AppCurrency) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCurrency.entries.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(currency) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currency == selectedCurrency,
                            onClick = { onSelect(currency) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${currency.displayName} (${currency.symbol})")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun biometricSubtitle(
    availability: BiometricAvailability,
    enabled: Boolean
): String {
    return when (availability) {
        BiometricAvailability.Available -> {
            if (enabled) {
                "Prompt for fingerprint or face unlock when the app opens"
            } else {
                "Protect the app with fingerprint or face unlock"
            }
        }

        is BiometricAvailability.Unavailable -> availability.message
    }
}
