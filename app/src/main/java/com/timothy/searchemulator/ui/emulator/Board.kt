package com.timothy.searchemulator.ui.emulator

//import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.R
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorPage(
    viewModel: EmulatorViewModel = hiltViewModel()
){
    val state by viewModel.state.collectAsState()

    Scaffold {paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()){

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ControlPanel(status = state.status)
                BoardView(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .weight(1f)
                        .background(Color.Green))
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
    SideEffect {
        val screenHeight = with(density){ configuration.screenHeightDp.dp.toPx()}
        val screenWidth = with(density){configuration.screenWidthDp.dp.toPx()}
        viewModel.setEvent(Contract.Event.OnScreenMeasured(screenWidth, screenHeight))
    }

    val screenWidth = state.width
    val screenHeight = state.height
    val blockSize = state.blockSize
    val rowsCnt = (screenHeight / blockSize).toInt()
    val columnCnt = (screenWidth / blockSize).toInt()

    Box(modifier = modifier){
        for(i in 0 until rowsCnt){
            
            for(j in 0 until columnCnt){

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