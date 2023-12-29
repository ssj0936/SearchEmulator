package com.timothy.searchemulator.ui.emulator

import androidx.lifecycle.viewModelScope
import com.timothy.searchemulator.model.getMovementSpeedDelay
import com.timothy.searchemulator.ui.base.BaseViewModel
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
            start = Block(3, 5),
            dest = Block(14, 14)
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
                    state = currentState,
                    onProcess = { movementType, block ->
                        onSearchProcessStep(movementType, block)
                    },

                    onPause = {
                        Timber.d("onPause")
                    },
                    onFinish = { isFound ->
                        onSearchFinish(isFound)
                    }
                )
        }
    }

    private fun onSearchLaunch(){
        val start = currentState.start!!
        val dest = currentState.dest!!
        val sizeW = currentState.matrixW
        val sizeH = currentState.matrixH

        job?.cancel()
        job = viewModelScope.launch {
            currentState.searchStrategy.setSizeW(sizeW)
                .setSizeH(sizeH)
                .setStart(start)
                .setDest(dest)
                .init()
                .search(
                    state = currentState,

                    onProcess = { movementType, block ->
                        onSearchProcessStep(movementType, block)
                    },

                    onPause = {
                        Timber.d("onPause")
                    },
                    onFinish = { isFound ->
                        onSearchFinish(isFound)
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
        setState { copy(status = Contract.Status.Started) }
    }

    private fun onSearchFinish(isFound: Boolean) {
        setState { copy(status = Contract.Status.SearchFinish) }
        setEffect(Contract.Effect.OnSearchFinish(isFound))
    }

    private fun onSearchProcessStep(
        movementType: MovementType,
        block: Block
    ) = viewModelScope.launch {
        when (movementType) {
            MovementType.MOVEMENT_STEP_IN -> {
                setState { copy(path = currentState.path.toMutableList().apply { add(block) }) }
            }

            MovementType.MOVEMENT_REVERSE -> {
                setState { copy(path = currentState.path.toMutableList().apply { remove(block) }) }
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
                path = mutableListOf()
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
}