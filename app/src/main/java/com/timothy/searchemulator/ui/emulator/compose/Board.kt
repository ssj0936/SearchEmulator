package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorPage(
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Timber.d("state:$state")

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ControlPanel(state = state)
                BoardView(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                BottomControlPanel(state = state)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SearchEmulatorTheme {
        EmulatorPage()
    }
}