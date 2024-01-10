package com.timothy.searchemulator.ui.emulator

import androidx.lifecycle.viewModelScope
import com.timothy.searchemulator.ui.emulator.algo.MovementType
import com.timothy.searchemulator.ui.emulator.algo.SearchBFS
import com.timothy.searchemulator.model.MOVEMENT_SPEED_DEFAULT
import com.timothy.searchemulator.model.getMovementSpeedDelay
import com.timothy.searchemulator.ui.base.BaseViewModel
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
            status = Contract.Status.ConditionsMissing,
            minSideBlockCnt = 20,
            start = Block(3, 5),
            dest = Block(14, 14),
//            dest = Block(6, 5),
//            dest = Block(13, 13),
            barrier = listOf(
                Block(3,2), Block(2,2), Block(1,4),
                Block(6,2), Block(7,3), Block(7,4), Block(7,5), Block(8,6), Block(9,7),Block(9,8),Block(9,9),
                Block(2,10),Block(3,10),Block(4,10),Block(5,10),Block(7,10),Block(8,10),
                Block(12,11),Block(13,11),Block(14,11),Block(12,12),Block(12,13),Block(12,14),Block(12,15),Block(11,16),Block(10,17),Block(10,18),
                Block(14,12),Block(14,13),Block(14,16),Block(13,14),
            ),
            searchStrategy = SearchDFS(),
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

            is Contract.Event.OnBlockPressed -> {}
            is Contract.Event.OnScreenMeasured -> {
                onScreenMeasured(event.heightInPx, event.widthInPx)
            }

            is Contract.Event.OnSizeSliderChange ->{
                onSizeSliderChange(event.value.toInt())
            }
            is Contract.Event.OnSpeedSliderChange ->{
                onSpeedSliderChange(event.value)
            }

            is Contract.Event.OnSearchStrategyChange->{
                onSearchStrategyChange(event.strategy)
            }
        }
    }

    private fun onScreenMeasured(height: Int, width: Int) {
        val blockSize = minOf(width, height) / currentState.minSideBlockCnt

        val matrixW = (width / blockSize)
        val matrixH = (height / blockSize)
        val status = if (currentState.start == null
            || currentState.start?.first !in 0 until matrixW
            || currentState.start?.second !in 0 until matrixH
            || currentState.dest == null
            || currentState.dest?.first !in 0 until matrixW
            || currentState.dest?.second !in 0 until matrixH
        ) Contract.Status.ConditionsMissing else Contract.Status.Idle

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

    private fun onSearchPause(){
        currentState.searchStrategy.onPause()
    }

    private fun onSearchResume(){
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

    private fun onSearchLaunch(){
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

                    onPause = {
                        Timber.d("onPause")
                    },
                    onFinish = { isFound, path ->
                        onSearchFinish(isFound, path)
                    }
                )
        }
    }

    private fun onSearchStop(){
        currentState.searchStrategy.onStop()
        job?.cancel()
    }

    private fun onStartButtonClick() {
        if (currentState.status == Contract.Status.Paused) {
            onSearchResume()
        } else {
            onSearchLaunch()
        }
        setState { copy(
            status = Contract.Status.Started,
            path = emptyList()
        )}
    }

    private fun onSearchFinish(isFound: Boolean, path: List<Block>?) {
        setState { copy(
            status = Contract.Status.SearchFinish,
            path = path?: emptyList()
        )}

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
                setState { copy(passed = currentState.passed.toMutableList().apply { remove(block) }) }
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

    private fun onSizeSliderChange(size:Int){
        val blockSize = minOf(currentState.width, currentState.height) / size
        val matrixW = (currentState.width / blockSize)
        val matrixH = (currentState.height / blockSize)

        setState{copy(
            minSideBlockCnt = size,
            blockSize = blockSize,
            matrixW = matrixW,
            matrixH = matrixH
        )}
    }

    private fun onSpeedSliderChange(speed:Float){
        setState{copy(searchProcessDelay = getMovementSpeedDelay(speed))}
        if(currentState.status == Contract.Status.Started) {
            onSearchPause()
            onSearchResume()
        }
    }

    private fun onSearchStrategyChange(strategy:SearchAlgo){
        if(currentState.searchStrategy.getType() == strategy) return

        onSearchStop()
        setState {
            copy(
                searchStrategy = when(strategy){
                    SearchAlgo.SEARCH_BFS-> SearchBFS()
                    SearchAlgo.SEARCH_DFS-> SearchDFS()
                }
            )
        }
    }
}