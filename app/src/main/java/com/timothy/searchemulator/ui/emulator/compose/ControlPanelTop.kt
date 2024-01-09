package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.R
import com.timothy.searchemulator.model.MOVEMENT_SPEED_DEFAULT
import com.timothy.searchemulator.model.getMovementSpeedDelay
import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.emulator.algo.SearchAlgo
import com.timothy.searchemulator.ui.emulator.algo.SearchBFS
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme

const val ID_BUTTON_START = 0
const val ID_BUTTON_PAUSE = 1
const val ID_BUTTON_STOP = 2

class ControlPanelButtonWrapper(
    val title: String,
    val icon: Int,
    val iconPressed: Int,
    val id: Int
)

val controlPanelButtonWrappers = listOf(
    ControlPanelButtonWrapper(
        "Start",
        R.drawable.ic_play_24,
        R.drawable.ic_play_circle_pressed_24,
        ID_BUTTON_START
    ),
    ControlPanelButtonWrapper(
        "Pause",
        R.drawable.ic_pause_24,
        R.drawable.ic_pause_circle_pressed_24,
        ID_BUTTON_PAUSE
    ),
    ControlPanelButtonWrapper(
        "Stop",
        R.drawable.ic_stop_24,
        R.drawable.ic_stop_circle_pressed_24,
        ID_BUTTON_STOP
    )
)

@Composable
fun ControlPanel(
    modifier: Modifier = Modifier,
    state: Contract.State,
//    viewModel: EmulatorViewModel = hiltViewModel()
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayStateControlPanel(status = state.status)
            ToggleButtons(options = searchStrategyButtons, state = state)
            SegmentedButtons(options = searchStrategyButtons, state = state)
        }
    }
}

@Composable
fun SegmentedButtons(
    modifier: Modifier = Modifier,
    options: List<ToggleButtonOption>,
    borderStrokeWidth: Dp = 1.dp,
    state:Contract.State,
    viewModel: EmulatorViewModel = hiltViewModel()
){
    Row(modifier) {
        options.forEachIndexed { index, toggleButtonOption ->
            val selected = state.searchStrategy.getType() == toggleButtonOption.tag

            val buttonsModifier = Modifier
                .wrapContentSize()
                .offset(x = if (index == 0) 0.dp else borderStrokeWidth * -1 * index, y = 0.dp)
                .zIndex(if (selected) 1f else 0f)

            val shape:Shape = when(index){
                0->RoundedCornerShape(
                    topStartPercent = 10,
                    topEndPercent = 0,
                    bottomStartPercent = 10,
                    bottomEndPercent = 0
                )

                options.lastIndex -> RoundedCornerShape(
                    topStartPercent = 0,
                    topEndPercent = 10,
                    bottomStartPercent = 0,
                    bottomEndPercent = 10
                )

                else->RoundedCornerShape(
                    topStartPercent = 0,
                    topEndPercent = 0,
                    bottomStartPercent = 0,
                    bottomEndPercent = 0
                )
            }

            val border = BorderStroke(
                width = borderStrokeWidth,
                color = if(selected) Color.DarkGray else Color.DarkGray.copy(alpha = .75f)
            )

            val color = ButtonDefaults.outlinedButtonColors(
                containerColor = if(selected) Color.LightGray else Color.Transparent
            )

            OutlinedButton(
                modifier = buttonsModifier,
                onClick = {viewModel.setEvent(Contract.Event.OnSearchStrategyChange(toggleButtonOption.tag))},
                shape = shape,
                border = border,
                colors = color
            ){
                Text(
                    text = toggleButtonOption.title
                )
            }
        }
    }

}

