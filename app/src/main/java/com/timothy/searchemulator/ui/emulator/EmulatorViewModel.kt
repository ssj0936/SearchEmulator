package com.timothy.searchemulator.ui.emulator

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.timothy.searchemulator.model.BOARD_SIZE_DEFAULT
import com.timothy.searchemulator.ui.emulator.algo.MovementType
import com.timothy.searchemulator.ui.emulator.algo.SearchBFS
import com.timothy.searchemulator.model.MOVEMENT_SPEED_DEFAULT
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

import com.timothy.searchemulator.ui.emulator.MovementType.*
import com.timothy.searchemulator.ui.emulator.MovementType.MOVE_DEST
import com.timothy.searchemulator.ui.emulator.MovementType.MOVE_START

import com.timothy.searchemulator.ui.emulator.Contract.*

@HiltViewModel
class EmulatorViewModel @Inject constructor(
    private val movementRecordManager: MovementRecordManager
) :
    BaseViewModel<State, Event, Effect>() {

    private var job: Job? = null

    override fun createInitState(): State =
        State(
            status = Status.Idle,
            minSideBlockCnt = getBoardSize(BOARD_SIZE_DEFAULT.toFloat()),
            start = Block(0, 0),
            dest = Block(10, 10),
            barrier = hashSetOf(),
            searchStrategy = SearchDFS.instance,
            searchProcessDelay = getMovementSpeedDelay(MOVEMENT_SPEED_DEFAULT.toFloat())
        )

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
                onSizeSliderChange(event.value)
            }

            is Event.OnSpeedSliderChange -> {
                onSpeedSliderChange(event.value)
            }

            is Event.OnSearchStrategyChange -> {
                onSearchStrategyChange(event.strategy)
            }

            //barrier events
            is Event.OnDraggingStart -> {
                if (currentState.status != Status.Idle) return
                onDraggingStart(event.offset)
            }

            is Event.OnDraggingEnd -> {
                if (currentState.status !is DrawingType) return
                onDraggingEnd()
            }

            is Event.OnDragging -> {
                if (currentState.status !is DrawingType) return
                onBarrierDragging(event.block)
            }

            is Event.OnPressed -> {
                if (currentState.status != Status.Idle) return
                onPressed(event.offset)
            }

            is Event.OnTap -> {
                if (currentState.status !is DrawingType) return
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
        }
    }

    private fun onPressed(offset: Offset) {
        onDraggingStart(offset)
    }

    private fun onTap(offset: Offset) {
        val block = offset.toBlock(currentState.blockSize)

        movementRecordManager.record(block)
        movementRecordManager.stopRecording()

        if (currentState.barrier.contains(block))
            setState {
                copy(
                    status = Status.Idle,
                    barrier = barrier.toHashSet().apply { remove(block) }
                )
            }
        else
            setState {
                copy(
                    status = Status.Idle,
                    barrier = barrier.toHashSet().apply { add(block) }
                )
            }

    }

    private fun onDraggingEnd() {
        if(currentState.status==Status.StartDragging)
            movementRecordManager.record(currentState.start!!)
        else if(currentState.status==Status.DestDragging)
            movementRecordManager.record(currentState.dest!!)
        movementRecordManager.stopRecording()

        setState { copy(status = Status.Idle) }
    }

    private fun onDraggingStart(offset: Offset) {
        val newStatus = when (offset.toBlock(currentState.blockSize)) {
            currentState.start -> Status.StartDragging
            currentState.dest -> Status.DestDragging
            else -> Status.BarrierDrawing
        }
        if (currentState.status == newStatus) return

        //MovementRecordManager init recording
        movementRecordManager.startRecording(
            type = when (newStatus) {
                Status.StartDragging -> MOVE_START
                Status.DestDragging -> MOVE_DEST
                else -> DRAWING_BARRIER
            }
        )

        if(newStatus==Status.StartDragging)
            movementRecordManager.record(currentState.start!!)
        else if(newStatus==Status.DestDragging)
            movementRecordManager.record(currentState.dest!!)

        setState {
            copy(status = newStatus)
        }
    }

    private fun onBarrierDragging(block: Block) {
        if (!block.isValidBlock) return

        when (currentState.status) {
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
        if(currentState.barrier.isEmpty()) return

        movementRecordManager.startRecording(DRAWING_BARRIER)
            .record(currentState.barrier.toList())
            .stopRecording()

        setState { copy(barrier = hashSetOf()) }
//        setEffect(Effect.OnBarrierCleaned)
    }

    private fun onBarrierUndoButtonClicked() {
        if(!movementRecordManager.hasUndoMovement()) return

        when(val movement = movementRecordManager.undoLastMovement()){
            is Movement.MoveStart->{
                setState { copy(start = movement.from) }
            }
            is Movement.MoveDest->{
                setState { copy(dest = movement.from) }
            }
            is Movement.DrawBarrier->{
                val currentBarrier = currentState.barrier.toHashSet()
                movement.drawPath.reversed().forEach {block->
                    if(currentBarrier.contains(block))
                        currentBarrier.remove(block)
                    else
                        currentBarrier.add(block)
                }
                setState { copy(barrier = currentBarrier) }
            }
            else -> {/*throw IllegalStateException("no movement to undo")*/}
        }
    }

    private fun onBarrierRedoButtonClicked() {
        if(!movementRecordManager.hasRedoMovement()) return

        when(val movement = movementRecordManager.redoLastMovement()){
            is Movement.MoveStart->{
                setState { copy(start = movement.to) }
            }
            is Movement.MoveDest->{
                setState { copy(dest = movement.to) }
            }
            is Movement.DrawBarrier->{
                val currentBarrier = currentState.barrier.toHashSet()
                movement.drawPath.forEach {block->
                    if(currentBarrier.contains(block))
                        currentBarrier.remove(block)
                    else
                        currentBarrier.add(block)
                }
                setState { copy(barrier = currentBarrier) }
            }
            else -> {/*throw IllegalStateException("no movement to redo")*/}
        }
    }

    private val Block.isValidBlock: Boolean
        get() = isValidBlock(currentState.matrixW, currentState.matrixH)

    private fun Block.isValidBlock(w: Int, h: Int): Boolean {
        return this.first in 0 until w
                && this.second in 0 until h
    }

    private fun onScreenMeasured(height: Int, width: Int) {
        val blockSize = minOf(width, height) / currentState.minSideBlockCnt
        val matrixW = (width / blockSize)
        val matrixH = (height / blockSize)

        setState {
            copy(
                status = status,
                width = width,
                height = height,
                blockSize = blockSize,
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
                .setBarriers(barrier.toList())
                .init()
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

    private fun onSearchStop() {
        currentState.searchStrategy.onStop()
        job?.cancel()
    }

    private fun onStartButtonClick() {
        if (currentState.status == Status.Paused) {
            onSearchResume()
        } else {
            onSearchLaunch()
        }
        setState {
            copy(
                status = Status.Started,
                path = emptyList()
            )
        }
    }

    private fun onSearchFinish(isFound: Boolean, path: List<Block>?) {
        setState {
            copy(
                status = Status.SearchFinish,
                path = path ?: emptyList()
            )
        }

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
        setState { copy(status = Status.Paused) }
    }

    private fun onStopButtonClick() {
        onSearchStop()
        //reset
        setState {
            copy(
                status = Status.Idle,
                passed = emptyList(),
                path = emptyList()
            )
        }
    }

    private fun onSizeSliderChange(size: Float) {
        val minSideBlockCnt = getBoardSize(size)
        val blockSize = minOf(currentState.width, currentState.height) / minSideBlockCnt
        val matrixW = (currentState.width / blockSize)
        val matrixH = (currentState.height / blockSize)

        setState {
            copy(
                minSideBlockCnt = minSideBlockCnt,
                blockSize = blockSize,
                matrixW = matrixW,
                matrixH = matrixH,
            )
        }
    }

    private fun onSpeedSliderChange(speed: Float) {
        setState { copy(searchProcessDelay = getMovementSpeedDelay(speed)) }
        if (currentState.status == Status.Started) {
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
}