package com.timothy.searchemulator.ui.emulator.compose

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffset
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.searchemulator.model.MS_PER_PATH_BLOCK
import com.timothy.searchemulator.ui.base.toBlock
import com.timothy.searchemulator.ui.base.toOffset
import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.Contract
import com.timothy.searchemulator.ui.emulator.EmulatorViewModel
import com.timothy.searchemulator.ui.emulator.StatusType
import com.timothy.searchemulator.ui.emulator.x
import com.timothy.searchemulator.ui.emulator.y
import com.timothy.searchemulator.ui.theme.color
import timber.log.Timber

@Composable
fun BoardView(
    modifier: Modifier,
    state: () -> Contract.State,
    blockSize: Int,
    matrixW: Int,
    matrixH: Int,
    start: Block,
    dest: Block,
    lastMovement: () -> StatusType,
    passed: () -> List<Block>,
    barrier: () -> List<Block>,
    finalPath: () -> List<Block>,
    isNeedAnimate:Boolean,

    viewModel: EmulatorViewModel = hiltViewModel(),
) {
    var availableW by remember { mutableIntStateOf(0) }
    var availableH by remember { mutableIntStateOf(0) }

    val drawingPosition = remember { mutableStateOf(Block(0, 0)) }

//    Timber.d("(BoardView) state:${state.lastMovement}")
    Box(modifier = modifier
        .fillMaxSize()
        .pointerInput(viewModel.currentState.blockSize) {
            dragging(blockSize = viewModel.currentState.blockSize,
                drawingPosition = drawingPosition,
                onDragStart = { offset ->
                    viewModel.setEvent(Contract.Event.OnDraggingStart(offset))
                },
                onDragEnd = { viewModel.setEvent(Contract.Event.OnDraggingEnd) },
                onDrag = { block -> viewModel.setEvent(Contract.Event.OnDragging(block)) })
        }
        .pointerInput(viewModel.currentState.blockSize) {
            detectTapGestures(onPress = {
                viewModel.setEvent(Contract.Event.OnPressed(it))
            }, onTap = {
                viewModel.setEvent(Contract.Event.OnTap(it))
            })
        }
        .onGloballyPositioned { coordinates ->
            if (coordinates.size.width * coordinates.size.height != availableW * availableH) {
                availableW = coordinates.size.width
                availableH = coordinates.size.height
                viewModel.setEvent(
                    Contract.Event.OnScreenMeasured(
                        availableW, availableH
                    )
                )
            }
        }) {
        Timber.d("(recompose) BoardView")
        BoardCanvases(
            modifier = Modifier.fillMaxSize(),
            stateProvider = state,
            blockSize = blockSize,
            matrixW = matrixW,
            matrixH = matrixH,
            start = start,
            dest = dest,
            lastMovement = lastMovement,
            passed = passed,
            barrier = barrier,
            finalPath = finalPath,
            isNeedAnimate = isNeedAnimate,
            colorBackground = MaterialTheme.color.colorBlockBackground,
            colorPassed = MaterialTheme.color.colorBlockPassed,
            colorCurrent = MaterialTheme.color.colorBlockTail,
            colorBarrier = MaterialTheme.color.colorBlockBarrier,
            colorStartPoint = MaterialTheme.color.colorBlockStart,
            colorDestPoint = MaterialTheme.color.colorBlockDest,
        )
    }
}

@Composable
fun CanvasBackground(
    modifier: Modifier = Modifier,
    blockSize: Int,
    matrixW: Int,
    matrixH: Int,
    colorBackground: Color,
) {
    Timber.d("CanvasBackground")
    Canvas(modifier = modifier) {
        drawBackground(blockSize, matrixW, matrixH, colorBackground)
    }
}

