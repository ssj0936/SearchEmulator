package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.R
import com.timothy.searchemulator.model.Description
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorPage(
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            snackBarEffectHandle(effect, scope, snackbarHostState)
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (showBottomSheet) {
            BottomSheetView(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                scope = scope
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ControlPanel(
                    modifier = Modifier
                        .fillMaxWidth(),
                    bottomSheetLauncher = {
                        showBottomSheet = true
                    },
                    coroutineScope = scope
                )
                Spacer(modifier = Modifier.height(12.dp))
                BoardView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                BottomControlPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetView(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    scope: CoroutineScope,
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    val scrollState: ScrollState = rememberScrollState()
    var description by remember{ mutableStateOf<Description?>(null) }
    val animationValue = remember { Animatable(0f) }

    SideEffect {
        description = null
        scope.launch {
            animationValue.snapTo(1f)
            description = viewModel.getAlgoDescription()
            animationValue.animateTo(0f, animationSpec = tween(800))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            IconButton(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .align(Alignment.End),
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismissRequest()
                        }
                    }
                }) {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_close_small_24),
                    contentDescription = ""
                )
            }

            description?.let {algoDesc->
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .weight(1f)
                        .padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
//                        .alpha(1f - animationValue.value)
                        .offset {
                            IntOffset(0, (50 * animationValue.value).toInt())
                        }
                ){
                    Text(
                        text = algoDesc.bigTitle,
                        style = MaterialTheme.typography.headlineLarge
                    )

                    algoDesc.descriptionSets.forEach{descriptionUnit ->
                        Spacer(modifier = Modifier.height(18.dp))
                        descriptionUnit.title?.let {unitTitle->
                            Text(
                                text = unitTitle,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        descriptionUnit.descriptions.forEach { description->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

fun snackBarEffectHandle(
    effect: Contract.Effect,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    if (effect is Contract.Effect.OnSearchFinish) {
        val isSuccess = effect.isSuccess
        scope.launch {
            snackbarHostState.showSnackbar(if (isSuccess) "found path" else "path not found")
        }
    } else if (effect is Contract.Effect.OnBarrierCleaned) {
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