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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.ui.base.toBlock
import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.theme.color
import com.timothy.searchemulator.ui.emulator.compose.PathBlockType.*
import com.timothy.searchemulator.ui.emulator.x
import com.timothy.searchemulator.ui.emulator.y
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme

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
        drawPathBlockFilled(brickSize, it.first, it.second, color)
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

enum class PathBlockType{
    TYPE_START_UP, TYPE_START_RIGHT, TYPE_START_DOWN, TYPE_START_LEFT,
    TYPE_UP_DEST, TYPE_RIGHT_DEST, TYPE_DOWN_DEST, TYPE_LEFT_DEST,
    TYPE_UP_LEFT, TYPE_LEFT_DOWN, TYPE_DOWN_RIGHT, TYPE_RIGHT_UP,
    TYPE_UP_DOWN, TYPE_LEFT_RIGHT
}

fun DrawScope.drawPathBlockFilled(
    brickSize: Int, x: Int, y: Int,
    color: Color = Color.Black,
    type:PathBlockType = TYPE_START_UP
) {
    val absoluteOffset = Offset(brickSize * x.toFloat(), brickSize * y.toFloat())
    val padding = brickSize * 0.25f
//    val outerSize = brickSize - padding * 2
    val lengthWithPadding = brickSize - padding
    val lengthWith2Padding = brickSize - padding*2

    var topLeft:Offset? = null
    var size:Size? = null
    var path:Path? = null
    when(type){
        TYPE_START_UP, TYPE_UP_DEST->{
            topLeft = absoluteOffset + Offset(padding, 0f)
            size = Size(lengthWith2Padding, lengthWithPadding)
        }

        TYPE_START_RIGHT, TYPE_RIGHT_DEST->{
            topLeft = absoluteOffset + Offset(padding, padding)
            size = Size(lengthWithPadding, lengthWith2Padding)
        }

        TYPE_START_DOWN, TYPE_DOWN_DEST->{
            topLeft = absoluteOffset + Offset(padding, padding)
            size = Size(lengthWith2Padding, lengthWithPadding)
        }

        TYPE_START_LEFT, TYPE_LEFT_DEST->{
            topLeft = absoluteOffset + Offset(0f, padding)
            size = Size(lengthWithPadding, lengthWith2Padding)
        }

        TYPE_UP_DOWN->{
            topLeft = absoluteOffset + Offset(padding, 0f)
            size = Size(lengthWith2Padding, brickSize.toFloat())
        }

        TYPE_LEFT_RIGHT->{
            topLeft = absoluteOffset + Offset(0f, padding)
            size = Size(brickSize.toFloat(), lengthWith2Padding)
        }

        TYPE_UP_LEFT->{
            path = Path().apply {
                moveTo(absoluteOffset.x+padding, absoluteOffset.y)
                lineTo(absoluteOffset.x+padding+ lengthWith2Padding, absoluteOffset.y)
                lineTo(absoluteOffset.x+padding+ lengthWith2Padding, absoluteOffset.y+lengthWithPadding)
                lineTo(absoluteOffset.x, absoluteOffset.y+lengthWithPadding)
                lineTo(absoluteOffset.x, absoluteOffset.y+padding)
                lineTo(absoluteOffset.x+padding, absoluteOffset.y+padding)
                lineTo(absoluteOffset.x+padding, absoluteOffset.y)
                close()
            }
        }

        TYPE_LEFT_DOWN->{
            path = Path().apply {
                moveTo(absoluteOffset.x, absoluteOffset.y + padding)
                lineTo(absoluteOffset.x+lengthWithPadding, absoluteOffset.y + padding)
                lineTo(absoluteOffset.x+lengthWithPadding, absoluteOffset.y + brickSize.toFloat())
                lineTo(absoluteOffset.x+padding, absoluteOffset.y + brickSize.toFloat())
                lineTo(absoluteOffset.x+padding, absoluteOffset.y+lengthWithPadding)
                lineTo(absoluteOffset.x, absoluteOffset.y+lengthWithPadding)
                lineTo(absoluteOffset.x, absoluteOffset.y + padding)
                close()
            }
        }

        TYPE_DOWN_RIGHT->{
            path = Path().apply {
                moveTo(absoluteOffset.x + padding, absoluteOffset.y + padding)
                lineTo(absoluteOffset.x + brickSize.toFloat(), absoluteOffset.y + padding)
                lineTo(absoluteOffset.x + brickSize.toFloat(), absoluteOffset.y + lengthWithPadding)
                lineTo(absoluteOffset.x + lengthWithPadding, absoluteOffset.y + lengthWithPadding)
                lineTo(absoluteOffset.x + lengthWithPadding, absoluteOffset.y + brickSize.toFloat())
                lineTo(absoluteOffset.x + padding, absoluteOffset.y + brickSize.toFloat())
                lineTo(absoluteOffset.x + padding, absoluteOffset.y + padding)
                close()
            }
        }

        TYPE_RIGHT_UP->{
            path = Path().apply {
                moveTo(absoluteOffset.x+padding, absoluteOffset.y)
                lineTo(absoluteOffset.x+lengthWithPadding, absoluteOffset.y)
                lineTo(absoluteOffset.x+lengthWithPadding, absoluteOffset.y+padding)
                lineTo(absoluteOffset.x+brickSize.toFloat(), absoluteOffset.y+padding)
                lineTo(absoluteOffset.x+brickSize.toFloat(), absoluteOffset.y+lengthWithPadding)
                lineTo(absoluteOffset.x+padding, absoluteOffset.y+lengthWithPadding)
                lineTo(absoluteOffset.x+padding, absoluteOffset.y)
                close()
            }
        }
    }

    when(type){
        TYPE_UP_LEFT, TYPE_LEFT_DOWN, TYPE_DOWN_RIGHT, TYPE_RIGHT_UP->{
            drawPath(path = path!!, color = color)
        }
        else->{
            drawRect(
                color = color,
                topLeft = topLeft!!,
                size = size!!,
                style = Fill
            )
        }
    }
}

@Preview
@Composable
fun Preview(){

    val mockBlock = listOf(
        Block(0,0),Block(0,1),Block(0,2),Block(0,3),
        Block(1,0),Block(1,1),Block(1,2),Block(1,3),
        Block(2,0),Block(2,1),Block(2,2),Block(2,3),Block(2,4),Block(2,5),
    )
    val mockType = listOf<PathBlockType>(
        TYPE_START_UP, TYPE_START_RIGHT, TYPE_START_DOWN, TYPE_START_LEFT,
        TYPE_UP_DEST, TYPE_RIGHT_DEST, TYPE_DOWN_DEST, TYPE_LEFT_DEST,
        TYPE_UP_LEFT, TYPE_LEFT_DOWN, TYPE_DOWN_RIGHT, TYPE_RIGHT_UP,
        TYPE_UP_DOWN, TYPE_LEFT_RIGHT
    )
    SearchEmulatorTheme {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for(i in mockBlock.indices){
                drawPathBlockFilled(
                    brickSize=350,
                    x = mockBlock[i].x,
                    y = mockBlock[i].y,
                    type = mockType[i]
                )
                drawUnitBlockOutline(
                    brickSize=350,
                    x = mockBlock[i].x,
                    y = mockBlock[i].y,
                    color = Color.Red
                )
            }
        }
    }

}