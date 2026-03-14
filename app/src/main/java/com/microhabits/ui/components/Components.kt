package com.microhabits.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microhabits.data.model.HabitWithStatus
import com.microhabits.ui.theme.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas

// ── Circular Progress ─────────────────────────────────────────────────────────

@Composable
fun CircularProgressIndicatorCustom(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = Surface2Dark,
    progressColor: Color = Indigo500,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "circularProgress"
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(trackColor,    startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
            drawArc(progressColor, startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false, style = stroke)
        }
        content()
    }
}

// ── XP Progress Bar ───────────────────────────────────────────────────────────

@Composable
fun XpProgressBar(
    current: Int,
    max: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    val fraction = if (max > 0) current.toFloat() / max else 0f
    val animFraction by animateFloatAsState(fraction, tween(700), label = "xpBar")

    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Niveau $level", style = MaterialTheme.typography.labelLarge, color = Indigo300)
            Text("$current / $max XP", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Surface2Dark)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Indigo500, Pink400)
                        )
                    )
            )
        }
    }
}

// ── Habit Card (drag + complete) ──────────────────────────────────────────────

@Composable
fun HabitCard(
    item: HabitWithStatus,
    onToggle: () -> Unit,
    onFocusClick: () -> Unit,
    dragModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    val habit = item.habit
    val accentColor = runCatching {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    }.getOrDefault(Indigo500)

    var bounceScale by remember { mutableFloatStateOf(1f) }
    val scale by animateFloatAsState(bounceScale, spring(Spring.DampingRatioMediumBouncy), label = "bounce")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Accent bar
            Box(
                Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )

            // Emoji icon
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.emoji, fontSize = 22.sp)
            }

            // Text info
            Column(Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.completedToday)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${habit.durationMinutes} min · +${habit.xpReward} XP" +
                            if (item.streakDays > 1) " · 🔥${item.streakDays}" else "",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Focus button
            TextButton(
                onClick = onFocusClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("▶", color = accentColor, fontSize = 13.sp)
            }

            // Checkmark
            val checkBg = if (item.completedToday) Green400 else Color.Transparent
            val checkBorder = if (item.completedToday) Green400 else MaterialTheme.colorScheme.outline

            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(checkBg)
                    .border(1.5.dp, checkBorder, CircleShape)
                    .clickable {
                        bounceScale = 0.85f
                        onToggle()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (item.completedToday) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(80)
                        bounceScale = 1.2f
                        kotlinx.coroutines.delay(120)
                        bounceScale = 1f
                    }
                }
            }

            // Drag handle
            Icon(
                Icons.Default.DragHandle, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragModifier.size(20.dp)
            )
        }
    }
}

// ── Stat card ─────────────────────────────────────────────────────────────────

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface2Dark)
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}
