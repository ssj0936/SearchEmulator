package com.timothy.searchemulator.ui.emulator.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.model.MOVEMENT_SPEED_DEFAULT
import com.timothy.searchemulator.model.MS_PER_PATH_BLOCK
import com.timothy.searchemulator.model.getMovementSpeedDelay
import com.timothy.searchemulator.ui.base.toBlock
import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.emulator.Movement
import com.timothy.searchemulator.ui.emulator.algo.SearchBFS
import com.timothy.searchemulator.ui.emulator.x
import com.timothy.searchemulator.ui.emulator.y
import com.timothy.searchemulator.ui.theme.SearchEmulatorTheme
import com.timothy.searchemulator.ui.theme.color
import timber.log.Timber

@Composable
fun BoardView(
    modifier: Modifier,
    state: Contract.State,
    viewModel: EmulatorViewModel = hiltViewModel(),
) {
    var availableW by remember { mutableIntStateOf(0) }
    var availableH by remember { mutableIntStateOf(0) }

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
        }
    ) {
        BoardCanvas(
            modifier = Modifier.fillMaxSize(),
            state = state,
            colorBackground = MaterialTheme.color.colorBlockBackground,
            colorPassed = MaterialTheme.color.colorBlockPassed,
            colorCurrent = MaterialTheme.color.colorBlockTail,
            colorBarrier = MaterialTheme.color.colorBlockBarrier,
            colorStartPoint = MaterialTheme.color.colorBlockStart,
            colorDestPoint = MaterialTheme.color.colorBlockDest
        )

        FinalPathCanvas(
            modifier = Modifier.fillMaxSize(),
            state = state,
            pathColor = MaterialTheme.color.colorBlockPath,
        )
    }
}

