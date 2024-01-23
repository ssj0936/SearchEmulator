package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
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
import com.timothy.searchemulator.ui.emulator.Contract.Event
import com.timothy.searchemulator.ui.emulator.Contract.Status
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.emulator.algo.SearchAlgo
import com.timothy.searchemulator.ui.emulator.algo.SearchBFS
import com.timothy.searchemulator.ui.emulator.algo.SearchStrategy
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme
import com.timothy.searchemulator.ui.theme.color


enum class ControlPanelButtonType {
    TYPE_BUTTON, TYPE_SPACER
}

class ControlPanelButtonWrapper(
    val title: String,
    val icon: Int,
    val iconPressed: Int? = null,
    val type: ControlPanelButtonType = ControlPanelButtonType.TYPE_BUTTON
)

val controlPanelButtonWrappers = listOf(
    ControlPanelButtonWrapper(
        "Start",
        R.drawable.ic_play_24,
        R.drawable.ic_play_circle_pressed_24,
    ),
    ControlPanelButtonWrapper(
        "Pause",
        R.drawable.ic_pause_24,
        R.drawable.ic_pause_circle_pressed_24,
    ),
    ControlPanelButtonWrapper(
        "Stop",
        R.drawable.ic_stop_24,
        R.drawable.ic_stop_circle_pressed_24,
    ),

    ControlPanelButtonWrapper(
        "Undo",
        R.drawable.baseline_undo_24
    ),
    ControlPanelButtonWrapper(
        "Redo",
        R.drawable.baseline_redo_24
    ),
    ControlPanelButtonWrapper(
        "Clean",
        R.drawable.baseline_cleaning_services_24
    ),

    )

//search strategy single-choice buttons
class ToggleButtonOption(
    val title: String,
    val icon: Int? = null,
    val tag: SearchAlgo
)

val searchStrategyButtons = listOf<ToggleButtonOption>(
    ToggleButtonOption("BFS", R.drawable.baseline_search_24, SearchAlgo.SEARCH_BFS),
    ToggleButtonOption("DFS", R.drawable.baseline_search_24, SearchAlgo.SEARCH_DFS)
)

@Composable
fun ControlPanel(
    modifier: Modifier = Modifier,
//    state: Contract.State,
    status: Status,
    currentSearchStrategyType:SearchAlgo
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayStateControlPanel(status = status)
            SegmentedButtons(options = searchStrategyButtons, searchStrategyType = currentSearchStrategyType, enabled = (status == Status.Idle)/*, state = state*/)
        }
    }
}
//
//@Composable
//fun BasicOutlinedButton(
//    onClick: () -> Unit,
//    iconId: Int,
//    borderStrokeWidth: Dp = 1.dp,
//    enabled: Boolean,
//    color: Color
//) {
//    OutlinedButton(
//        onClick = onClick,
//        shape = CircleShape,
//        enabled = enabled,
//        border = ButtonDefaults.outlinedButtonBorder.copy(
//            width = borderStrokeWidth,
//            brush = SolidColor(color)
//        ),
//        contentPadding = PaddingValues(0.dp)
//    ) {
//        Icon(
//            modifier = Modifier.size(16.dp),
//            painter = painterResource(id = iconId),
//            contentDescription = null,
//            tint = color
//
//        )
//    }
//}

