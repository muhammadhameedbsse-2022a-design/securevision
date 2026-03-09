package com.securevision.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.securevision.core.domain.model.Alert
import com.securevision.core.domain.model.AlertSeverity
import com.securevision.core.ui.components.AlertCard
import com.securevision.core.ui.components.AlertSeverity as UiAlertSeverity
import com.securevision.core.ui.components.SecureVisionTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SecureVisionTopBar(
                title = "Alerts",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header row with unread badge and mark-all-read button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgedBox(
                    badge = {
                        if (uiState.unreadCount > 0) {
                            Badge { Text(uiState.unreadCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (uiState.unreadCount > 0) {
                    IconButton(onClick = viewModel::markAllAsRead) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all as read",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedSeverity == null && !uiState.showUnreadOnly,
                        onClick = { viewModel.filterBySeverity(null) },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.showUnreadOnly,
                        onClick = viewModel::toggleUnreadFilter,
                        label = { Text("Unread") }
                    )
                }
                items(AlertSeverity.values().toList()) { severity ->
                    FilterChip(
                        selected = uiState.selectedSeverity == severity,
                        onClick = { viewModel.filterBySeverity(severity) },
                        label = { Text(severity.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredAlerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No alerts",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredAlerts, key = { it.id }) { alert ->
                        SwipeableAlertItem(
                            alert = alert,
                            onDismiss = { viewModel.deleteAlert(alert.id) },
                            onClick = { viewModel.markAsRead(alert.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableAlertItem(
    alert: Alert,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else false
        }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {}
    ) {
        val dateFormatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val uiSeverity = when (alert.severity) {
            AlertSeverity.LOW -> UiAlertSeverity.LOW
            AlertSeverity.MEDIUM -> UiAlertSeverity.MEDIUM
            AlertSeverity.HIGH -> UiAlertSeverity.HIGH
            AlertSeverity.CRITICAL -> UiAlertSeverity.CRITICAL
        }
        AlertCard(
            title = alert.title,
            description = alert.description,
            timestamp = dateFormatter.format(Date(alert.timestamp)),
            severity = uiSeverity,
            isRead = alert.isRead,
            onClick = onClick
        )
    }
}
