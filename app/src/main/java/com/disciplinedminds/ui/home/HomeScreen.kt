package com.disciplinedminds.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disciplinedminds.R
import com.disciplinedminds.ui.components.bounceClick

private val DurationOptions = listOf(30, 60, 90, 120)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onExtendTimer: () -> Unit,
    onToggleStudyMode: (Boolean) -> Unit,
    onManageApps: () -> Unit,
    onManualRefresh: () -> Unit
) {
    // Themed background
    val backgroundColor = MaterialTheme.colorScheme.background
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Logo and Title
        Spacer(modifier = Modifier.height(32.dp))
        
        // Brain Logo
        Icon(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp),
            tint = Color.Unspecified
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App Title
        Text(
            text = "DisciplinedMinds",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Tagline
        Text(
            text = "Stay focused, achieve more",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Blocking Status Card
        StatusCard(
            uiState = uiState,
            onManualRefresh = onManualRefresh
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Focus Timer Card
        TimerCard(
            uiState = uiState,
            selectedDuration = selectedDuration,
            onDurationSelected = onDurationSelected,
            onStartTimer = onStartTimer,
            onStopTimer = onStopTimer,
            onExtendTimer = onExtendTimer,
            onShowConfirmDialog = { showConfirmDialog = true }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Quick Actions Card
        QuickActionsCard(
            onManageApps = onManageApps
        )
        
        // Study Mode Card
        // StudyModeCard(
        //     isStudyMode = uiState.isStudyMode,
        //     onToggle = onToggleStudyMode
        // )
        
        // Manage Apps Card
        // ManageAppsCard(
        //     lockedCount = uiState.lockedApps,
        //     unlockedCount = uiState.unlockedApps,
        //     onManageApps = onManageApps
        // )
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        StartTimerConfirmDialog(
            duration = selectedDuration,
            onConfirm = {
                showConfirmDialog = false
                onStartTimer()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
private fun StartTimerConfirmDialog(
    duration: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val durationText = when (duration) {
        30 -> "30 minutes"
        60 -> "1 hour"
        90 -> "1 hour 30 minutes"
        120 -> "2 hours"
        else -> "$duration minutes"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Start Focus Timer?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "This will start a $durationText focus session and block all locked apps. You won't be able to access them until the timer ends.\n\nAre you ready to focus?"
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("START", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("CANCEL")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun StatusCard(
    uiState: HomeUiState,
    onManualRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "App Blocking Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.blockingStatusLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (uiState.isBlocking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Check icon in green circle
            if (uiState.isBlocking) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_circle),
                        contentDescription = "Active",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerCard(
    uiState: HomeUiState,
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onExtendTimer: () -> Unit,
    onShowConfirmDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Focus Timer",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Timer Display
            Text(
                text = uiState.timerDisplay,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (!uiState.isTimerActive) {
                // Duration Selection
                Text(
                    text = "Select Duration",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DurationOptions.forEach { duration ->
                        DurationButton(
                            duration = duration,
                            isSelected = duration == selectedDuration,
                            onClick = { onDurationSelected(duration) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Start Timer Button
                Button(
                    onClick = onShowConfirmDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "START TIMER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // Timer Active - Show Stop button
                Button(
                    onClick = onStopTimer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = "STOP TIMER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationButton(
    duration: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    
    val label = when (duration) {
        30 -> "30MIN"
        60 -> "1HR"
        90 -> "1.5HR"
        120 -> "2HR"
        else -> "${duration}MIN"
    }
    
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun QuickActionsCard(
    onManageApps: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Quick Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Manage App Blocking Button
            Button(
                onClick = onManageApps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "MANAGE APP BLOCKING",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun StudyModeCard(
    isStudyMode: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Study Mode",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = isStudyMode,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF6B8E23),
                        checkedTrackColor = Color(0xFF6B8E23)
                    )
                )
            }
        }
    }
}

@Composable
private fun ManageAppsCard(
    lockedCount: Int,
    unlockedCount: Int,
    onManageApps: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Manage Apps",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Locked Apps",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = lockedCount.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Unlocked Apps",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = unlockedCount.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onManageApps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB388FF)
                )
            ) {
                Text(
                    text = "MANAGE APPS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
