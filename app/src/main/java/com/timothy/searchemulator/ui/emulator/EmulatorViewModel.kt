package com.timothy.searchemulator.ui.emulator

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.timothy.searchemulator.model.AlgoDescriptionRepository
import com.timothy.searchemulator.model.BOARD_SIZE_DEFAULT
import com.timothy.searchemulator.model.Description
import com.timothy.searchemulator.ui.emulator.algo.MovementType
import com.timothy.searchemulator.ui.emulator.algo.SearchBFS
import com.timothy.searchemulator.model.MOVEMENT_SPEED_DEFAULT
import com.timothy.searchemulator.model.dirs
import com.timothy.searchemulator.model.getBoardSize
import com.timothy.searchemulator.model.getMovementSpeedDelay
import com.timothy.searchemulator.ui.base.BaseViewModel
import com.timothy.searchemulator.ui.base.toBlock
import com.timothy.searchemulator.ui.emulator.algo.SearchAlgo
import com.timothy.searchemulator.ui.emulator.algo.SearchDFS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

import com.timothy.searchemulator.ui.emulator.Contract.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.LinkedList

@HiltViewModel
class EmulatorViewModel @Inject constructor(
    private val movementRecordManager: MovementRecordManager,
    private val descriptionRepository: AlgoDescriptionRepository
) :
    BaseViewModel<State, Status, Event, Effect>() {

    private var job: Job? = null

    override fun createInitState(): State =
        State(
            minSideBlockCnt = getBoardSize(BOARD_SIZE_DEFAULT),
            start = Block(0, 0),
            dest = Block(10, 10),
            barrier = hashSetOf(),
            searchStrategy = SearchDFS.instance,
            searchProcessDelay = getMovementSpeedDelay(MOVEMENT_SPEED_DEFAULT),
            lastOperationType = OperationType.NORMAL
        )

    override fun createInitStatus(): Status = Status.Idle

    override fun eventHandle(event: Event) {
        when (event) {
            is Event.OnSearchBtnClick -> {
                onStartButtonClick()
            }

            is Event.OnPauseBtnClick -> {
                onPauseButtonClick()
            }

            is Event.OnResetBtnClick -> {
                onStopButtonClick()
            }

            is Event.OnScreenMeasured -> {
                onScreenMeasured(event.heightInPx, event.widthInPx)
            }

            is Event.OnSizeSliderChange -> {
                onBoardSizeChange(event.value)
            }

            is Event.OnSpeedSliderChange -> {
                onSearchSpeedChange(event.value)
            }

            is Event.OnSearchStrategyChange -> {
                onSearchStrategyChange(event.strategy)
            }

            //barrier events
            is Event.OnDraggingStart -> {
                if (currentStatus != Status.Idle) return
                onDraggingStart(event.offset)
            }

            is Event.OnDraggingEnd -> {
                if (currentStatus !is DrawingType) return
                onDraggingEnd()
            }

            is Event.OnDragging -> {
                if (currentStatus !is DrawingType) return
                onDragging(event.block)
            }

            is Event.OnPressed -> {
                if (currentStatus != Status.Idle) return
                onPressed(event.offset)
            }

            is Event.OnTap -> {
                if (currentStatus !is DrawingType) return
                onTap(event.offset)
            }

            is Event.OnBarrierClearButtonClicked -> {
                onBarrierClearButtonClicked()
            }

            is Event.OnBarrierUndoButtonClicked -> {
                onBarrierUndoButtonClicked()
            }

            is Event.OnBarrierRedoButtonClicked -> {
                onBarrierRedoButtonClicked()
            }

            is Event.OnMazeGeneratePressed -> {
                onMazeGeneratePressed()
            }
        }
    }

    private fun onPressed(offset: Offset) {
        onDraggingStart(offset)
    }

    private fun onTap(offset: Offset) {
        val block = offset.toBlock(currentState.blockSizePx)
        if (block == currentState.start || block == currentState.dest) return

        movementRecordManager.record(block)
        movementRecordManager.finishRecording()

        if (currentState.barrier.contains(block)) {
            setState {
                copy(
                    barrier = barrier.toHashSet().apply { remove(block) }
                )
            }
            setStatus(Status.Idle)
        } else {
            setState {
                copy(
                    barrier = barrier.toHashSet().apply { add(block) }
                )
            }
            setStatus(Status.Idle)
        }

    }

    private fun onDraggingEnd() {
        if (currentStatus == Status.StartDragging)
            movementRecordManager.record(currentState.start!!)
        else if (currentStatus == Status.DestDragging)
            movementRecordManager.record(currentState.dest!!)
        movementRecordManager.finishRecording()

        setStatus(Status.Idle)
    }

    private fun onDraggingStart(offset: Offset) {
        //define current status rely on pointed Block
        val newStatus = when (offset.toBlock(currentState.blockSizePx)) {
            currentState.start -> Status.StartDragging
            currentState.dest -> Status.DestDragging
            else -> Status.BarrierDrawing
        }
        if (currentStatus == newStatus) return

        //MovementRecordManager init recording
        movementRecordManager.startRecording(
            operationUnit = when (newStatus) {
                Status.StartDragging -> OperationUnit.MoveStart()
                Status.DestDragging -> OperationUnit.MoveDest()
                Status.BarrierDrawing -> OperationUnit.DrawBarrier()
                else -> {
                    throw IllegalArgumentException("dragging in inappropriate status:$newStatus")
                }
            }
        )

        if (newStatus == Status.StartDragging)
            movementRecordManager.record(currentState.start!!)
        else if (newStatus == Status.DestDragging)
            movementRecordManager.record(currentState.dest!!)

        setState {
            copy(lastOperationType = OperationType.NORMAL)
        }
        setStatus(newStatus)
    }

    private fun onDragging(block: Block) {
        if (!block.isValidBlock) return

        when (currentStatus) {
            is Status.StartDragging -> {
                if (currentState.barrier.contains(block) || block == currentState.dest) return
                setState { copy(start = block) }
            }

            is Status.DestDragging -> {
                if (currentState.barrier.contains(block) || block == currentState.start) return
                setState { copy(dest = block) }
            }

            is Status.BarrierDrawing -> {
                if (block == currentState.dest || block == currentState.start) return
                if (currentState.barrier.contains(block))
                    setState { copy(barrier = barrier.toHashSet().apply { remove(block) }) }
                else
                    setState { copy(barrier = barrier.toHashSet().apply { add(block) }) }

                movementRecordManager.record(block)
            }

            else -> {}
        }
    }

    private fun onBarrierClearButtonClicked() {
        if (currentState.barrier.isEmpty()) return

        movementRecordManager.startRecording(operationUnit = OperationUnit.DrawBarrier())
            .record(currentState.barrier.toList())
            .finishRecording()

        setState { copy(barrier = hashSetOf()) }
//        setEffect(Effect.OnBarrierCleaned)
    }

    private fun onBarrierUndoButtonClicked() {
        if (!movementRecordManager.hasUndoMovement()) return
        val lastOperation = movementRecordManager.undoLastMovement() ?: return

        when (lastOperation) {
            is OperationUnit.MoveStart -> {
                setState {
                    copy(
                        start = lastOperation.from,
                        lastOperationType = OperationType.UNDO_START
                    )
                }
            }

            is OperationUnit.MoveDest -> {
                setState {
                    copy(
                        dest = lastOperation.from,
                        lastOperationType = OperationType.UNDO_DEST
                    )
                }
            }

            is OperationUnit.DrawBarrier -> {
                val currentBarrier = currentState.barrier.toHashSet()
                lastOperation.drawPath.reversed().forEach { block ->
                    if (currentBarrier.contains(block))
                        currentBarrier.remove(block)
                    else
                        currentBarrier.add(block)
                }
                setState {
                    copy(
                        barrier = currentBarrier,
                        lastOperationType = OperationType.UNDO_BARRIERS
                    )
                }
            }

            is OperationUnit.GenerateMaze -> {
                val currentBarrier = currentState.barrier.toHashSet()
                lastOperation.barriersDiff.reversed().forEach { block ->
                    if (currentBarrier.contains(block))
                        currentBarrier.remove(block)
                    else
                        currentBarrier.add(block)
                }
                setState {
                    copy(
                        barrier = currentBarrier,
                        start = lastOperation.startFrom,
                        dest = lastOperation.destFrom,
                        lastOperationType = OperationType.GENERATE_MAZE
                    )
                }
            }

            else -> {/*throw IllegalStateException("no movement to undo")*/
            }
        }
        setEffect(Effect.OnUndoEvent(operationUnit = lastOperation))

    }

    private fun onBarrierRedoButtonClicked() {
        if (!movementRecordManager.hasRedoMovement()) return
        val lastOperation = movementRecordManager.redoLastMovement() ?: return

        when (lastOperation) {
            is OperationUnit.MoveStart -> {
                setState {
                    copy(
                        start = lastOperation.to,
                        lastOperationType = OperationType.REDO_START
                    )
                }
            }

            is OperationUnit.MoveDest -> {
                setState {
                    copy(
                        dest = lastOperation.to,
                        lastOperationType = OperationType.REDO_DEST
                    )
                }
            }

            is OperationUnit.DrawBarrier -> {
                val currentBarrier = currentState.barrier.toHashSet()
                lastOperation.drawPath.forEach { block ->
                    if (currentBarrier.contains(block))
                        currentBarrier.remove(block)
                    else
                        currentBarrier.add(block)
                }
                setState {
                    copy(
                        barrier = currentBarrier,
                        lastOperationType = OperationType.REDO_BARRIERS
                    )
                }
            }

            is OperationUnit.GenerateMaze -> {
                val currentBarrier = currentState.barrier.toHashSet()
                lastOperation.barriersDiff.forEach { block ->
                    if (currentBarrier.contains(block))
                        currentBarrier.remove(block)
                    else
                        currentBarrier.add(block)
                }
                setState {
                    copy(
                        barrier = currentBarrier,
                        start = lastOperation.startTo,
                        dest = lastOperation.destTo,
                        lastOperationType = OperationType.GENERATE_MAZE
                    )
                }
            }

            else -> {/*throw IllegalStateException("no movement to redo")*/
            }
        }
        setEffect(Effect.OnRedoEvent(operationUnit = lastOperation))

    }

    /*
    * determine whether this block out of the range of board
    */
    private val Block.isValidBlock: Boolean
        get() = (this.x in 0 until currentState.matrixW
                && this.y in 0 until currentState.matrixH)

    private fun onScreenMeasured(height: Int, width: Int) {
        val blockSize = minOf(width, height) / currentState.minSideBlockCnt
        val matrixW = (width / blockSize)
        val matrixH = (height / blockSize)

        setState {
            copy(
                widthPx = width,
                heightPx = height,
                blockSizePx = blockSize,
                matrixW = matrixW,
                matrixH = matrixH,
            )
        }
    }

    private fun onSearchPause() {
        currentState.searchStrategy.onPause()
    }

    private fun onSearchResume() {
        currentState.searchStrategy.onResume()

        job?.cancel()
        job = viewModelScope.launch {
            currentState.searchStrategy
                .search(
                    delayBetweenSteps = currentState.searchProcessDelay,
                    onProcess = { movementType, block ->
                        onSearchProcessStep(movementType, block)
                    },

                    onPause = {
                        Timber.d("onPause")
                    },
                    onFinish = { isFound, path ->
                        onSearchFinish(isFound, path)
                    }
                )
        }
    }

    private fun onSearchLaunch() {
        val start = currentState.start!!
        val dest = currentState.dest!!
        val sizeW = currentState.matrixW
        val sizeH = currentState.matrixH
        val barrier = currentState.barrier

        job?.cancel()
        job = viewModelScope.launch {
            currentState.searchStrategy.setSizeW(sizeW)
                .setSizeH(sizeH)
                .setStart(start)
                .setDest(dest)
                .setBarriers(barrier)
                .init()
                .search(
                    delayBetweenSteps = currentState.searchProcessDelay,

                    onProcess = { movementType, block ->
                        onSearchProcessStep(movementType, block)
                    },
                    onPause = {},
                    onFinish = { isFound, path ->
                        onSearchFinish(isFound, path)
                    }
                )
        }
    }

    private fun onSearchStop() {
        currentState.searchStrategy.onStop()
        job?.cancel()
    }

    private fun onStartButtonClick() {
        if (currentStatus == Status.Paused) {
            onSearchResume()
        } else {
            onSearchLaunch()
        }
        setState { copy(path = emptyList()) }
        setStatus(Status.Started)
    }

    private fun onSearchFinish(isFound: Boolean, path: List<Block>?) {
        setState { copy(path = path ?: emptyList()) }
        setStatus(Status.SearchFinish)
        setEffect(Effect.OnSearchFinish(isFound))
    }

    private fun onSearchProcessStep(
        movementType: MovementType,
        block: Block
    ) = viewModelScope.launch {
        when (movementType) {
            MovementType.MOVEMENT_STEP_IN -> {
                setState { copy(passed = currentState.passed.toMutableList().apply { add(block) }) }
            }

            MovementType.MOVEMENT_REVERSE -> {
                setState {
                    copy(
                        passed = currentState.passed.toMutableList().apply { remove(block) })
                }
            }
        }
    }

    private fun onPauseButtonClick() {
        onSearchPause()
        setStatus(Status.Paused)
    }

    private fun onStopButtonClick() {
        onSearchStop()
        //reset
        setState {
            copy(
                passed = emptyList(),
                path = emptyList()
            )
        }
        setStatus(Status.Idle)

    }

    private fun onBoardSizeChange(size: Int) {
        val minSideBlockCnt = getBoardSize(size)
        val blockSize = minOf(currentState.widthPx, currentState.heightPx) / minSideBlockCnt
        val matrixW = (currentState.widthPx / blockSize)
        val matrixH = (currentState.heightPx / blockSize)

        setState {
            copy(
                minSideBlockCnt = minSideBlockCnt,
                blockSizePx = blockSize,
                matrixW = matrixW,
                matrixH = matrixH,
            )
        }
    }

    private fun onSearchSpeedChange(speed: Int) {
        setState { copy(searchProcessDelay = getMovementSpeedDelay(speed)) }
        if (currentStatus == Status.Started) {
            onSearchPause()
            onSearchResume()
        }
    }

    private fun onSearchStrategyChange(strategy: SearchAlgo) {
        if (currentState.searchStrategy.getType() == strategy) return

        onSearchStop()
        setState {
            copy(
                searchStrategy = when (strategy) {
                    SearchAlgo.SEARCH_BFS -> SearchBFS.instance
                    SearchAlgo.SEARCH_DFS -> SearchDFS.instance
                }
            )
        }
    }

    private fun onMazeGeneratePressed() {
        val mazeBarrier = MazeGeneratorImpl()
            .setWidth(currentState.matrixW)
            .setHeight(currentState.matrixH)
            .setIsSurroundedByBarriers(true/*(0..1).random() == 1*/) //random
            .generateBarriers()


        //XOR for getting diff
        val barriersDiff = hashSetOf<Block>().apply {
            addAll(currentState.barrier.filter { !mazeBarrier.contains(it) })
            addAll(mazeBarrier.filter { !currentState.barrier.contains(it) })
        }

        val newStart = if (currentState.start != null && mazeBarrier.contains(currentState.start)) {
            mazeBarrier.getNearestPathBlock(
                currentState.start!!,
                currentState.matrixW,
                currentState.matrixH
            ) ?: throw IllegalArgumentException("no place to put start")
        } else {
            currentState.start
        }

        val newDest = if (currentState.dest != null && mazeBarrier.contains(currentState.dest)) {
            mazeBarrier.getNearestPathBlock(
                currentState.dest!!,
                currentState.matrixW,
                currentState.matrixH
            ) ?: throw IllegalArgumentException("no place to put dest")
        } else {
            currentState.dest
        }

        movementRecordManager.startRecording(
            OperationUnit.GenerateMaze(
                startFrom = currentState.start,
                startTo = newStart,
                destFrom = currentState.dest,
                destTo = newDest
            )
        ).record(barriersDiff).finishRecording()

        //state update
        setState {
            copy(
                barrier = mazeBarrier,
                start = newStart,
                dest = newDest,
                lastOperationType = OperationType.GENERATE_MAZE
            )
        }
    }

    private fun HashSet<Block>.getNearestPathBlock(center: Block, width: Int, height: Int): Block? {
        val queue = LinkedList<Block>().apply { offer(center) }
        val visited = hashSetOf(center)

        while (queue.isNotEmpty()) {
            val pop = queue.poll()!!

            if (!this.contains(pop))
                return pop

            for (dir in dirs) {
                val x = pop.x + dir[0]
                val y = pop.y + dir[1]
                if (x in 0 until width && y in 0 until height && !visited.contains(Block(x, y))) {
                    queue.offer(Block(x, y))
                }
            }
        }
        return null
    }

    suspend fun getAlgoDescription(): Description = viewModelScope.async(Dispatchers.IO) {
        descriptionRepository.getDescriptions(currentState.searchStrategy.getType())
    }.await()


    val blockSizeProvider: () -> Int = { currentState.blockSizePx }
}