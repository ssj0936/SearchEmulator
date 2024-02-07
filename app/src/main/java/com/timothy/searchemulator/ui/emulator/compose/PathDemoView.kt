package com.timothy.searchemulator.ui.emulator.compose

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import com.timothy.searchemulator.ui.emulator.OperationType
import com.timothy.searchemulator.ui.emulator.x
import com.timothy.searchemulator.ui.emulator.y
import com.timothy.searchemulator.ui.theme.LightColorScheme
import com.timothy.searchemulator.ui.theme.color

@Composable
fun BoardView(
    modifier: Modifier,
    viewModel: EmulatorViewModel = hiltViewModel(),
) {
    var availableW by remember { mutableIntStateOf(0) }
    var availableH by remember { mutableIntStateOf(0) }

    val drawingPosition = remember { mutableStateOf(Block(0, 0)) }

//    Timber.d("(BoardView) state:${state.lastMovement}")
    Box(modifier = modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            dragging(blockSizeProvider = viewModel.blockSizeProvider,
                drawingPosition = drawingPosition,
                onDragStart = { offset ->
                    viewModel.setEvent(Contract.Event.OnDraggingStart(offset))
                },
                onDragEnd = { viewModel.setEvent(Contract.Event.OnDraggingEnd) },
                onDrag = { block -> viewModel.setEvent(Contract.Event.OnDragging(block)) })
        }
        .pointerInput(Unit) {
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
//        Timber.d("(recompose) BoardView")
        BoardCanvases(
            modifier = Modifier.fillMaxSize(),
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
    blockSizeProvider: () -> Int,
    matrixWProvider: () -> Int,
    matrixHProvider: () -> Int,
    colorBackground: Color,
) {
//    Timber.d("CanvasBackground")
    Canvas(modifier = modifier) {
        drawBackground(blockSizeProvider(), matrixWProvider(), matrixHProvider(), colorBackground)
    }
}

@Composable
fun CanvasPassed(
    modifier: Modifier = Modifier,
    blockSizeProvider: () -> Int,
    passedProvider: () -> List<Block>,
    isSearchDone: () -> Boolean,
    colorPassed: Color,
    colorCurrent: Color,
) {
    if (passedProvider().isEmpty()) return
//    Timber.d("CanvasPassed")
    Canvas(modifier = modifier) {
        drawPassedBlocks(
            passedProvider(),
            blockSizeProvider(),
            isSearchDone,
            colorPassed,
            colorCurrent
        )
    }
}

@Composable
fun CanvasBarrier(
    modifier: Modifier = Modifier,
    barrier: () -> HashSet<Block>,
    blockSizeProvider: () -> Int,
    matrixWProvider: () -> Int,
    matrixHProvider: () -> Int,
    isAnimationNeed: () -> Boolean,
    colorBarrier: Color
) {
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(key1 = barrier()) {
        if (isAnimationNeed()) {
            alpha.snapTo(0f)
            alpha.animateTo(1f, animationSpec = tween(300))
        }
    }

    Canvas(modifier = modifier) {
        drawBarrier(
            barrier().toList(),
            matrixWProvider(),
            matrixHProvider(),
            blockSizeProvider(),
            colorBarrier,
            alpha.value
        )
    }
}

@Composable
fun CanvasEndPoints(
    modifier: Modifier = Modifier,
    blockSizeProvider: () -> Int,
    nodeProvider: () -> Block,
    color: Color,
    isAnimationNeed: () -> Boolean,
) {
//    Timber.d("CanvasEndPoints???")

    val offset by animateOffsetAsState(
        targetValue = nodeProvider().toOffset(blockSizeProvider()),
        animationSpec = if (isAnimationNeed()) tween(durationMillis = 200) else SnapSpec(),
        label = ""
    )

    Canvas(modifier = modifier) {
        drawEndPointWithOffset(offset, blockSizeProvider(), color)
    }
}

@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun BoardCanvases(
    modifier: Modifier = Modifier,
    colorBackground: Color,
    colorPassed: Color,
    colorCurrent: Color,
    colorBarrier: Color,
    colorStartPoint: Color,
    colorDestPoint: Color,
    viewModel: EmulatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val status by viewModel.status.collectAsState()

//    Timber.d("(recompose) BoardCanvas")
    Box(modifier = modifier) {
        CanvasBackground(
            blockSizeProvider = { state.blockSizePx },
            matrixWProvider = { state.matrixW },
            matrixHProvider = { state.matrixH },
            colorBackground = colorBackground
        )
        CanvasPassed(
            passedProvider = { state.passed },
            blockSizeProvider = { state.blockSizePx },
            colorPassed = colorPassed,
            colorCurrent = colorCurrent,
            isSearchDone = { status == Contract.Status.SearchFinish }
        )

        CanvasBarrier(
            barrier = { state.barrier },
            blockSizeProvider = { state.blockSizePx },
            matrixWProvider = { state.matrixW },
            matrixHProvider = { state.matrixH },
            colorBarrier = colorBarrier,
            isAnimationNeed = { state.lastOperationType == OperationType.GENERATE_MAZE }
        )

        CanvasEndPoints(
            blockSizeProvider = { state.blockSizePx }, nodeProvider = { state.start!! },
            color = colorStartPoint,
            isAnimationNeed = {
                state.lastOperationType == OperationType.UNDO_START
                        || state.lastOperationType == OperationType.REDO_START
                        || state.lastOperationType == OperationType.GENERATE_MAZE
            }
        )

        CanvasEndPoints(
            blockSizeProvider = { state.blockSizePx }, nodeProvider = { state.dest!! },
            color = colorDestPoint,
            isAnimationNeed = {
                state.lastOperationType == OperationType.UNDO_DEST
                        || state.lastOperationType == OperationType.REDO_DEST
                        || state.lastOperationType == OperationType.GENERATE_MAZE
            }
        )

        CanvasFinalPath(
            modifier = Modifier.fillMaxSize(),
            blockSizeProvider = { state.blockSizePx },
            finalPathProvider = { state.path },
            pathColor = MaterialTheme.color.colorBlockPath,
        )
    }
}

@Composable
fun CanvasFinalPath(
    modifier: Modifier = Modifier,
    blockSizeProvider: () -> Int,
    finalPathProvider: () -> List<Block>,
    animationMsPerBlock: Int = MS_PER_PATH_BLOCK,
    pathColor: Color,
    strokeWidth: Dp = 8.dp,
    onAnimationFinish: () -> Unit = {}
) {
    if (finalPathProvider().isEmpty()) return


    val currPath: Path = remember { Path() }
    val finalPathOffsets: List<Offset> = remember {
        finalPathProvider().map {
            Offset(
                blockSizeProvider() * it.x.toFloat() + blockSizeProvider() / 2,
                blockSizeProvider() * it.y.toFloat() + blockSizeProvider() / 2
            )
        }
    }
    var targetIndexValue by remember {
        mutableIntStateOf(0)
    }

    val animateIndexValue by animateIntAsState(
        targetValue = targetIndexValue,
        animationSpec = tween(
            durationMillis = finalPathOffsets.size * animationMsPerBlock, easing = LinearEasing
        ),
        label = "",
        finishedListener = { onAnimationFinish() }
    )
    var currentIndexValue: Int = remember { -1 }
    //animation Path
    Canvas(modifier = modifier) {
        if (currentIndexValue < animateIndexValue) {
            for (i in currentIndexValue + 1..animateIndexValue) {
                if (i == 0)
                    currPath.moveTo(finalPathOffsets.first().x, finalPathOffsets.first().y)
                else
                    currPath.lineTo(
                        finalPathOffsets[i].x, finalPathOffsets[i].y
                    )
            }
            currentIndexValue = animateIndexValue
        }

        drawPath(
            path = currPath, color = pathColor, style = Stroke(
                width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round
            )
        )
    }

    //set targetValue
    LaunchedEffect(key1 = Unit) {
        targetIndexValue = finalPathOffsets.size - 1
    }
}

suspend fun PointerInputScope.dragging(
    onDragStart: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Block) -> Unit,
    blockSizeProvider: () -> Int,
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
            val currentBlock = change.position.toBlock(blockSizeProvider())
            if (currentBlock != drawingPosition.value) {
                drawingPosition.value = currentBlock
                onDrag(drawingPosition.value)
            }
        })
}

