package com.disciplinedminds.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
// Home screen redesigned to be minimalist & premium
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disciplinedminds.R
import com.disciplinedminds.ui.components.bounceClick
import kotlin.math.cos
import kotlin.math.sin

private val DurationOptions = listOf(30, 60, 90, 120)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onExtendTimer: () -> Unit,
    onToggleStudyMode: (Boolean) -> Unit, // kept for future use
    onManageApps: () -> Unit,
    onManualRefresh: () -> Unit // kept for future use
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Premium gradient background
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1a1a2e),
            Color(0xFF16213e),
            Color(0xFF0f3460)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background with gradient and circular accents
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Base gradient
            drawRect(brush = gradient)
            
            // Circular accent shapes for depth
            drawCircle(
                color = Color(0x20DC2626),
                radius = 150.dp.toPx(),
                center = Offset(size.width + 100.dp.toPx(), -100.dp.toPx())
            )
            
            drawCircle(
                color = Color(0x15EF4444),
                radius = 200.dp.toPx(),
                center = Offset(-150.dp.toPx(), size.height + 150.dp.toPx())
            )
            
            drawCircle(
                color = Color(0x10DC2626),
                radius = 100.dp.toPx(),
                center = Offset(-50.dp.toPx(), 100.dp.toPx())
            )
            
            // Overlay gradient for texture
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x20DC2626),
                        Color(0x00DC2626),
                        Color(0x30EF4444)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }
        
        // Content overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand title minimal
            Text(
                text = "DisciplinedMinds",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (uiState.isTimerActive) "Focused session in progress" else "Select a duration & begin",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(28.dp))
            CircularTimerCard(
                uiState = uiState,
                selectedDuration = selectedDuration,
                onDurationSelected = onDurationSelected,
                onStartTimer = { showConfirmDialog = true },
                onStopTimer = onStopTimer,
                onExtendTimer = onExtendTimer
            )
            Spacer(modifier = Modifier.height(28.dp))
            ManageAppsAction(onManageApps)
            Spacer(modifier = Modifier.height(12.dp))
        }
        }
    }
    
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
private fun CircularTimerCard(
    uiState: HomeUiState,
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onExtendTimer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = if (uiState.isTimerActive) "FOCUS MODE" else "READY TO FOCUS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Circular Progress
            CircularTimer(
                timeText = uiState.timerDisplay,
                progress = calculateProgress(uiState),
                isActive = uiState.isTimerActive
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (!uiState.isTimerActive) {
                // Duration Selection
                Text(
                    text = "Choose your focus duration",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DurationOptions.forEach { duration ->
                        ModernDurationButton(
                            duration = duration,
                            isSelected = duration == selectedDuration,
                            onClick = { onDurationSelected(duration) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Start Button
                Button(
                    onClick = onStartTimer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_timer),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "START FOCUS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                // Timer Active Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Extend Button
                    Button(
                        onClick = onExtendTimer,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .bounceClick(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "+15",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Stop Button
                    Button(
                        onClick = onStopTimer,
                        modifier = Modifier
                            .weight(2f)
                            .height(56.dp)
                            .bounceClick(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STOP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularTimer(
    timeText: String,
    progress: Float,
    isActive: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background circle
            drawCircle(
                color = surfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc
            if (isActive && animatedProgress > 0) {
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                )
            }
        }
        
        // Time text in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                letterSpacing = 2.sp
            )
            if (isActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "remaining",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun ModernDurationButton(
    duration: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    
    val label = when (duration) {
        30 -> "30m"
        60 -> "1h"
        90 -> "1.5h"
        120 -> "2h"
        else -> "${duration}m"
    }
    
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor
        )
    }
}

private fun calculateProgress(uiState: HomeUiState): Float {
    if (!uiState.isTimerActive || uiState.totalDurationMinutes == 0) return 0f
    
    val remainingSeconds = (uiState.remainingTimeMillis / 1000).toFloat()
    val totalSeconds = (uiState.totalDurationMinutes * 60).toFloat()
    
    return if (totalSeconds > 0) {
        (remainingSeconds / totalSeconds).coerceIn(0f, 1f)
    } else {
        0f
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
                        .height(56.dp)
                        .bounceClick(),
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
private fun ManageAppsAction(onManageApps: () -> Unit) {
    Button(
        onClick = onManageApps,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .bounceClick(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_lock),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "MANAGE APPS",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

