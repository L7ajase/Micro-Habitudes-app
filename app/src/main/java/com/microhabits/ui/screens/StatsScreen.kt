package com.microhabits.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.microhabits.ui.components.StatCard
import com.microhabits.ui.theme.*
import com.microhabits.viewmodel.HabitViewModel
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.statsState.collectAsState()

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(uiState.dailyStats) {
        if (uiState.dailyStats.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                modelProducer.runTransaction {
                    columnSeries { series(uiState.dailyStats.map { it.completedCount }) }
                }
            }
        }
    }

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
                    titleContentColor = Color(0xFFF0EFF8)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = 32.dp, start = 24.dp, end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Stat cards grid ────────────────────────────────────────────
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
                    val level = computeLevel(uiState.totalXp)
                    StatCard("Niveau", "Lv. $level", Modifier.weight(1f))
                }
            }

            // ── Bar chart ──────────────────────────────────────────────────
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Habitudes par jour (30 derniers jours)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (uiState.dailyStats.isNotEmpty()) {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    rememberColumnCartesianLayer(
                                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                            rememberLineComponent(
                                                color = Indigo500,
                                                thickness = 12.dp,
                                                shape = com.patrykandpatrick.vico.core.common.shape.CorneredShape.rounded(4)
                                            )
                                        )
                                    ),
                                    startAxis = rememberStartAxis(),
                                    bottomAxis = rememberBottomAxis()
                                ),
                                modelProducer = modelProducer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        } else {
                            Box(
                                Modifier.fillMaxWidth().height(180.dp),
                                contentAlignment = androidx.compose.ui.Alignment.Center
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

            // ── XP per day table ──────────────────────────────────────────
            if (uiState.dailyStats.isNotEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Historique récent", style = MaterialTheme.typography.titleMedium)
                            uiState.dailyStats.takeLast(7).reversed().forEach { stat ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stat.date, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${stat.completedCount} habit. · ${stat.totalXp} XP",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Indigo300
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun computeLevel(totalXp: Int): Int {
    var xp = totalXp; var level = 1
    while (xp >= level * 100) { xp -= level * 100; level++ }
    return level
}
