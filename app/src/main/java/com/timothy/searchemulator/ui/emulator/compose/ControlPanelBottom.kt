package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.timothy.searchemulator.ui.theme.color
import timber.log.Timber

@Composable
fun BottomControlPanel(
    modifier: Modifier = Modifier,
    viewModel: EmulatorViewModel = hiltViewModel(),
    state:Contract.State
) {
    Column(modifier = modifier) {
        ValueSlideBar(
            enabled = (state.status == Contract.Status.Idle),
            value = getBoardSizeTick(viewModel.currentState.minSideBlockCnt),
            title = "size",
            valueRange = BOARD_SIZE_MIN.toFloat()..BOARD_SIZE_MAX.toFloat(),
            steps = BOARD_SIZE_STEP,
            onValueChange = { viewModel.setEvent(Contract.Event.OnSizeSliderChange(it)) }
        )

        ValueSlideBar(
            value = getMovementSpeedTick(viewModel.currentState.searchProcessDelay),
            title = "speed",
            valueRange = MOVEMENT_SPEED_MIN.toFloat()..MOVEMENT_SPEED_MAX.toFloat(),
            steps = MOVEMENT_SPEED_STEP,
            onValueChange = { viewModel.setEvent(Contract.Event.OnSpeedSliderChange(it)) }
        )
    }
}

@Composable
fun ValueSlideBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
            enabled = enabled,
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