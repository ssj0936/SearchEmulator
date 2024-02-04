package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.R
import com.timothy.searchemulator.model.RangeChooserData
import com.timothy.searchemulator.model.boardSizeRange
import com.timothy.searchemulator.model.getBoardSizeTick
import com.timothy.searchemulator.model.getMovementSpeedTick
import com.timothy.searchemulator.model.speedRange
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme
import com.timothy.searchemulator.ui.theme.color
import kotlin.math.roundToInt

@Composable
fun BottomControlPanel(
    modifier: Modifier = Modifier,
    viewModel: EmulatorViewModel = hiltViewModel(),
) {
    val status by viewModel.status.collectAsState()

    Row(modifier = modifier) {
        RangeChooser(
            modifier = Modifier,
            enabled = { status == Contract.Status.Idle },
            value = getBoardSizeTick(viewModel.currentState.minSideBlockCnt),
            valueRange = boardSizeRange,
            onValueChange = { viewModel.setEvent(Contract.Event.OnSizeSliderChange(it)) }
        )
        Spacer(modifier = Modifier.weight(1f))
        RangeChooser(
            modifier = Modifier,
            value = getMovementSpeedTick(viewModel.currentState.searchProcessDelay).roundToInt(),
            valueRange = speedRange,
            onValueChange = { viewModel.setEvent(Contract.Event.OnSpeedSliderChange(it)) }
        )
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RangeChooser(
    modifier: Modifier = Modifier,
    enabled: () -> Boolean = { true },
    value: Int = 0,
    valueRange: List<RangeChooserData>,
    onValueChange: (Int) -> Unit,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge
) {
    //measure max width
    var componentMaxWidth by remember{ mutableStateOf(0.dp) }
    val density = LocalDensity.current

    if(componentMaxWidth == 0.dp){
        Box(modifier = Modifier.onGloballyPositioned {
            componentMaxWidth = with(density) {
                it.size.width.toDp()
            }
        }){
            valueRange.forEach {
                Text(
                    text = it.displayContent,
                    style = textStyle
                )
            }
        }
    }

    //component
    var currentIndex by remember { mutableIntStateOf(valueRange.indexOfFirst { it.value == value }) }
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
                .alpha(if (enabled() && currentIndex > 0) 1f else .5f)
                .clickable((enabled() && currentIndex > 0), onClick = {
                    --currentIndex
                    onValueChange(valueRange[currentIndex].value)
                })
        )
        AnimatedContent(
            targetState = currentIndex,
            label = "",
            transitionSpec = {
                if(targetState > initialState){//increase
                    slideInHorizontally {width -> width/2} + fadeIn() with
                            slideOutHorizontally {width->-width/2} + fadeOut()
                }else{
                    slideInHorizontally {width->-width/2} + fadeIn() with
                            slideOutHorizontally {width->width/2} + fadeOut()
                }.using(SizeTransform(clip = false))
            }) { index ->
            Text(
                modifier = Modifier.alpha(if (enabled()) 1f else .5f).width(componentMaxWidth),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.color.buttonColors,
                text = valueRange[index].displayContent
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.baseline_plus_24),
            contentDescription = null,
            tint = MaterialTheme.color.buttonColors,
            modifier = Modifier
                .size(14.dp)
                .alpha(if (enabled() && currentIndex < valueRange.lastIndex) 1f else .5f)
                .clickable((enabled() && currentIndex < valueRange.lastIndex), onClick = {
                    ++currentIndex
                    onValueChange(valueRange[currentIndex].value)
                })
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ValueSlideBarPreview() {
    SearchEmulatorTheme {
        RangeChooser(
            modifier = Modifier,
            enabled = { true },
            value = 1,
            valueRange = boardSizeRange,
            onValueChange = {}
        )
    }
}