//fun Modifier.pointerInputCombine(
//    key1: Any?, blocks: List<suspend PointerInputScope.() -> Unit>, index: Int = 0
//): Modifier {
//    return when (index) {
//        blocks.size -> {
//            Modifier
//        }
//
//        else -> {
//            run {
//                then(pointerInput(key1 = key1, blocks[index])).then(
//                    pointerInputCombine(
//                        key1, blocks, index + 1
//                    )
//                )
//            }
//        }
//    }
//}

fun DrawScope.drawBackground(brickSize: Int, matrixW: Int, matrixH: Int, color: Color) {
    //background
    drawRect(
        color = LightColorScheme.background,
        topLeft = Offset.Zero,
        size = Size(matrixW.toFloat() * brickSize, matrixH.toFloat() * brickSize)
    )
    //Blocks
    (0 until matrixW).forEach { x ->
        (0 until matrixH).forEach { y ->
            drawUnitBlockOutline(brickSize, x, y, color)
        }
    }
}

fun DrawScope.drawEndPointWithOffset(offset: Offset, brickSize: Int, color: Color) {
    drawUnitBlockFilledFromOffset(brickSize, offset, color)
}

//last 2 block normal color
//then 3 alpha 60 blocks
//then 4 alpha 25 blocks
fun DrawScope.drawPassedBlocks(
    passed: List<Block>,
    brickSize: Int,
    isSearchDoneProvider: () -> Boolean,
    color: Color,
    currentColor: Color
) {
    val li = passed.lastIndex
    //draw passed
    passed.forEachIndexed { i, block ->

        drawUnitBlockFilled(
            brickSize, block.x, block.y,
            color = color
        )
    }
    if (isSearchDoneProvider()) return

    for (i in maxOf(0, li - 2 - 3 - 4) until passed.size) {
        drawUnitBlockFilled(
            brickSize, passed[i].x, passed[i].y,
            color = when (i) {
                in li - 1..li -> currentColor
                in li - 4..li - 2 -> currentColor.copy(alpha = .6f)
                in li - 8..li - 5 -> currentColor.copy(alpha = .2f)
                else -> color
            }
        )
    }
}

