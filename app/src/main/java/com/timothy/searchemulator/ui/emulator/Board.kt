package com.timothy.searchemulator.ui.emulator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.R
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

const val REFRESH_RATE = 600L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorPage(
    viewModel: EmulatorViewModel = hiltViewModel()
){
    val state by viewModel.state.collectAsState()

//    LaunchedEffect(key1 = Unit){
//        while (isActive){
//            viewModel.refresh()
//            delay(REFRESH_RATE)
//        }
//    }


    Scaffold {paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .windowInsetsPadding(WindowInsets.navigationBars)){

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ControlPanel(status = state.status)
                BoardView(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize())
            }
        }
    }
}

const val ID_BUTTON_START = 0
const val ID_BUTTON_PAUSE = 1
const val ID_BUTTON_STOP = 2

class ControlPanelButtonWrapper(
    val title: String,
    val icon: Int,
    val iconPressed: Int,
    val id:Int
)
val controlPanelButtonWrappers = listOf(
    ControlPanelButtonWrapper("Start", R.drawable.ic_play_24, R.drawable.ic_play_circle_pressed_24, ID_BUTTON_START),
    ControlPanelButtonWrapper("Pause", R.drawable.ic_pause_24, R.drawable.ic_pause_circle_pressed_24, ID_BUTTON_PAUSE),
    ControlPanelButtonWrapper("Stop", R.drawable.ic_stop_24, R.drawable.ic_stop_circle_pressed_24, ID_BUTTON_STOP)
)

@Composable
fun ControlPanelButton(
    modifier: Modifier = Modifier,
    data: ControlPanelButtonWrapper,
    pressed:Boolean = false,
    enabled: Boolean = true,
    onClick:()->Unit
){
    Box(modifier = modifier
        .alpha(if (enabled) 1f else .7f)
        .clickable(enabled, onClick = onClick)
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = if(pressed) data.iconPressed else data.icon),
                contentDescription = data.title
            )

            Text(
                text = data.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
fun ControlPanel(
    modifier: Modifier = Modifier,
    status: Contract.Status,
    viewModel: EmulatorViewModel = hiltViewModel()
){
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ){
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,

        ){
            //start
            ControlPanelButton(
                data = controlPanelButtonWrappers[0],
                enabled = status == Contract.Status.Idle,
                pressed = status == Contract.Status.Started,
                onClick = {viewModel.setEvent(Contract.Event.OnSearchBtnClick)}
            )

            //pause
            ControlPanelButton(
                data = controlPanelButtonWrappers[1],
                enabled = status == Contract.Status.Started,
                pressed = status == Contract.Status.Idle,
                onClick = {viewModel.setEvent(Contract.Event.OnPauseBtnClick)}
            )

            //stop
            ControlPanelButton(
                data = controlPanelButtonWrappers[2],
                enabled = status == Contract.Status.Started,
                pressed = false,
                onClick = {viewModel.setEvent(Contract.Event.OnResetBtnClick)}
            )
        }
    }
}

@Composable
fun BoardView(
    modifier: Modifier,
    state:Contract.State,
    viewModel: EmulatorViewModel = hiltViewModel()
){
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    var availableW by remember{ mutableStateOf(0) }
    var availableH by remember{ mutableStateOf(0) }
//    SideEffect {
////        val statusBarHeight = with (density) { LocalWindowInsets.current.statusBars.top.toDp() }
////
//        val screenHeight = with(density){ configuration.screenHeightDp.dp.toPx()}
//        val screenWidth = with(density){configuration.screenWidthDp.dp.toPx()}
//        viewModel.setEvent(Contract.Event.OnScreenMeasured(screenWidth, screenHeight))
//    }

        val blockSize = state.blockSize
        val matrixW = state.matrixW
        val matrixH = state.matrixH
        Box(modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                if (coordinates.size.width * coordinates.size.height != availableW * availableH) {
                    availableW = coordinates.size.width
                    availableH = coordinates.size.height
                    viewModel.setEvent(
                        Contract.Event.OnScreenMeasured(
                            availableW,
                            availableH
                        )
                    )
                }
            }) {
            Canvas(modifier = Modifier.fillMaxSize()){
                drawBackground(blockSize, matrixW, matrixH)
            }
        }

}

fun DrawScope.drawBackground(brickSize:Int, matrixW:Int, matrixH:Int){
    (0 until matrixW).forEach { x->
        (0 until matrixH).forEach { y->
            drawUnitBlock(brickSize, x, y)
        }
    }
}

fun DrawScope.drawUnitBlock(brickSize:Int, x:Int, y:Int, color: Color= Color.Black){
    val absoluteOffset = Offset(brickSize*x.toFloat(), brickSize*y.toFloat())
    val padding  = brickSize * 0.05f
    val outerSize = brickSize - padding*2
    drawRect(
        color = color,
        topLeft = absoluteOffset+ Offset(padding, padding),
        size= Size(outerSize, outerSize),
        style = Stroke(outerSize / 30)
    )
//    drawPoints(points = listOf(absoluteOffset), pointMode = PointMode.Points, color= Color.Red,strokeWidth = 10f,
//        cap = StrokeCap.Round)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SearchEmulatorTheme {
        EmulatorPage()
    }
}