@Composable
fun CanvasPassed(
    modifier: Modifier = Modifier,
    blockSize: Int,
    passedProvider: () -> List<Block>,
    colorPassed: Color,
    colorCurrent: Color,
) {
    if (passedProvider().isEmpty()) return
    Timber.d("CanvasPassed")
    Canvas(modifier = modifier) {
        drawPassedBlocks(passedProvider(), blockSize, colorPassed, colorCurrent)
    }
}

@Composable
fun CanvasEndPointsWithAnimation(
    modifier: Modifier = Modifier,
    blockSize: Int,
    start: () -> Block,
    dest: () -> Block,
    lastMovement: () -> StatusType,
    colorStartPoint: Color,
    colorDestPoint: Color,
) {
    Timber.d("CanvasEndPoints(2)")
//        Timber.d("(recompose) CanvasEndPoints")
    val transition = updateTransition(
        targetState = Triple(start(), dest(), lastMovement()), label = "pathTransition"
    )

    val startOffset by transition.animateOffset(
        transitionSpec = {
            tween(
                durationMillis = if (this.targetState.third == StatusType.Normal) 0 else 200,
                easing = LinearOutSlowInEasing
            )
        }, label = "animation of startOffset transition"
    ) { s -> s.first.toOffset(blockSize) }

    val destOffset by transition.animateOffset(
        transitionSpec = {
            tween(
                durationMillis = if (this.targetState.third == StatusType.Normal) 0 else 200,
                easing = LinearOutSlowInEasing
            )
        }, label = "animation of destOffset transition"
    ) { s -> s.second.toOffset(blockSize) }

    Canvas(modifier = modifier) {
        drawEndPointWithOffset(/*startOffset*/startOffset, blockSize, colorStartPoint)
        drawEndPointWithOffset(/*destOffset*/destOffset, blockSize, colorDestPoint)
    }
}

@Composable
fun CanvasBarrier(
    modifier: Modifier = Modifier,
    barrier: () -> List<Block>,
    blockSize: Int,
    matrixW: Int,
    matrixH: Int,
    colorBarrier: Color
) {
    Timber.d("CanvasBarrier")
    Canvas(modifier = modifier) {

//        if(state.lastMovement is StatusType.BarrierTimeMachine){
//            Timber.d("drawBarrierWithAnimation")
//            val lastMovement = state.lastMovement.diff
//            val needToShow = mutableListOf<Block>()
//            val needToHide = mutableListOf<Block>()
//            val others = state.barrier.toHashSet()
//            lastMovement.forEach {
//                if(others.contains(it))//目前已經有了，所以本來沒有，要show出來
//                    needToShow.add(it)
//                else //目前沒有，所以本來有，要hide
//                    needToHide.add(it)
//                others.remove(it)
//            }
//
//            drawBarrierWithAnimation(
//                barrierShow = needToShow,
//                barrierHide = needToHide,
//                others = others.toList(),
//                matrixW,
//                matrixH,
//                blockSize,
//                colorBarrier,
//                bias = alphaBias
//            )
//        }else {
//        Timber.d("drawBarrier")
        drawBarrier(
            barrier().toList(), matrixW, matrixH, blockSize, colorBarrier
        )
    }
}

@Composable
fun CanvasEndPoints(
    modifier: Modifier = Modifier,
    blockSize: Int,
    start: () -> Block,
    dest: () -> Block,
    colorStartPoint: Color,
    colorDestPoint: Color,
    isAnimationNeed:Boolean,
) {
    Timber.d("CanvasEndPoints")
    if(!isAnimationNeed) {
        Canvas(modifier = modifier) {
            drawEndPointWithOffset(start().toOffset(blockSize), blockSize, colorStartPoint)
            drawEndPointWithOffset(dest().toOffset(blockSize), blockSize, colorDestPoint)
        }
    }else {
        Timber.d("CanvasEndPoints(2)")
//        Timber.d("(recompose) CanvasEndPoints")
        val transition = updateTransition(
            targetState = Pair(start(), dest()), label = "pathTransition"
        )

        val startOffset by transition.animateOffset(
            transitionSpec = {
                tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            }, label = "animation of startOffset transition"
        ) { s -> s.first.toOffset(blockSize) }

        val destOffset by transition.animateOffset(
            transitionSpec = {
                tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            }, label = "animation of destOffset transition"
        ) { s -> s.second.toOffset(blockSize) }

        Canvas(modifier = modifier) {
            drawEndPointWithOffset(/*startOffset*/startOffset, blockSize, colorStartPoint)
            drawEndPointWithOffset(/*destOffset*/destOffset, blockSize, colorDestPoint)
        }
    }

}

