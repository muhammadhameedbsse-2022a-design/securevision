package com.securevision.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.securevision.core.ui.components.SecureVisionTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            SecureVisionTopBar(
                title = "Settings",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSectionHeader("Notifications")
                SettingsCard {
                    SettingsToggleRow(
                        title = "Push Notifications",
                        subtitle = "Receive alerts on this device",
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                    SettingsToggleRow(
                        title = "Alert Sound",
                        subtitle = "Play sound for new alerts",
                        checked = uiState.alertSoundEnabled,
                        onCheckedChange = viewModel::setAlertSoundEnabled
                    )
                    SettingsToggleRow(
                        title = "Vibration",
                        subtitle = "Vibrate on new alerts",
                        checked = uiState.alertVibrationEnabled,
                        onCheckedChange = viewModel::setAlertVibrationEnabled
                    )
                }
            }

            item {
                SettingsSectionHeader("Detection")
                SettingsCard {
                    SettingsToggleRow(
                        title = "Face Detection",
                        subtitle = "Detect and recognize faces",
                        checked = uiState.faceDetectionEnabled,
                        onCheckedChange = viewModel::setFaceDetectionEnabled
                    )
                    SettingsToggleRow(
                        title = "Weapon Detection",
                        subtitle = "Detect weapons in frame",
                        checked = uiState.weaponDetectionEnabled,
                        onCheckedChange = viewModel::setWeaponDetectionEnabled
                    )
                    SettingsToggleRow(
                        title = "Attribute Analysis",
                        subtitle = "Analyze clothing & attributes",
                        checked = uiState.attributeDetectionEnabled,
                        onCheckedChange = viewModel::setAttributeDetectionEnabled
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Confidence Threshold",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${(uiState.detectionConfidenceThreshold * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = uiState.detectionConfidenceThreshold,
                            onValueChange = viewModel::setConfidenceThreshold,
                            valueRange = 0.4f..0.95f,
                            steps = 10
                        )
                        Text(
                            text = "Minimum confidence required to trigger an alert",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader("Camera")
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resolution",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CameraResolution.values().forEach { resolution ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = resolution.label,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                androidx.compose.material3.RadioButton(
                                    selected = uiState.cameraResolution == resolution,
                                    onClick = { viewModel.setCameraResolution(resolution) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionHeader("Appearance")
                SettingsCard {
                    SettingsToggleRow(
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        checked = uiState.darkMode,
                        onCheckedChange = viewModel::setDarkMode
                    )
                }
            }

            item {
                SettingsSectionHeader("Data Retention")
                SettingsCard {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Keep events for", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${uiState.retentionDays} days",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = uiState.retentionDays.toFloat(),
                            onValueChange = { viewModel.setRetentionDays(it.toInt()) },
                            valueRange = 7f..365f,
                            steps = 11
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