@Composable
fun SegmentedButtons(
    modifier: Modifier = Modifier,
    options: List<ToggleButtonOption>,
    borderStrokeWidth: Dp = 1.dp,
    roundedCornerPercent: Int = 50,
//    state: Contract.State,
    searchStrategyType: SearchAlgo,
    enabled:Boolean,
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    Row(modifier) {
        options.forEachIndexed { index, toggleButtonOption ->
            val selected = searchStrategyType/*state.searchStrategy.getType()*/ == toggleButtonOption.tag
            /*val enabled = state.status == Status.Idle*/

            val buttonsModifier = Modifier
                .wrapContentSize()
                .offset(x = if (index == 0) 0.dp else -borderStrokeWidth * index, y = 0.dp)
                .zIndex(if (selected) 1f else 0f)

            val shape: Shape = when (index) {
                0 -> RoundedCornerShape(
                    topStartPercent = roundedCornerPercent,
                    topEndPercent = 0,
                    bottomStartPercent = roundedCornerPercent,
                    bottomEndPercent = 0
                )

                options.lastIndex -> RoundedCornerShape(
                    topStartPercent = 0,
                    topEndPercent = roundedCornerPercent,
                    bottomStartPercent = 0,
                    bottomEndPercent = roundedCornerPercent
                )

                else -> RoundedCornerShape(
                    topStartPercent = 0,
                    topEndPercent = 0,
                    bottomStartPercent = 0,
                    bottomEndPercent = 0
                )
            }

            val border = BorderStroke(
                width = borderStrokeWidth,
                color = if (selected) MaterialTheme.color.buttonOutlinePressedColors else MaterialTheme.color.buttonOutlineColors.copy(
                    alpha = .75f
                )
            )

            val color = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selected) MaterialTheme.color.buttonOutlinePressedColors else Color.Transparent,
                disabledContainerColor = if (selected) MaterialTheme.color.buttonOutlinePressedColors else Color.Transparent,
            )

            val contentColor = if (selected)
                MaterialTheme.color.buttonOutlineContentPressedColors.copy(alpha = if (enabled) 1f else .35f)
            else
                MaterialTheme.color.buttonOutlineContentColors.copy(alpha = if (enabled) 1f else .35f)

            OutlinedButton(
                modifier = buttonsModifier,
                onClick = {
                    viewModel.setEvent(
                        Event.OnSearchStrategyChange(
                            toggleButtonOption.tag
                        )
                    )
                },
                shape = shape,
                border = border,
                colors = color,
                enabled = enabled
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = toggleButtonOption.title,
                        color = contentColor
                    )

                    toggleButtonOption.icon?.let {
                        Icon(
                            painter = painterResource(id = toggleButtonOption.icon),
                            contentDescription = toggleButtonOption.title,
                            tint = contentColor
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun PlayStateControlPanel(
    modifier: Modifier = Modifier,
    status: Status,
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            val buttonModifier = Modifier.padding(horizontal = 4.dp)

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                //start
                ControlPanelButton(
                    modifier = buttonModifier,
                    data = controlPanelButtonWrappers[0],
                    enabled = (status == Status.Idle) || (status == Status.Paused),
                    pressed = status == Status.Started,
                    onClick = { viewModel.setEvent(Event.OnSearchBtnClick) }
                )

                //pause
                ControlPanelButton(
                    modifier = buttonModifier,
                    data = controlPanelButtonWrappers[1],
                    enabled = status == Status.Started,
                    pressed = status == Status.Idle,
                    onClick = { viewModel.setEvent(Event.OnPauseBtnClick) }
                )

                //stop
                ControlPanelButton(
                    modifier = buttonModifier,
                    data = controlPanelButtonWrappers[2],
                    enabled = (status == Status.Started) || (status == Status.SearchFinish) || (status == Status.Paused),
                    pressed = false,
                    onClick = { viewModel.setEvent(Event.OnResetBtnClick) }
                )
            }

            with(MaterialTheme.color.buttonContentColors) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(4.dp)
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            drawCircle(
                                color = this@with,
                                radius = (4 / 2).dp.toPx()
                            )
                        }
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                //undo
                ControlPanelButton(
                    modifier = buttonModifier,
                    data = controlPanelButtonWrappers[3],
                    enabled = (status == Status.Idle),
                    pressed = false,
                    onClick = { viewModel.setEvent(Event.OnBarrierUndoButtonClicked) }
                )

                //redo
                ControlPanelButton(
                    modifier = buttonModifier,
                    data = controlPanelButtonWrappers[4],
                    enabled = (status == Status.Idle),
                    pressed = false,
                    onClick = { viewModel.setEvent(Event.OnBarrierRedoButtonClicked) }
                )

                //clean
                ControlPanelButton(
                    modifier = buttonModifier,
                    data = controlPanelButtonWrappers[5],
                    enabled = (status == Status.Idle),
                    pressed = false,
                    onClick = { viewModel.setEvent(Event.OnBarrierClearButtonClicked) }
                )
            }
        }
    }
}

@Composable
fun ControlPanelButton(
    modifier: Modifier = Modifier,
    data: ControlPanelButtonWrapper,
    pressed: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    when (data.type) {
        ControlPanelButtonType.TYPE_BUTTON -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(6.dp))
                    .alpha(if (enabled) 1f else .5f)
                    .clickable(enabled, onClick = onClick)
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = if (data.iconPressed == null || !pressed) data.icon else data.iconPressed),
                        contentDescription = data.title,
                        tint = MaterialTheme.color.buttonColors
                    )
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.color.buttonColors,
                    )
                }
            }
        }

        ControlPanelButtonType.TYPE_SPACER -> {
            Spacer(
                modifier = Modifier
                    .background(MaterialTheme.color.buttonColors)
                    .width(2.dp)
                    .padding(top = 3.dp, bottom = 3.dp)
                    .fillMaxHeight()
            )
        }
    }
}

fun Modifier.ifThen(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        this.then(modifier())
    } else {
        this
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreviewTop() {
//    SearchEmulatorTheme {
//        ControlPanel(
//            state = Contract.State(
//                status = Status.Idle,
//                minSideBlockCnt = 20,
//                start = Block(3, 5),
//                dest = Block(14, 14),
//                barrier = hashSetOf(),
//                searchStrategy = SearchBFS.instance,
//                searchProcessDelay = getMovementSpeedDelay(MOVEMENT_SPEED_DEFAULT.toFloat())
//
//            )
//        )
//    }
//}