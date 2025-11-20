package com.disciplinedminds.ui.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.disciplinedminds.R
import com.disciplinedminds.ui.components.bounceClick

@Composable
fun PermissionScreen(
    state: PermissionState,
    onRequestUsageAccess: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Accent heading card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.permission_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.permission_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (state.shouldShowUsageRationale) {
            OptionalInfoCard(text = stringResource(R.string.permission_usage_rationale))
            Spacer(modifier = Modifier.height(16.dp))
        }

        PermissionCard(
            title = stringResource(R.string.permission_usage_title),
            description = stringResource(R.string.permission_usage_body),
            granted = state.hasUsageAccess,
            actionText = stringResource(if (state.hasUsageAccess) R.string.permission_granted else R.string.permission_grant),
            onClick = onRequestUsageAccess
        )

        PermissionCard(
            title = stringResource(R.string.permission_overlay_title),
            description = stringResource(R.string.permission_overlay_body),
            granted = state.hasOverlay,
            actionText = stringResource(if (state.hasOverlay) R.string.permission_granted else R.string.permission_grant),
            onClick = onRequestOverlayPermission
        )

        PermissionCard(
            title = stringResource(R.string.permission_notification_title),
            description = stringResource(R.string.permission_notification_body),
            granted = state.hasNotificationAccess,
            actionText = stringResource(if (state.hasNotificationAccess) R.string.permission_granted else R.string.permission_grant_optional),
            onClick = onRequestNotificationAccess,
            optional = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(),
            enabled = state.allGranted,
            onClick = onContinue,
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(text = stringResource(if (state.allGranted) R.string.permission_continue else R.string.permission_grant_all))
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    actionText: String,
    onClick: () -> Unit,
    optional: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Colored icon badge for visual emphasis
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = (if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error).copy(alpha = 0.12f)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
                if (optional) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.permission_optional_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                enabled = optional || !granted,
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(text = actionText)
            }
        }
    }
}

@Composable
private fun OptionalInfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
