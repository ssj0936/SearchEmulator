package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import com.timothy.searchemulator.model.MOVEMENT_SPEED_MAX
import com.timothy.searchemulator.model.MOVEMENT_SPEED_MIN
import com.timothy.searchemulator.model.getMovementSpeedTick
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel

@Composable
fun BottomControlPanel(
    modifier: Modifier = Modifier,
    state: Contract.State,
    viewModel: EmulatorViewModel = hiltViewModel(),
){
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)) {
        ValueSlideBar(
            enabled = (viewModel.currentState.status== Contract.Status.Idle),
            value = state.minSideBlockCnt.toFloat(),
            title = "size",
            valueRange = 10f..40f,
            steps = 2,
            onValueChange = {viewModel.setEvent(Contract.Event.OnSizeSliderChange(it))}
        )

        ValueSlideBar(
            value = getMovementSpeedTick(viewModel.currentState.searchProcessDelay),
            title = "speed",
            valueRange = MOVEMENT_SPEED_MIN.toFloat()..MOVEMENT_SPEED_MAX.toFloat(),
            steps = 10,
            onValueChange = {viewModel.setEvent(Contract.Event.OnSpeedSliderChange(it))}
        )
    }
}

@Composable
fun ValueSlideBar(
    modifier: Modifier = Modifier,
    enabled: Boolean=true,
    value:Float,
    title:String,
    valueRange:ClosedFloatingPointRange<Float>,
    steps:Int,
    onValueChange:(Float)->Unit
){
    var sliderPosition by remember { mutableFloatStateOf(value) }

    Row(
        modifier = modifier,

        verticalAlignment = Alignment.CenterVertically
    ){
        //title
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium
        )

        Slider(
            enabled = enabled,
            value = sliderPosition,
            valueRange = valueRange,
            steps = steps,
            onValueChange = {
                sliderPosition = it
                onValueChange(it)
            }
        )
    }
}