@Composable
fun CanvasEndPoints(
    modifier: Modifier = Modifier,
    blockSize: Int,
    node: Block,
    color: Color,
    isAnimationNeed:Boolean,
) {
    Timber.d("CanvasEndPoints???")

    val offset by animateOffsetAsState(
        targetValue = node.toOffset(blockSize),
        animationSpec = if(isAnimationNeed) tween(durationMillis = 200) else SnapSpec(),
        label = ""
    )

    Canvas(modifier = modifier) {
        drawEndPointWithOffset(offset, blockSize, color)
    }
}

@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun BoardCanvases(
    modifier: Modifier = Modifier,
    stateProvider: () -> Contract.State,
    blockSize: Int,
    matrixW: Int,
    matrixH: Int,
    start: Block,
    dest: Block,
    lastMovement: () -> StatusType,
    passed: () -> List<Block>,
    barrier: () -> List<Block>,
    finalPath: () -> List<Block>,
    isNeedAnimate:Boolean,
    colorBackground: Color,
    colorPassed: Color,
    colorCurrent: Color,
    colorBarrier: Color,
    colorStartPoint: Color,
    colorDestPoint: Color,
) {
    Timber.d("(recompose) BoardCanvas")
    Box(modifier = modifier) {
        CanvasBackground(
            blockSize = blockSize,
            matrixW = matrixW,
            matrixH = matrixH,
            colorBackground = colorBackground
        )
        CanvasPassed(
            passedProvider = passed,
            blockSize = blockSize,
            colorPassed = colorPassed,
            colorCurrent = colorCurrent
        )

        CanvasBarrier(
            barrier = barrier,
            blockSize = blockSize,
            matrixW = matrixW,
            matrixH = matrixH,
            colorBarrier = colorBarrier
        )

        CanvasEndPoints(
            blockSize = blockSize, node = start,
            color = colorStartPoint,
            isAnimationNeed = isNeedAnimate
        )

        CanvasEndPoints(
            blockSize = blockSize, node = dest,
            color = colorDestPoint,
            isAnimationNeed = isNeedAnimate
        )

        CanvasFinalPath(
            modifier = Modifier.fillMaxSize(),
            blockSize = blockSize,
            finalPathProvider = finalPath,
            pathColor = MaterialTheme.color.colorBlockPath,
        )
    }
}

