package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.compose.runtime.rememberCoroutineScope as rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorPage(
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 =Unit){
        viewModel.effect.collect{effect->
            effectHandle(effect, scope, snackbarHostState)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ControlPanel(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                BoardView(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
//                Spacer(modifier = Modifier.height(8.dp))
                BottomControlPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }
        }
    }
}

fun effectHandle(
    effect: Contract.Effect,
    scope:CoroutineScope,
    snackbarHostState:SnackbarHostState
) = when(effect){
    is Contract.Effect.OnSearchFinish ->{
        val isSuccess = effect.isSuccess
        scope.launch {
            snackbarHostState.showSnackbar(if(isSuccess) "found path" else "path not found")
        }
    }

    is Contract.Effect.OnBarrierCleaned->{
        scope.launch {
            snackbarHostState.showSnackbar("Barrier cleaned")
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