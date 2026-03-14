package com.microhabits.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.microhabits.ui.components.CircularProgressIndicatorCustom
import com.microhabits.ui.theme.*
import com.microhabits.viewmodel.FocusViewModel
import com.microhabits.viewmodel.HabitViewModel

@Composable
fun FocusScreen(
    habitId: Int,
    habitName: String,
    habitEmoji: String,
    durationMinutes: Int,
    onFinish: () -> Unit,
    focusViewModel: FocusViewModel = hiltViewModel(),
    habitViewModel: HabitViewModel = hiltViewModel()
) {
    val state by focusViewModel.state.collectAsState()
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) {
        focusViewModel.startFocus(habitId, habitName, habitEmoji, durationMinutes)
    }

    // Auto-complete when timer hits 0
    LaunchedEffect(state.isFinished) {
        if (state.isFinished && !completed) {
            completed = true
            habitViewModel.toggleHabit(habitId, false)
        }
    }

    // Pulse animation for timer when running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(BgDark),
        contentAlignment = Alignment.Center
    ) {
        // Close button
        IconButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Surface2Dark)
        ) {
            Icon(Icons.Default.Close, "Fermer", tint = Color(0xFFF0EFF8))
        }

        // Reset button
        IconButton(
            onClick = { focusViewModel.reset(); completed = false },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Surface2Dark)
        ) {
            Icon(Icons.Default.Refresh, "Réinitialiser", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Emoji
            Text(
                text = if (state.isFinished) "🏆" else state.habitEmoji,
                fontSize = 72.sp
            )

            // Habit name
            Text(
                text = state.habitName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF0EFF8)
            )

            // Sub label
            Text(
                text = if (state.isFinished) "Terminé ! Bravo 🎉" else "Mode focus — concentre-toi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Big circular timer
            val timerColor = when {
                state.isFinished -> Green400
                state.isRunning -> Indigo500.copy(alpha = if (state.isRunning) pulseAlpha else 1f)
                else -> Surface2Dark
            }

            CircularProgressIndicatorCustom(
                progress = state.progressFraction,
                size = 200.dp,
                strokeWidth = 12.dp,
                progressColor = timerColor,
                trackColor = Surface2Dark
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.formattedTime,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF0EFF8)
                    )
                    Text(
                        text = if (state.isRunning) "En cours" else "En pause",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Action button
            AnimatedContent(
                targetState = when {
                    state.isFinished -> "done"
                    state.isRunning -> "pause"
                    else -> "start"
                },
                label = "btnState"
            ) { btnState ->
                when (btnState) {
                    "done" -> Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(containerColor = Green400),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("✓ Retour à l'accueil", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }

                    "pause" -> Button(
                        onClick = { focusViewModel.toggleTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = Surface2Dark),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("⏸ Pause", fontSize = 16.sp) }

                    else -> Button(
                        onClick = { focusViewModel.toggleTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("▶ Démarrer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                }
            }

            // Tip text
            if (!state.isFinished) {
                Text(
                    text = "💡 Pose ton téléphone et concentre-toi !",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
