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

@HiltViewModel
class EmulatorViewModel @Inject constructor() :
    BaseViewModel<Contract.State, Contract.Event, Contract.Effect>() {

    private var job: Job? = null

    override fun createInitState(): Contract.State =
        Contract.State(
            status = Contract.Status.Idle,
            minSideBlockCnt = getBoardSize(BOARD_SIZE_DEFAULT.toFloat()),
            start = Block(0, 0),
            dest = Block(10, 10),
            barrier = hashSetOf(),
            searchStrategy = SearchDFS.instance,
            searchProcessDelay = getMovementSpeedDelay(MOVEMENT_SPEED_DEFAULT.toFloat())
        )

    override fun eventHandle(event: Contract.Event) {
        when (event) {
            is Contract.Event.OnSearchBtnClick -> {
                onStartButtonClick()
            }

            is Contract.Event.OnPauseBtnClick -> {
                onPauseButtonClick()
            }

            is Contract.Event.OnResetBtnClick -> {
                onStopButtonClick()
            }

            is Contract.Event.OnScreenMeasured -> {
                onScreenMeasured(event.heightInPx, event.widthInPx)
            }

            is Contract.Event.OnSizeSliderChange -> {
                onSizeSliderChange(event.value)
            }

            is Contract.Event.OnSpeedSliderChange -> {
                onSpeedSliderChange(event.value)
            }

            is Contract.Event.OnSearchStrategyChange -> {
                onSearchStrategyChange(event.strategy)
            }

            //barrier events
            is Contract.Event.OnDraggingStart -> {
                if (currentState.status != Contract.Status.Idle) return
                onDraggingStart(event.offset)
            }

            is Contract.Event.OnDraggingEnd -> {
                if (currentState.status !is Contract.DrawingType) return
                onDraggingEnd()
            }

            is Contract.Event.OnDragging -> {
                if (currentState.status !is Contract.DrawingType) return
                onBarrierDragging(event.block)
            }

            is Contract.Event.OnPressed -> {
                if (currentState.status != Contract.Status.Idle) return
                onPressed(event.offset)
            }

            is Contract.Event.OnTap -> {
                onTap(event.offset)
            }

            is Contract.Event.OnBarrierClearButtonClicked -> {
                onBarrierClearButtonClicked()
            }
        }
    }

    private fun onPressed(offset: Offset) {
        onDraggingStart(offset)
    }

    private fun onTap(offset: Offset) {
        val block = offset.toBlock(currentState.blockSize)
        if (currentState.barrier.contains(block))
            setState {
                copy(
                    status = Contract.Status.Idle,
                    barrier = barrier.toHashSet().apply { remove(block) }
                )
            }
        else
            setState {
                copy(
                    status = Contract.Status.Idle,
                    barrier = barrier.toHashSet().apply { add(block) }
                )
            }

    }

    private fun onDraggingEnd() {
        setState { copy(status = Contract.Status.Idle) }
    }

    private fun onDraggingStart(offset: Offset) {
        setState {
            copy(
                status = when (offset.toBlock(currentState.blockSize)) {
                    currentState.start -> Contract.Status.StartDragging
                    currentState.dest -> Contract.Status.DestDragging
                    else -> Contract.Status.BarrierDrawing
                }
            )
        }
    }

    private fun onBarrierDragging(block: Block) {
        if (!block.isValidBlock) return

        when (currentState.status) {
            is Contract.Status.StartDragging -> {
                if (currentState.barrier.contains(block) || block == currentState.dest) return
                setState { copy(start = block) }
            }

            is Contract.Status.DestDragging -> {
                if (currentState.barrier.contains(block) || block == currentState.start) return
                setState { copy(dest = block) }
            }

            is Contract.Status.BarrierDrawing -> {
                if (block == currentState.dest || block == currentState.start) return
                if (currentState.barrier.contains(block))
                    setState { copy(barrier = barrier.toHashSet().apply { remove(block) }) }
                else
                    setState { copy(barrier = barrier.toHashSet().apply { add(block) }) }
            }

            else -> {}
        }
    }

    private fun onBarrierClearButtonClicked() {
        setState { copy(barrier = hashSetOf()) }
        setEffect(Contract.Effect.OnBarrierCleaned)
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
        if (currentState.status == Contract.Status.Paused) {
            onSearchResume()
        } else {
            onSearchLaunch()
        }
        setState {
            copy(
                status = Contract.Status.Started,
                path = emptyList()
            )
        }
    }

    private fun onSearchFinish(isFound: Boolean, path: List<Block>?) {
        setState {
            copy(
                status = Contract.Status.SearchFinish,
                path = path ?: emptyList()
            )
        }

        setEffect(Contract.Effect.OnSearchFinish(isFound))
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
        setState { copy(status = Contract.Status.Paused) }
    }

    private fun onStopButtonClick() {
        onSearchStop()
        //reset
        setState {
            copy(
                status = Contract.Status.Idle,
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
        if (currentState.status == Contract.Status.Started) {
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