@Composable
fun BoardCanvas(
    modifier: Modifier = Modifier,
    state: Contract.State,
    colorBackground: Color,
    colorPassed: Color,
    colorCurrent: Color,
    colorBarrier: Color,
    colorStartPoint: Color,
    colorDestPoint: Color,
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    val blockSize = state.blockSize
    val matrixW = state.matrixW
    val matrixH = state.matrixH

    var startBlock by remember { mutableStateOf(state.start) }
    var isMoveStartAnimationNeed by remember { mutableStateOf(false) }
    var destBlock by remember { mutableStateOf(state.dest) }
    var isMoveDestAnimationNeed by remember { mutableStateOf(false) }
    var barrierBlocks by remember { mutableStateOf(state.barrier.toSet()) }
    var barrierBlocksDiffShow by remember { mutableStateOf(emptyList<Block>()) }
    var barrierBlocksDiffHide by remember { mutableStateOf(emptyList<Block>()) }
    var isBarrierAnimationNeed by remember { mutableStateOf(false) }
//    var barrierAlphaTargetValue by remember { mutableFloatStateOf(0f) }

    val animAlpha = remember { Animatable(0f) }

    val currentStartOffset by animateOffsetAsState(
        targetValue = Offset(
            blockSize * startBlock!!.x.toFloat(),
            blockSize * startBlock!!.y.toFloat()
        ),
        animationSpec = tween(
            durationMillis = 200,
            easing = LinearOutSlowInEasing
        ),
        label = "",
        finishedListener = { isMoveStartAnimationNeed = false }
    )

    val currentDestOffset by animateOffsetAsState(
        targetValue = destBlock?.let {
            Offset(
                blockSize * it.x.toFloat(),
                blockSize * it.y.toFloat()
            )
        } ?: Offset(-1f, -1f),
        animationSpec = tween(
            durationMillis = 200,
            easing = LinearOutSlowInEasing
        ),
        label = "",
        finishedListener = { isMoveDestAnimationNeed = false }
    )

    startBlock = state.start
    destBlock = state.dest
//    barrierBlocks = state.barrier

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Contract.Effect.OnUndoEvent -> {
                    when (effect.movement) {
                        is Movement.MoveStart -> {
                            isMoveStartAnimationNeed = true
                        }

                        is Movement.MoveDest -> {
                            isMoveDestAnimationNeed = true
                        }

                        is Movement.DrawBarrier->{
                            isBarrierAnimationNeed = true

                            val tmp = viewModel.currentState.barrier.toHashSet()
                            animAlpha.snapTo(0f)
                            val moves = effect.movement.drawPath
                            val needToShow = mutableListOf<Block>()
                            val needToHide = mutableListOf<Block>()
                            val others = viewModel.currentState.barrier.toHashSet()
                            moves.forEach {
                                if(others.contains(it))
                                    needToShow.add(it)
                                else
                                    needToHide.add(it)
                                others.remove(it)
                            }
                            barrierBlocksDiffShow = needToShow
                            barrierBlocksDiffHide = needToHide
                            barrierBlocks = others

//                            barrierAlphaTargetValue = 1f
                            animAlpha.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                            barrierBlocks = tmp
                            isBarrierAnimationNeed = false
                        }
                    }
                }

                is Contract.Effect.OnRedoEvent -> {
                    when (effect.movement) {
                        is Movement.MoveStart -> {
                            isMoveStartAnimationNeed = true
                        }

                        is Movement.MoveDest -> {
                            isMoveDestAnimationNeed = true
                        }

                        is Movement.DrawBarrier->{
                            isBarrierAnimationNeed = true

                            val tmp = viewModel.currentState.barrier.toHashSet()
                            animAlpha.snapTo(0f)
                            val moves = effect.movement.drawPath
                            val needToShow = mutableListOf<Block>()
                            val needToHide = mutableListOf<Block>()
                            val others = viewModel.currentState.barrier.toHashSet()
                            moves.forEach {
                                if(others.contains(it))
                                    needToShow.add(it)
                                else
                                    needToHide.add(it)
                                others.remove(it)
                            }
                            barrierBlocksDiffShow = needToShow
                            barrierBlocksDiffHide = needToHide
                            barrierBlocks = others

//                            barrierAlphaTargetValue = 1f
                            animAlpha.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                            barrierBlocks = tmp
                            isBarrierAnimationNeed = false
                        }
                    }
                }

                else -> {}
            }
            Timber.d(effect.toString())
        }
    }

    Canvas(modifier = modifier) {

        drawBackground(blockSize, matrixW, matrixH, colorBackground)
        drawPassedBlocks(state.passed, blockSize, colorPassed, colorCurrent)
        if(isBarrierAnimationNeed){
            drawBarrierWithAnimation(
                barrierBlocks.toList(),
                barrierShow = barrierBlocksDiffShow,
                barrierHide = barrierBlocksDiffHide,
                matrixW,
                matrixH,
                blockSize,
                colorBarrier,
                bias = animAlpha.value
            )
        }else {
            drawBarrier(
                state.barrier.toList(),
                matrixW,
                matrixH,
                blockSize,
                colorBarrier
            )
        }

        if (isMoveStartAnimationNeed) {
            drawEndPointAnimated(currentStartOffset, blockSize, colorStartPoint)
        } else {
            drawEndPoint(startBlock, blockSize, colorStartPoint)
        }

        if (isMoveDestAnimationNeed) {
            drawEndPointAnimated(currentDestOffset, blockSize, colorDestPoint)
        } else {
            drawEndPoint(state.dest, blockSize, colorDestPoint)
        }
    }
}

