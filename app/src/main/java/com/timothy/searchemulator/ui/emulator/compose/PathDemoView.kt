package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.timothy.searchemulator.ui.base.toBlock
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

    val drawingPosition = remember { mutableStateOf(Block(0, 0)) }

//    Timber.d("(BoardView) state:$state")
    Box(modifier = modifier
        .fillMaxSize()
        .pointerInput(viewModel.currentState.blockSize) {
            dragging(
                blockSize = viewModel.currentState.blockSize,
                drawingPosition = drawingPosition,
                onDragStart = { offset ->
                    viewModel.setEvent(Contract.Event.OnDraggingStart(offset))
                },
                onDragEnd = { viewModel.setEvent(Contract.Event.OnDraggingEnd) },
                onDrag = { block -> viewModel.setEvent(Contract.Event.OnDragging(block)) }
            )
        }
        .pointerInput(viewModel.currentState.blockSize) {
            detectTapGestures(
                onPress = {
                    viewModel.setEvent(Contract.Event.OnPressed(it))
                },
                onTap = {
                    viewModel.setEvent(Contract.Event.OnTap(it))
                }
            )
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
        with(MaterialTheme.color) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBackground(blockSize, matrixW, matrixH, this@with.colorBlockBackground)
                drawPassedBlocks(state.passed, blockSize, this@with.colorBlockPassed)
                drawPath(state.path, blockSize, this@with.colorBlockPath)
                drawBarrier(
                    state.barrier.toList(),
                    matrixW,
                    matrixH,
                    blockSize,
                    this@with.colorBlockBarrier
                )
                drawEndPoint(state.start, blockSize, this@with.colorBlockStart)
                drawEndPoint(state.dest, blockSize, this@with.colorBlockDest)
            }
        }
    }
}

suspend fun PointerInputScope.dragging(
    onDragStart: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Block) -> Unit,
    blockSize: Int,
    drawingPosition: MutableState<Block>
) {
    detectDragGestures(
        onDragStart = {
            onDragStart(it)
        },

        onDragEnd = {
            onDragEnd()
            drawingPosition.value = Block(0, 0)
        },

        onDrag = { change, _ ->
            val currentBlock = change.position.toBlock(blockSize)
            if (currentBlock != drawingPosition.value) {
                drawingPosition.value = currentBlock
                onDrag(drawingPosition.value)
            }
        }
    )
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

fun DrawScope.drawBarrier(
    barrier: List<Block>,
    matrixW: Int,
    matrixH: Int,
    brickSize: Int,
    color: Color
) {
    //draw passed
    barrier.forEach {
        if (it.first in 0 until matrixW && it.second in 0 until matrixH)
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