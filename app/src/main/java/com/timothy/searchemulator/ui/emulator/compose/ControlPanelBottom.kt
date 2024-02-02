package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.R
import com.timothy.searchemulator.model.BOARD_SIZE_MAX
import com.timothy.searchemulator.model.BOARD_SIZE_MIN
import com.timothy.searchemulator.model.BOARD_SIZE_STEP
import com.timothy.searchemulator.model.MOVEMENT_SPEED_MAX
import com.timothy.searchemulator.model.MOVEMENT_SPEED_MIN
import com.timothy.searchemulator.model.MOVEMENT_SPEED_STEP
import com.timothy.searchemulator.model.getBoardSizeTick
import com.timothy.searchemulator.model.getMovementSpeedTick
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme
import com.timothy.searchemulator.ui.theme.color

@Composable
fun BottomControlPanel(
    modifier: Modifier = Modifier,
    viewModel: EmulatorViewModel = hiltViewModel(),
) {
    val status by viewModel.status.collectAsState()

    Box(modifier = modifier) {
        Column {
            RangeChooser(
                modifier = Modifier.weight(1f),
                enabled = { status == Contract.Status.Idle },
                value = 5,
                title = "size",
                valueRange = 1..9,
                steps = BOARD_SIZE_STEP,
                onValueChange = {}
            )
            Row {
                ValueSlideBar(
                    modifier = Modifier.weight(1f),
                    enabled = { status == Contract.Status.Idle },
                    value = getBoardSizeTick(viewModel.currentState.minSideBlockCnt),
                    title = "size",
                    valueRange = BOARD_SIZE_MIN.toFloat()..BOARD_SIZE_MAX.toFloat(),
                    steps = BOARD_SIZE_STEP,
                    onValueChange = { viewModel.setEvent(Contract.Event.OnSizeSliderChange(it)) }
                )

                ValueSlideBar(
                    modifier = Modifier.weight(1f),
                    value = getMovementSpeedTick(viewModel.currentState.searchProcessDelay),
                    title = "speed",
                    valueRange = MOVEMENT_SPEED_MIN.toFloat()..MOVEMENT_SPEED_MAX.toFloat(),
                    steps = MOVEMENT_SPEED_STEP,
                    onValueChange = { viewModel.setEvent(Contract.Event.OnSpeedSliderChange(it)) }
                )
            }
        }
    }
}

class RangeChooserData(
    val value:Int,
    val displayContent:String
)

val boardSizeRange = listOf<RangeChooserData>(
    RangeChooserData(0, "Normal Board"),
    RangeChooserData(1, "Big Board"),
    RangeChooserData(2, "Large Size"),
)

val speedRange = listOf<RangeChooserData>(
    RangeChooserData(0, "Slowest"),
    RangeChooserData(1, "Slow"),
    RangeChooserData(2, "Normal"),
    RangeChooserData(3, "Fast"),
    RangeChooserData(4, "Fast as fxxx"),
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RangeChooser(
    modifier: Modifier = Modifier,
    enabled: ()->Boolean = { true },
    value: Int = 0,
    valueRange:List<RangeChooserData>,
    onValueChange: (Int) -> Unit
){
    var currentValue by remember { mutableIntStateOf(value) }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_minus_24),
            contentDescription = null,
            tint = MaterialTheme.color.buttonColors,
            modifier = Modifier
                .size(14.dp)
                .alpha(if (enabled() && currentValue > valueRange.first().value) 1f else .5f)
                .clickable((enabled() && currentValue > valueRange.first().value), onClick = {
                    onValueChange(--currentValue)
                })
        )
        AnimatedContent(targetState = currentValue, label = "") { targetCount ->
            // Make sure to use `targetCount`, not `count`.
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.color.buttonColors,
                text = "$targetCount"
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.baseline_plus_24),
            contentDescription = null,
            tint = MaterialTheme.color.buttonColors,
            modifier = Modifier
                .size(14.dp)
                .alpha(if (enabled() && currentValue < valueRange.last().value) 1f else .5f)
                .clickable((enabled() && currentValue < valueRange.last().value), onClick = {
                    onValueChange(++currentValue)
                })
        )
    }
}

@Composable
fun ValueSlideBar(
    modifier: Modifier = Modifier,
    enabled: ()->Boolean = { true },
    value: Float,
    title: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(value) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //title
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.width(12.dp))

        Slider(
            enabled = enabled(),
            value = sliderPosition,
            valueRange = valueRange,
            steps = steps,
            onValueChange = {
                sliderPosition = it
                onValueChange(it)
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.color.sliderThumbColors,
                activeTrackColor = MaterialTheme.color.sliderTrackColors,
                inactiveTrackColor = MaterialTheme.color.sliderInactiveTrackColors
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ValueSlideBarPreview(){
    SearchEmulatorTheme {
        RangeChooser(
            modifier = Modifier,
            enabled = {true},
            value = 5,
            valueRange = boardSizeRange,
            onValueChange = {}
        )
    }
}