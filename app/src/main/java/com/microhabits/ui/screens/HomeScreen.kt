package com.microhabits.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.microhabits.data.model.HabitWithStatus
import com.microhabits.ui.components.*
import com.microhabits.ui.theme.*
import com.microhabits.viewmodel.HabitViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFocus: (Int, String, String, Int) -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.homeState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Keep a local mutable list so reorder is instant (optimistic UI)
    var habitList by remember(uiState.habitsWithStatus) {
        mutableStateOf(uiState.habitsWithStatus)
    }

    val reorderState = rememberReorderableLazyColumnState(
        onMove = { from, to ->
            habitList = habitList.toMutableList().apply { add(to.index, removeAt(from.index)) }
        },
        onDragEnd = { _, _ ->
            viewModel.reorderHabits(habitList.map { it.habit })
        }
    )

    // Reward snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    var lastDoneCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.completedCount) {
        if (uiState.completedCount > lastDoneCount && lastDoneCount >= 0) {
            val msg = when {
                uiState.completedCount == uiState.totalCount && uiState.totalCount > 0 ->
                    "🏆 Toutes les habitudes complétées ! +${uiState.habitsWithStatus.lastOrNull()?.habit?.xpReward ?: 0} XP"
                else -> rewardMessages[uiState.completedCount % rewardMessages.size]
            }
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
        lastDoneCount = uiState.completedCount
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    data, modifier = Modifier.padding(16.dp),
                    containerColor = Surface2Dark,
                    contentColor = Color(0xFFF0EFF8),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Indigo500,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.Add, "Ajouter") }
        }
    ) { padding ->
        LazyColumn(
            state = reorderState.lazyListState,
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = greeting(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color(0xFFF0EFF8)
                            )
                            Text(
                                formattedDate(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = onNavigateToStats) {
                            Icon(Icons.Default.EmojiEvents, "Stats", tint = Amber400)
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // ── Circular progress + info ───────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CircularProgressIndicatorCustom(
                            progress = uiState.progressPercent,
                            size = 120.dp,
                            progressColor = when {
                                uiState.progressPercent >= 1f -> Green400
                                uiState.progressPercent >= 0.5f -> Indigo500
                                else -> Pink400
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${(uiState.progressPercent * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("du jour", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "${uiState.completedCount}/${uiState.totalCount} habitudes",
                                style = MaterialTheme.typography.titleMedium
                            )
                            XpProgressBar(
                                current = uiState.xpInCurrentLevel,
                                max = uiState.xpToNextLevel,
                                level = uiState.xpLevel,
                                modifier = Modifier.width(180.dp)
                            )
                            AnimatedVisibility(uiState.progressPercent >= 1f) {
                                Text("🏆 Journée parfaite !", color = Amber400, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        "MES HABITUDES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // ── Habit cards (reorderable) ──────────────────────────────────
            items(habitList, key = { it.habit.id }) { item ->
                ReorderableItem(reorderState, key = item.habit.id) { isDragging ->
                    HabitCard(
                        item = item,
                        onToggle = { viewModel.toggleHabit(item.habit.id, item.completedToday) },
                        onFocusClick = {
                            onNavigateToFocus(
                                item.habit.id, item.habit.name,
                                item.habit.emoji, item.habit.durationMinutes
                            )
                        },
                        dragModifier = Modifier.draggableHandle(),
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .animateItem()
                    )
                }
            }

            if (habitList.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Appuie sur + pour ajouter ta première habitude !",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, emoji, duration, xp, color ->
                viewModel.addHabit(name, emoji, duration, xp, color)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("⭐") }
    var duration by remember { mutableIntStateOf(5) }
    var xp by remember { mutableIntStateOf(30) }
    val colors = listOf("#6C63FF","#2ECF8A","#FF6584","#FFB547","#9B59B6","#4FC3F7")
    var selectedColor by remember { mutableStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Nouvelle habitude") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nom de l'habitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = emoji, onValueChange = { emoji = it },
                        label = { Text("Emoji") },
                        modifier = Modifier.width(80.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text("Durée : $duration min", style = MaterialTheme.typography.bodySmall)
                        Slider(value = duration.toFloat(), onValueChange = { duration = it.toInt() },
                            valueRange = 1f..30f, colors = SliderDefaults.colors(thumbColor = Indigo500, activeTrackColor = Indigo500))
                    }
                }
                Column {
                    Text("XP : $xp", style = MaterialTheme.typography.bodySmall)
                    Slider(value = xp.toFloat(), onValueChange = { xp = it.toInt() },
                        valueRange = 10f..100f, colors = SliderDefaults.colors(thumbColor = Green400, activeTrackColor = Green400))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Indigo500)
                        Box(
                            Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(c)
                                .clickable { selectedColor = hex }
                                .then(if (selectedColor == hex) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, emoji, duration, xp, selectedColor) },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
            ) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Bonjour ☀️"
        hour < 18 -> "Bon après-midi 🌤"
        else -> "Bonsoir 🌙"
    }
}

private fun formattedDate(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH))
        .replaceFirstChar { it.uppercase() }

private val rewardMessages = listOf(
    "⚡ +XP ! Belle progression !",
    "🌟 Excellent ! Continue !",
    "💪 Habitude validée !",
    "🎯 Dans le mille !",
    "🔥 Tu es en feu !"
)

// Extension nécessaire pour le border dans le dialog
private fun Modifier.border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape) =
    this.then(androidx.compose.foundation.border(width, color, shape))
private fun Modifier.clickable(onClick: () -> Unit) =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
