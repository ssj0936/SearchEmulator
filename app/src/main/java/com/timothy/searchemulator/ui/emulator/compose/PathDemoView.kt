package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.theme.color

@Composable
fun BoardView(
    modifier: Modifier,
    state: Contract.State,
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    var availableW by remember { mutableIntStateOf(0) }
    var availableH by remember { mutableIntStateOf(0) }

    val colorBlockBackgroundColor = MaterialTheme.color.colorBlockBackground
    val colorBlockStart = MaterialTheme.color.colorBlockStart
    val colorBlockDest = MaterialTheme.color.colorBlockDest
    val colorBlockBarrier = MaterialTheme.color.colorBlockBarrier
    val colorBlockPassed = MaterialTheme.color.colorBlockPassed
    val colorBlockPath = MaterialTheme.color.colorBlockPath


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
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackground(blockSize, matrixW, matrixH, colorBlockBackgroundColor)
            drawPassedBlocks(state.passed, blockSize, colorBlockPassed)
            drawPath(state.path, blockSize, colorBlockPath)
            drawBarrier(state.barrier, blockSize, colorBlockBarrier)
            drawEndPoint(state.start, blockSize, colorBlockStart)
            drawEndPoint(state.dest, blockSize, colorBlockDest)
        }
    }
}

fun DrawScope.drawBackground(brickSize: Int, matrixW: Int, matrixH: Int, color: Color) {
    (0 until matrixW).forEach { x ->
        (0 until matrixH).forEach { y ->
            drawUnitBlockOutline(brickSize, x, y, color)
        }
    }
}

fun DrawScope.drawEndPoint(position: Block?, brickSize: Int, color: Color){
    //start
    position?.let {drawUnitBlockFilled(brickSize, it.first, it.second, color)}
}

fun DrawScope.drawPassedBlocks(passed:List<Block>, brickSize: Int, color: Color){
    //draw passed
    passed.forEach {
        drawUnitBlockFilled(brickSize, it.first, it.second, color)
    }
}
fun DrawScope.drawBarrier(barrier:List<Block>, brickSize: Int, color: Color){
    //draw passed
    barrier.forEach {
        drawUnitBlockFilled(brickSize, it.first, it.second, color)
    }
}

fun DrawScope.drawPath(path:List<Block>?, brickSize: Int, color: Color){
    path?.forEach {
        drawUnitBlockFilled(brickSize, it.first, it.second, color)
    }
}

fun DrawScope.drawUnitBlockOutline(
    brickSize: Int, x: Int, y: Int,
    color: Color
) {
    val absoluteOffset = Offset(brickSize * x.toFloat(), brickSize * y.toFloat())
    val padding = brickSize * 0.05f
    val outerSize = brickSize - padding * 2

    drawRect(
        color = color,
        topLeft = absoluteOffset + Offset(padding, padding),
        size = Size(outerSize, outerSize),
        style = Stroke(outerSize / 30)
    )
}

fun DrawScope.drawUnitBlockFilled(
    brickSize: Int, x: Int, y: Int,
    color: Color = Color.Black
) {
    val absoluteOffset = Offset(brickSize * x.toFloat(), brickSize * y.toFloat())
    val padding = brickSize * 0.05f
    val outerSize = brickSize - padding * 2

    drawRect(
        color = color,
        topLeft = absoluteOffset + Offset(padding, padding),
        size = Size(outerSize, outerSize),
        style = Fill
    )
}