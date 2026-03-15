package com.microhabits.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.microhabits.data.model.DailyStats
import com.microhabits.ui.components.StatCard
import com.microhabits.ui.theme.*
import com.microhabits.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.statsState.collectAsState()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Statistiques", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark,
                    titleContentColor = Color(0xFFF0EFF8),
                    navigationIconContentColor = Color(0xFFF0EFF8)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = 32.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("XP Total", "${uiState.totalXp} XP", Modifier.weight(1f))
                    StatCard("Jours actifs", "${uiState.totalActiveDays}", Modifier.weight(1f))
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Complétions", "${uiState.totalCompletions}", Modifier.weight(1f))
                    StatCard("Niveau", "Lv. ${computeLevel(uiState.totalXp)}", Modifier.weight(1f))
                }
            }

            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Habitudes / jour (30 derniers jours)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (uiState.dailyStats.isNotEmpty()) {
                            SimpleBarChart(
                                data = uiState.dailyStats,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        } else {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Complete des habitudes pour voir tes stats !",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.dailyStats.isNotEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Historique récent",
                                style = MaterialTheme.typography.titleMedium
                            )
                            uiState.dailyStats.takeLast(7).reversed().forEach { stat ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        stat.date,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "${stat.completedCount} habit. · ${stat.totalXp} XP",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Indigo300
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleBarChart(
    data: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.completedCount }
        ?.toFloat()
        ?.coerceAtLeast(1f) ?: 1f

    Canvas(modifier) {
        val totalBars = data.size
        val barWidth = (size.width / totalBars) * 0.6f
        val gap = (size.width / totalBars) * 0.4f
        val chartHeight = size.height - 16.dp.toPx()

        data.forEachIndexed { i, stat ->
            val x = i * (barWidth + gap) + gap / 2f
            val barH = (stat.completedCount / maxVal) * chartHeight

            drawRoundRect(
                color = Surface2Dark,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            if (barH > 0f) {
                drawRoundRect(
                    color = Indigo500,
                    topLeft = Offset(x, chartHeight - barH),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
        }
    }
}

private fun computeLevel(totalXp: Int): Int {
    var xp = totalXp
    var level = 1
    while (xp >= level * 100) {
        xp -= level * 100
        level++
    }
    return level
}