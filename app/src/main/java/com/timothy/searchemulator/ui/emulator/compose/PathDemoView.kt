package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.theme.color
import timber.log.Timber

@Composable
fun BoardView(
    modifier: Modifier,
    state: Contract.State,
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    var availableW by remember { mutableIntStateOf(0) }
    var availableH by remember { mutableIntStateOf(0) }


    val blockSize = state.blockSize
    val matrixW = state.matrixW
    val matrixH = state.matrixH

    var drawingBlockX by remember { mutableIntStateOf(0) }
    var drawingBlockY by remember { mutableIntStateOf(0) }

    Timber.d("(BoardView) state:$state")
    Box(modifier = modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    viewModel.setEvent(Contract.Event.OnBarrierDrawingStart(it))
                    Timber.d("onDragStart:$it")
                },
                onDragEnd = {
                    viewModel.setEvent(Contract.Event.OnBarrierDrawingEnd)
                    drawingBlockX = 0
                    drawingBlockY = 0
                    Timber.d("onDragEnd")
                }
            ) { change, _ ->
                val hoveringX = (change.position.x/viewModel.currentState.blockSize).toInt()
                val hoveringY = (change.position.y/viewModel.currentState.blockSize).toInt()
                if(hoveringX>=0 && hoveringY >=0 && ((hoveringX != drawingBlockX) || (hoveringY != drawingBlockY))){
                    Timber.d("offset:${change.position}, blockSize:${viewModel.currentState.blockSize}")
                    drawingBlockX = hoveringX
                    drawingBlockY = hoveringY
                    viewModel.setEvent(Contract.Event.OnBarrierDrawing(Block(drawingBlockX, drawingBlockY)))
                    Timber.d("drag change:($drawingBlockX, $drawingBlockY)")
                }

            }
        }
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
        with(MaterialTheme.color){
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBackground(blockSize, matrixW, matrixH, this@with.colorBlockBackground)
                drawPassedBlocks(state.passed, blockSize, this@with.colorBlockPassed)
                drawPath(state.path, blockSize, this@with.colorBlockPath)
                drawBarrier(state.barrier.toList(), matrixW, matrixH, blockSize, this@with.colorBlockBarrier)
                drawEndPoint(state.start, blockSize, this@with.colorBlockStart)
                drawEndPoint(state.dest, blockSize, this@with.colorBlockDest)
            }
        }


    }
}

fun Modifier.pointerInputCombine(
    key1: Any?,
    blocks: List<suspend PointerInputScope.() -> Unit>,
    index: Int = 0
): Modifier {
    return when (index) {
        blocks.size -> {
            Modifier
        }

        else -> {
            run {
                then(pointerInput(key1 = key1, blocks[index]))
                    .then(pointerInputCombine(key1, blocks, index + 1))
            }
        }
    }
}

suspend fun PointerInputScope.detectTapping() {
    this.detectTapGestures(
        onTap = {
            Timber.d("tap:$it")
        }
    )
}


suspend fun PointerInputScope.detectDragging(
    state: Contract.State,
    offsetX:MutableIntState,
    offsetY: MutableIntState,
    viewModel: EmulatorViewModel
) {
    this.detectDragGestures(
        onDragStart = {
//            offsetX.floatValue = it.x
//            offsetY.floatValue = it.y

            Timber.d("onDragStart:$it")
        },
        onDragEnd = {
            Timber.d("onDragEnd")
        }
    ) { change, _ ->
        Timber.d("drag change:${change.position}")
    }
}

fun DrawScope.drawBackground(brickSize: Int, matrixW: Int, matrixH: Int, color: Color) {
    (0 until matrixW).forEach { x ->
        (0 until matrixH).forEach { y ->
            drawUnitBlockOutline(brickSize, x, y, color)
        }
    }
}

fun DrawScope.drawEndPoint(position: Block?, brickSize: Int, color: Color) {
    //start
    position?.let { drawUnitBlockFilled(brickSize, it.first, it.second, color) }
}

fun DrawScope.drawPassedBlocks(passed: List<Block>, brickSize: Int, color: Color) {
    //draw passed
    passed.forEach {
        drawUnitBlockFilled(brickSize, it.first, it.second, color)
    }
}

fun DrawScope.drawBarrier(barrier: List<Block>, matrixW: Int, matrixH: Int, brickSize: Int, color: Color) {
    //draw passed
    barrier.forEach {
        if(it.first in 0 until matrixW && it.second in 0 until matrixH)
            drawUnitBlockFilled(brickSize, it.first, it.second, color)
    }
}

fun DrawScope.drawPath(path: List<Block>?, brickSize: Int, color: Color) {
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