package com.microhabits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.microhabits.ui.AppNavGraph
import com.microhabits.ui.theme.MicroHabitsTheme
import com.microhabits.viewmodel.HabitViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.seedIfEmpty()   // seed default habits on first launch
        setContent {
            MicroHabitsTheme {
                AppNavGraph()
            }
        }
    }
}