@Composable
fun PlayStateControlPanel(
    modifier: Modifier = Modifier,
    status: Contract.Status,
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,

            ) {
            //start
            ControlPanelButton(
                data = controlPanelButtonWrappers[0],
                enabled = (status == Contract.Status.Idle) || (status == Contract.Status.Paused),
                pressed = status == Contract.Status.Started,
                onClick = { viewModel.setEvent(Contract.Event.OnSearchBtnClick) }
            )

            //pause
            ControlPanelButton(
                data = controlPanelButtonWrappers[1],
                enabled = status == Contract.Status.Started,
                pressed = status == Contract.Status.Idle,
                onClick = { viewModel.setEvent(Contract.Event.OnPauseBtnClick) }
            )

            //stop
            ControlPanelButton(
                data = controlPanelButtonWrappers[2],
                enabled = (status == Contract.Status.Started) || (status == Contract.Status.SearchFinish) || (status == Contract.Status.Paused),
                pressed = false,
                onClick = { viewModel.setEvent(Contract.Event.OnResetBtnClick) }
            )
        }
    }
}

@Composable
fun ControlPanelButton(
    modifier: Modifier = Modifier,
    data: ControlPanelButtonWrapper,
    pressed: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else .7f)
            .clickable(enabled, onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = if (pressed) data.iconPressed else data.icon),
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

class ToggleButtonOption(
    val title: String,
    val icon:Int? = null,
    val tag:SearchAlgo
)

val searchStrategyButtons = listOf<ToggleButtonOption>(
    ToggleButtonOption("BFS", R.drawable.baseline_search_24, SearchAlgo.SEARCH_BFS),
    ToggleButtonOption("DFS", R.drawable.baseline_search_24, SearchAlgo.SEARCH_DFS)
)

@Composable
fun ToggleButton(
    modifier: Modifier = Modifier,
    toggleButtonOption:ToggleButtonOption,
    selected:Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
){
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(0),
        colors = ButtonDefaults.buttonColors(
            containerColor = if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
        contentPadding = PaddingValues(0.dp),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = toggleButtonOption.title,
                style = MaterialTheme.typography.titleMedium,
                color = if(selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )

            if(toggleButtonOption.icon!=null){
                Icon(
                    painter = painterResource(id = toggleButtonOption.icon),
                    contentDescription = toggleButtonOption.title
                )
            }
        }
    }
}

@Composable
fun ToggleButtons(
    modifier: Modifier = Modifier,
    options:List<ToggleButtonOption>,
    state:Contract.State,
    viewModel: EmulatorViewModel = hiltViewModel(),
){
    if(options.isEmpty())
        return

    Box(
//        border = BorderStroke(1.dp, Color.DarkGray),
//        shape = RoundedCornerShape(50),
        modifier = modifier
    ){
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            options.forEach {
                ToggleButton(
                    toggleButtonOption = it ,
                    selected = state.searchStrategy.getType() == it.tag,
                    enabled = state.status==Contract.Status.Idle
                ) {
                    viewModel.setEvent(Contract.Event.OnSearchStrategyChange(it.tag))
                }
            }
        }
    }
}

fun Modifier.ifThen(condition:Boolean, modifier:Modifier.()->Modifier):Modifier{
    return if(condition){
        this.then(modifier())
    }else{
        this
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreviewTop() {
    SearchEmulatorTheme {
        ControlPanel(state = Contract.State(
            status = Contract.Status.ConditionsMissing,
            minSideBlockCnt = 20,
            start = Block(3, 5),
            dest = Block(14, 14),
            barrier = listOf(
                Block(3,2), Block(2,2), Block(1,4),
                Block(6,2), Block(7,3), Block(7,4), Block(7,5), Block(8,6), Block(9,7),Block(9,8),Block(9,9),
                Block(2,10),Block(3,10),Block(4,10),Block(5,10),Block(7,10),Block(8,10),
                Block(12,11),Block(13,11),Block(14,11),Block(12,12),Block(12,13),Block(12,14),Block(12,15),Block(11,16),Block(10,17),Block(10,18),
            ),
            searchStrategy = SearchBFS(),
            searchProcessDelay = getMovementSpeedDelay(MOVEMENT_SPEED_DEFAULT.toFloat())

        ))
    }
}