@Composable
fun FinalPathCanvas(
    modifier: Modifier = Modifier,
    state: Contract.State,
    animationMsPerBlock: Int = MS_PER_PATH_BLOCK,
    pathColor: Color,
    strokeWidth: Dp = 8.dp,
    onAnimationFinish: () -> Unit = {}
) {
    if (state.path.isEmpty()) return

    val blockSize = state.blockSize
    val finalPath = state.path

    val currPath: Path = remember { Path() }
    val finalPathOffsets: List<Offset> = remember {
        finalPath.map {
            Offset(
                blockSize * it.x.toFloat() + blockSize / 2,
                blockSize * it.y.toFloat() + blockSize / 2
            )
        }
    }
    var targetIndexValue by remember {
        mutableIntStateOf(0)
    }

    val currentIndexValue by animateIntAsState(
        targetValue = targetIndexValue,
        animationSpec = tween(
            durationMillis = finalPath.size * animationMsPerBlock,
            easing = LinearEasing
        ),
        label = "",
        finishedListener = { onAnimationFinish() }
    )
    //animation Path
    Canvas(modifier = modifier) {
        if (currentIndexValue == 0) {
            currPath.moveTo(finalPathOffsets.first().x, finalPathOffsets.first().y)
        } else {
            currPath.lineTo(
                finalPathOffsets[currentIndexValue].x,
                finalPathOffsets[currentIndexValue].y
            )
        }

        drawPath(
            path = currPath,
            color = pathColor,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    //set targetValue
    LaunchedEffect(key1 = Unit) {
        targetIndexValue = finalPath.size - 1
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

fun DrawScope.drawEndPointAnimated(offset: Offset, brickSize: Int, color: Color) {
    drawUnitBlockFilledFromOffset(brickSize, offset, color)
}

fun DrawScope.drawPassedBlocks(
    passed: List<Block>,
    brickSize: Int,
    color: Color,
    currentColor: Color
) {
    //draw passed
    passed.forEachIndexed { i, block ->

        drawUnitBlockFilled(
            brickSize,
            block.x,
            block.y,
            if (i == passed.lastIndex) currentColor else color
        )
    }
}

fun DrawScope.drawBarrierWithAnimation(
    barrier: List<Block>,
    barrierShow: List<Block>,
    barrierHide: List<Block>,
    matrixW: Int,
    matrixH: Int,
    brickSize: Int,
    color: Color,
    bias : Float
) {
    //draw passed
    barrier.forEach {
        if (it.first in 0 until matrixW && it.second in 0 until matrixH)
            drawUnitBlockFilled(brickSize, it.first, it.second, color)
    }

    barrierShow.forEach {block->
        val absoluteOffset = Offset(brickSize * block.x.toFloat(), brickSize * block.y.toFloat())
        val padding = brickSize * 0.05f
        val outerSize = brickSize - padding * 2

        drawRect(
            color = color.copy(alpha = bias),
            topLeft = absoluteOffset + Offset(padding, padding),
            size = Size(outerSize, outerSize),
            style = Fill
        )
    }

    barrierHide.forEach {block->
        val absoluteOffset = Offset(brickSize * block.x.toFloat(), brickSize * block.y.toFloat())
        val padding = brickSize * 0.05f
        val outerSize = brickSize - padding * 2

        drawRect(
            color = color.copy(alpha = 1 - bias),
            topLeft = absoluteOffset + Offset(padding, padding),
            size = Size(outerSize, outerSize),
            style = Fill
        )
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

fun DrawScope.drawUnitBlockFilled(
    brickSize: Int, x: Int, y: Int,
    color: Color = Color.Black,
    bias: Float
) {
    val absoluteOffset = Offset(brickSize * x.toFloat(), bias + brickSize * y.toFloat())
    val padding = brickSize * 0.05f
    val outerSize = brickSize - padding * 2

    drawRect(
        color = color.copy(alpha = 1 - bias),
        topLeft = absoluteOffset + Offset(padding, padding),
        size = Size(outerSize, outerSize),
        style = Fill
    )


}

fun DrawScope.drawUnitBlockFilledFromOffset(
    brickSize: Int, offset: Offset,
    color: Color = Color.Black
) {
    val padding = brickSize * 0.05f
    val outerSize = brickSize - padding * 2

    drawRect(
        color = color,
        topLeft = offset + Offset(padding, padding),
        size = Size(outerSize, outerSize),
        style = Fill
    )
}


@Preview
@Composable
fun PreviewBoard() {
    SearchEmulatorTheme {
        BoardView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            state = Contract.State(
                status = Contract.Status.Idle,
                minSideBlockCnt = 18,
                start = Block(14, 11),
                dest = Block(13, 23),
                barrier = hashSetOf(
                    Block(4, 4),
                    Block(8, 9),
                    Block(12, 14),
                    Block(12, 15),
                    Block(16, 19),
                    Block(16, 20),
                    Block(4, 9),
                    Block(8, 14),
                    Block(12, 18),
                    Block(0, 9),
                    Block(8, 18),
                    Block(4, 15),
                    Block(4, 18),
                    Block(17, 6),
                    Block(17, 7),
                    Block(13, 7),
                    Block(9, 3),
                    Block(13, 9),
                    Block(17, 14),
                    Block(9, 7),
                    Block(5, 4),
                    Block(9, 9),
                    Block(13, 14),
                    Block(1, 4),
                    Block(5, 9),
                    Block(9, 14),
                    Block(13, 18),
                    Block(1, 9),
                    Block(1, 10),
                    Block(5, 14),
                    Block(9, 18),
                    Block(5, 15),
                    Block(5, 18),
                    Block(5, 21),
                    Block(5, 22),
                    Block(5, 23),
                    Block(5, 24),
                    Block(5, 25),
                    Block(14, 7),
                    Block(10, 3),
                    Block(10, 7),
                    Block(6, 4),
                    Block(10, 9),
                    Block(14, 14),
                    Block(6, 6),
                    Block(2, 3),
                    Block(6, 9),
                    Block(10, 14),
                    Block(14, 18),
                    Block(6, 10),
                    Block(10, 15),
                    Block(14, 19),
                    Block(6, 11),
                    Block(6, 12),
                    Block(2, 9),
                    Block(6, 14),
                    Block(10, 18),
                    Block(10, 20),
                    Block(10, 21),
                    Block(6, 18),
                    Block(10, 22),
                    Block(15, 7),
                    Block(11, 3),
                    Block(11, 7),
                    Block(7, 3),
                    Block(7, 4),
                    Block(11, 9),
                    Block(15, 14),
                    Block(7, 7),
                    Block(3, 3),
                    Block(3, 4),
                    Block(7, 9),
                    Block(11, 15),
                    Block(15, 19),
                    Block(3, 9),
                    Block(7, 14),
                    Block(11, 18),
                    Block(11, 19),
                    Block(11, 20),
                    Block(7, 18),
                    Block(3, 15),
                    Block(3, 16),
                    Block(3, 17),
                    Block(16, 7),
                    Block(12, 7),
                    Block(8, 3),
                    Block(12, 9),
                    Block(16, 14),
                    Block(8, 7)
                ),
                searchStrategy = SearchBFS.instance,
                searchProcessDelay = getMovementSpeedDelay(MOVEMENT_SPEED_DEFAULT.toFloat()),
                path = listOf(
                    Block(14, 11),
                    Block(13, 11),
                    Block(12, 11),
                    Block(11, 11),
                    Block(10, 11),
                    Block(9, 11),
                    Block(8, 11),
                    Block(7, 11),
                    Block(7, 12),
                    Block(7, 13),
                    Block(6, 13),
                    Block(5, 13),
                    Block(4, 13),
                    Block(3, 13),
                    Block(2, 13),
                    Block(1, 13),
                    Block(0, 13),
                    Block(0, 14),
                    Block(0, 15),
                    Block(0, 16),
                    Block(0, 17),
                    Block(0, 18),
                    Block(0, 19),
                    Block(0, 20),
                    Block(0, 21),
                    Block(0, 22),
                    Block(0, 23),
                    Block(0, 24),
                    Block(0, 25),
                    Block(1, 25),
                    Block(2, 25),
                    Block(3, 25),
                    Block(4, 25),
                    Block(4, 24),
                    Block(3, 24),
                    Block(2, 24),
                    Block(1, 24),
                    Block(1, 23),
                    Block(2, 23),
                    Block(3, 23),
                    Block(4, 23),
                    Block(4, 22),
                    Block(3, 22),
                    Block(2, 22),
                    Block(1, 22),
                    Block(1, 21),
                    Block(2, 21),
                    Block(3, 21),
                    Block(4, 21),
                    Block(4, 20),
                    Block(3, 20),
                    Block(2, 20),
                    Block(1, 20),
                    Block(1, 19),
                    Block(2, 19),
                    Block(3, 19),
                    Block(4, 19),
                    Block(5, 19),
                    Block(5, 20),
                    Block(6, 20),
                    Block(6, 21),
                    Block(6, 22),
                    Block(6, 23),
                    Block(6, 24),
                    Block(6, 25),
                    Block(7, 25),
                    Block(8, 25),
                    Block(9, 25),
                    Block(10, 25),
                    Block(11, 25),
                    Block(12, 25),
                    Block(13, 25),
                    Block(14, 25),
                    Block(15, 25),
                    Block(16, 25),
                    Block(17, 25),
                    Block(17, 24),
                    Block(16, 24),
                    Block(15, 24),
                    Block(14, 24),
                    Block(13, 24),
                    Block(12, 24),
                    Block(11, 24),
                    Block(10, 24),
                    Block(9, 24),
                    Block(8, 24),
                    Block(7, 24),
                    Block(7, 23),
                    Block(8, 23),
                    Block(9, 23),
                    Block(10, 23),
                    Block(11, 23),
                    Block(12, 23),
                    Block(13, 23)
                ),
                passed = listOf(
                    Block(14, 11),
                    Block(13, 11),
                    Block(12, 11),
                    Block(11, 11),
                    Block(10, 11),
                    Block(9, 11),
                    Block(8, 11),
                    Block(7, 11),
                    Block(7, 12),
                    Block(7, 13),
                    Block(6, 13),
                    Block(5, 13),
                    Block(4, 13),
                    Block(3, 13),
                    Block(2, 13),
                    Block(1, 13),
                    Block(0, 13),
                    Block(0, 14),
                    Block(0, 15),
                    Block(0, 16),
                    Block(0, 17),
                    Block(0, 18),
                    Block(0, 19),
                    Block(0, 20),
                    Block(0, 21),
                    Block(0, 22),
                    Block(0, 23),
                    Block(0, 24),
                    Block(0, 25),
                    Block(1, 25),
                    Block(2, 25),
                    Block(3, 25),
                    Block(4, 25),
                    Block(4, 24),
                    Block(3, 24),
                    Block(2, 24),
                    Block(1, 24),
                    Block(1, 23),
                    Block(2, 23),
                    Block(3, 23),
                    Block(4, 23),
                    Block(4, 22),
                    Block(3, 22),
                    Block(2, 22),
                    Block(1, 22),
                    Block(1, 21),
                    Block(2, 21),
                    Block(3, 21),
                    Block(4, 21),
                    Block(4, 20),
                    Block(3, 20),
                    Block(2, 20),
                    Block(1, 20),
                    Block(1, 19),
                    Block(2, 19),
                    Block(3, 19),
                    Block(4, 19),
                    Block(5, 19),
                    Block(5, 20),
                    Block(6, 20),
                    Block(6, 21),
                    Block(6, 22),
                    Block(6, 23),
                    Block(6, 24),
                    Block(6, 25),
                    Block(7, 25),
                    Block(8, 25),
                    Block(9, 25),
                    Block(10, 25),
                    Block(11, 25),
                    Block(12, 25),
                    Block(13, 25),
                    Block(14, 25),
                    Block(15, 25),
                    Block(16, 25),
                    Block(17, 25),
                    Block(17, 24),
                    Block(16, 24),
                    Block(15, 24),
                    Block(14, 24),
                    Block(13, 24),
                    Block(12, 24),
                    Block(11, 24),
                    Block(10, 24),
                    Block(9, 24),
                    Block(8, 24),
                    Block(7, 24),
                    Block(7, 23),
                    Block(8, 23),
                    Block(9, 23),
                    Block(10, 23),
                    Block(11, 23),
                    Block(12, 23)
                ),
                width = 890,
                height = 1280,
                blockSize = 49,
                matrixW = 18,
                matrixH = 26
            )
        )
    }
}