@Composable
fun CanvasFinalPath(
    modifier: Modifier = Modifier,
    blockSize: Int,
    finalPathProvider: () -> List<Block>,
    animationMsPerBlock: Int = MS_PER_PATH_BLOCK,
    pathColor: Color,
    strokeWidth: Dp = 8.dp,
    onAnimationFinish: () -> Unit = {}
) {
    if (finalPathProvider().isEmpty()) return

    val finalPath = finalPathProvider()

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

    val currentIndexValue by animateIntAsState(targetValue = targetIndexValue,
        animationSpec = tween(
            durationMillis = finalPath.size * animationMsPerBlock, easing = LinearEasing
        ),
        label = "",
        finishedListener = { onAnimationFinish() })
    //animation Path
    Canvas(modifier = modifier) {
        if (currentIndexValue == 0) {
            currPath.moveTo(finalPathOffsets.first().x, finalPathOffsets.first().y)
        } else {
            currPath.lineTo(
                finalPathOffsets[currentIndexValue].x, finalPathOffsets[currentIndexValue].y
            )
        }

        drawPath(
            path = currPath, color = pathColor, style = Stroke(
                width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round
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
    detectDragGestures(onDragStart = {
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
        })
}

fun Modifier.pointerInputCombine(
    key1: Any?, blocks: List<suspend PointerInputScope.() -> Unit>, index: Int = 0
): Modifier {
    return when (index) {
        blocks.size -> {
            Modifier
        }

        else -> {
            run {
                then(pointerInput(key1 = key1, blocks[index])).then(
                    pointerInputCombine(
                        key1, blocks, index + 1
                    )
                )
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

fun DrawScope.drawEndPointWithOffset(offset: Offset, brickSize: Int, color: Color) {
    drawUnitBlockFilledFromOffset(brickSize, offset, color)
}

fun DrawScope.drawPassedBlocks(
    passed: List<Block>, brickSize: Int, color: Color, currentColor: Color
) {
    //draw passed
    passed.forEachIndexed { i, block ->

        drawUnitBlockFilled(
            brickSize, block.x, block.y, if (i == passed.lastIndex) currentColor else color
        )
    }
}

fun DrawScope.drawBarrierWithAnimation(
    barrierShow: List<Block>,
    barrierHide: List<Block>,
    others: List<Block>,
    matrixW: Int,
    matrixH: Int,
    brickSize: Int,
    color: Color,
    bias: Float
) {

    //draw passed
    others.forEach {
        if (it.first in 0 until matrixW && it.second in 0 until matrixH) drawUnitBlockFilled(
            brickSize, it.first, it.second, color
        )
    }

    barrierShow.forEach { block ->
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
    Timber.d("1 - bias:${1 - bias}")
    barrierHide.forEach { block ->
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
    barrier: List<Block>, matrixW: Int, matrixH: Int, brickSize: Int, color: Color
) {
    //draw passed
    barrier.forEach {
        if (it.first in 0 until matrixW && it.second in 0 until matrixH) drawUnitBlockFilled(
            brickSize, it.first, it.second, color
        )
    }
}

fun DrawScope.drawUnitBlockOutline(
    brickSize: Int, x: Int, y: Int, color: Color
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
    brickSize: Int, x: Int, y: Int, color: Color = Color.Black
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
    brickSize: Int, x: Int, y: Int, color: Color = Color.Black, bias: Float
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
    brickSize: Int, offset: Offset, color: Color = Color.Black
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

//
//@Preview
//@Composable
//fun PreviewBoard() {
//    SearchEmulatorTheme {
//        BoardView(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 12.dp),
//            state = Contract.State(
//                status = Contract.Status.Idle,
//                minSideBlockCnt = 18,
//                start = Block(14, 11),
//                dest = Block(13, 23),
//                barrier = hashSetOf(
//                    Block(4, 4),
//                    Block(8, 9),
//                    Block(12, 14),
//                    Block(12, 15),
//                    Block(16, 19),
//                    Block(16, 20),
//                    Block(4, 9),
//                    Block(8, 14),
//                    Block(12, 18),
//                    Block(0, 9),
//                    Block(8, 18),
//                    Block(4, 15),
//                    Block(4, 18),
//                    Block(17, 6),
//                    Block(17, 7),
//                    Block(13, 7),
//                    Block(9, 3),
//                    Block(13, 9),
//                    Block(17, 14),
//                    Block(9, 7),
//                    Block(5, 4),
//                    Block(9, 9),
//                    Block(13, 14),
//                    Block(1, 4),
//                    Block(5, 9),
//                    Block(9, 14),
//                    Block(13, 18),
//                    Block(1, 9),
//                    Block(1, 10),
//                    Block(5, 14),
//                    Block(9, 18),
//                    Block(5, 15),
//                    Block(5, 18),
//                    Block(5, 21),
//                    Block(5, 22),
//                    Block(5, 23),
//                    Block(5, 24),
//                    Block(5, 25),
//                    Block(14, 7),
//                    Block(10, 3),
//                    Block(10, 7),
//                    Block(6, 4),
//                    Block(10, 9),
//                    Block(14, 14),
//                    Block(6, 6),
//                    Block(2, 3),
//                    Block(6, 9),
//                    Block(10, 14),
//                    Block(14, 18),
//                    Block(6, 10),
//                    Block(10, 15),
//                    Block(14, 19),
//                    Block(6, 11),
//                    Block(6, 12),
//                    Block(2, 9),
//                    Block(6, 14),
//                    Block(10, 18),
//                    Block(10, 20),
//                    Block(10, 21),
//                    Block(6, 18),
//                    Block(10, 22),
//                    Block(15, 7),
//                    Block(11, 3),
//                    Block(11, 7),
//                    Block(7, 3),
//                    Block(7, 4),
//                    Block(11, 9),
//                    Block(15, 14),
//                    Block(7, 7),
//                    Block(3, 3),
//                    Block(3, 4),
//                    Block(7, 9),
//                    Block(11, 15),
//                    Block(15, 19),
//                    Block(3, 9),
//                    Block(7, 14),
//                    Block(11, 18),
//                    Block(11, 19),
//                    Block(11, 20),
//                    Block(7, 18),
//                    Block(3, 15),
//                    Block(3, 16),
//                    Block(3, 17),
//                    Block(16, 7),
//                    Block(12, 7),
//                    Block(8, 3),
//                    Block(12, 9),
//                    Block(16, 14),
//                    Block(8, 7)
//                ),
//                searchStrategy = SearchBFS.instance,
//                searchProcessDelay = getMovementSpeedDelay(MOVEMENT_SPEED_DEFAULT.toFloat()),
//                path = listOf(
//                    Block(14, 11),
//                    Block(13, 11),
//                    Block(12, 11),
//                    Block(11, 11),
//                    Block(10, 11),
//                    Block(9, 11),
//                    Block(8, 11),
//                    Block(7, 11),
//                    Block(7, 12),
//                    Block(7, 13),
//                    Block(6, 13),
//                    Block(5, 13),
//                    Block(4, 13),
//                    Block(3, 13),
//                    Block(2, 13),
//                    Block(1, 13),
//                    Block(0, 13),
//                    Block(0, 14),
//                    Block(0, 15),
//                    Block(0, 16),
//                    Block(0, 17),
//                    Block(0, 18),
//                    Block(0, 19),
//                    Block(0, 20),
//                    Block(0, 21),
//                    Block(0, 22),
//                    Block(0, 23),
//                    Block(0, 24),
//                    Block(0, 25),
//                    Block(1, 25),
//                    Block(2, 25),
//                    Block(3, 25),
//                    Block(4, 25),
//                    Block(4, 24),
//                    Block(3, 24),
//                    Block(2, 24),
//                    Block(1, 24),
//                    Block(1, 23),
//                    Block(2, 23),
//                    Block(3, 23),
//                    Block(4, 23),
//                    Block(4, 22),
//                    Block(3, 22),
//                    Block(2, 22),
//                    Block(1, 22),
//                    Block(1, 21),
//                    Block(2, 21),
//                    Block(3, 21),
//                    Block(4, 21),
//                    Block(4, 20),
//                    Block(3, 20),
//                    Block(2, 20),
//                    Block(1, 20),
//                    Block(1, 19),
//                    Block(2, 19),
//                    Block(3, 19),
//                    Block(4, 19),
//                    Block(5, 19),
//                    Block(5, 20),
//                    Block(6, 20),
//                    Block(6, 21),
//                    Block(6, 22),
//                    Block(6, 23),
//                    Block(6, 24),
//                    Block(6, 25),
//                    Block(7, 25),
//                    Block(8, 25),
//                    Block(9, 25),
//                    Block(10, 25),
//                    Block(11, 25),
//                    Block(12, 25),
//                    Block(13, 25),
//                    Block(14, 25),
//                    Block(15, 25),
//                    Block(16, 25),
//                    Block(17, 25),
//                    Block(17, 24),
//                    Block(16, 24),
//                    Block(15, 24),
//                    Block(14, 24),
//                    Block(13, 24),
//                    Block(12, 24),
//                    Block(11, 24),
//                    Block(10, 24),
//                    Block(9, 24),
//                    Block(8, 24),
//                    Block(7, 24),
//                    Block(7, 23),
//                    Block(8, 23),
//                    Block(9, 23),
//                    Block(10, 23),
//                    Block(11, 23),
//                    Block(12, 23),
//                    Block(13, 23)
//                ),
//                passed = listOf(
//                    Block(14, 11),
//                    Block(13, 11),
//                    Block(12, 11),
//                    Block(11, 11),
//                    Block(10, 11),
//                    Block(9, 11),
//                    Block(8, 11),
//                    Block(7, 11),
//                    Block(7, 12),
//                    Block(7, 13),
//                    Block(6, 13),
//                    Block(5, 13),
//                    Block(4, 13),
//                    Block(3, 13),
//                    Block(2, 13),
//                    Block(1, 13),
//                    Block(0, 13),
//                    Block(0, 14),
//                    Block(0, 15),
//                    Block(0, 16),
//                    Block(0, 17),
//                    Block(0, 18),
//                    Block(0, 19),
//                    Block(0, 20),
//                    Block(0, 21),
//                    Block(0, 22),
//                    Block(0, 23),
//                    Block(0, 24),
//                    Block(0, 25),
//                    Block(1, 25),
//                    Block(2, 25),
//                    Block(3, 25),
//                    Block(4, 25),
//                    Block(4, 24),
//                    Block(3, 24),
//                    Block(2, 24),
//                    Block(1, 24),
//                    Block(1, 23),
//                    Block(2, 23),
//                    Block(3, 23),
//                    Block(4, 23),
//                    Block(4, 22),
//                    Block(3, 22),
//                    Block(2, 22),
//                    Block(1, 22),
//                    Block(1, 21),
//                    Block(2, 21),
//                    Block(3, 21),
//                    Block(4, 21),
//                    Block(4, 20),
//                    Block(3, 20),
//                    Block(2, 20),
//                    Block(1, 20),
//                    Block(1, 19),
//                    Block(2, 19),
//                    Block(3, 19),
//                    Block(4, 19),
//                    Block(5, 19),
//                    Block(5, 20),
//                    Block(6, 20),
//                    Block(6, 21),
//                    Block(6, 22),
//                    Block(6, 23),
//                    Block(6, 24),
//                    Block(6, 25),
//                    Block(7, 25),
//                    Block(8, 25),
//                    Block(9, 25),
//                    Block(10, 25),
//                    Block(11, 25),
//                    Block(12, 25),
//                    Block(13, 25),
//                    Block(14, 25),
//                    Block(15, 25),
//                    Block(16, 25),
//                    Block(17, 25),
//                    Block(17, 24),
//                    Block(16, 24),
//                    Block(15, 24),
//                    Block(14, 24),
//                    Block(13, 24),
//                    Block(12, 24),
//                    Block(11, 24),
//                    Block(10, 24),
//                    Block(9, 24),
//                    Block(8, 24),
//                    Block(7, 24),
//                    Block(7, 23),
//                    Block(8, 23),
//                    Block(9, 23),
//                    Block(10, 23),
//                    Block(11, 23),
//                    Block(12, 23)
//                ),
//                width = 890,
//                height = 1280,
//                blockSize = 49,
//                matrixW = 18,
//                matrixH = 26
//            )
//        )
//    }
//}