//fun DrawScope.drawBarrierWithAnimation(
//    barrierShow: List<Block>,
//    barrierHide: List<Block>,
//    others: List<Block>,
//    matrixW: Int,
//    matrixH: Int,
//    brickSize: Int,
//    color: Color,
//    bias: Float
//) {
//
//    //draw passed
//    others.forEach {
//        if (it.first in 0 until matrixW && it.second in 0 until matrixH) drawUnitBlockFilled(
//            brickSize, it.first, it.second, color
//        )
//    }
//
//    barrierShow.forEach { block ->
//        val absoluteOffset = Offset(brickSize * block.x.toFloat(), brickSize * block.y.toFloat())
//        val padding = brickSize * 0.05f
//        val outerSize = brickSize - padding * 2
//
//        drawRect(
//            color = color.copy(alpha = bias),
//            topLeft = absoluteOffset + Offset(padding, padding),
//            size = Size(outerSize, outerSize),
//            style = Fill
//        )
//    }
//    Timber.d("1 - bias:${1 - bias}")
//    barrierHide.forEach { block ->
//        val absoluteOffset = Offset(brickSize * block.x.toFloat(), brickSize * block.y.toFloat())
//        val padding = brickSize * 0.05f
//        val outerSize = brickSize - padding * 2
//
//        drawRect(
//            color = color.copy(alpha = 1 - bias),
//            topLeft = absoluteOffset + Offset(padding, padding),
//            size = Size(outerSize, outerSize),
//            style = Fill
//        )
//    }
//}

fun DrawScope.drawBarrier(
    barrier: List<Block>, matrixW: Int, matrixH: Int, brickSize: Int, color: Color, alpha: Float
) {
    //draw passed
    barrier.forEach {
        if (it.first in 0 until matrixW && it.second in 0 until matrixH) {
            drawUnitBlockFilled(
                brickSize, it.first, it.second, color, alpha
            )
        }
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
    brickSize: Int, x: Int, y: Int, color: Color = Color.Black, alpha: Float = 1f
) {
    val absoluteOffset = Offset(brickSize * x.toFloat(), brickSize * y.toFloat())
    val padding = brickSize * 0.05f
    val outerSize = brickSize - padding * 2

    drawRect(
        color = color,
        topLeft = absoluteOffset + Offset(padding, padding),
        size = Size(outerSize, outerSize),
        style = Fill,
        alpha = alpha
    )
}

//fun DrawScope.drawUnitBlockFilled(
//    brickSize: Int, x: Int, y: Int, color: Color = Color.Black, bias: Float
//) {
//    val absoluteOffset = Offset(brickSize * x.toFloat(), bias + brickSize * y.toFloat())
//    val padding = brickSize * 0.05f
//    val outerSize = brickSize - padding * 2
//
//    drawRect(
//        color = color.copy(alpha = 1 - bias),
//        topLeft = absoluteOffset + Offset(padding, padding),
//        size = Size(outerSize, outerSize),
//        style = Fill
//    )
//}

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