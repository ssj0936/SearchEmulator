package com.timothy.searchemulator.ui.emulator

import com.timothy.searchemulator.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmulatorViewModel @Inject constructor() :
    BaseViewModel<Contract.State, Contract.Event, Contract.Effect>() {
    override fun createInitState(): Contract.State =
        Contract.State(
            status = Contract.Status.ConditionsMissing,
            start = Block(3,5),
            dest = Block(14,14)
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
        }
    }

    private fun onScreenMeasured(height: Int, width: Int) {
        val blockSize = minOf(width, height) / 15

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

    private fun onStartButtonClick() {
        setState { copy(status = Contract.Status.Started) }
        val start = currentState.start!!
        val dest = currentState.dest!!
        val sizeW = currentState.matrixW
        val sizeH = currentState.matrixH

        currentState.searchStrategy.search(
            sizeW = sizeW,
            sizeH = sizeH,
            start = start,
            dest = dest,
            onProcess = { movementType, block ->
                onSearchProcessStep(movementType, block)
            },
            onFinish = { isFound ->
                onSearchFinish(isFound)
            }
        )
    }

    private fun onSearchFinish(isFound: Boolean) {
        setState { copy(status = Contract.Status.SearchFinish) }
        setEffect(Contract.Effect.OnSearchFinish(isFound))
    }

    private fun onSearchProcessStep(
        movementType: MovementType,
        block: Block
    )/* = viewModelScope.launch*/ {
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
        setState { copy(status = Contract.Status.Idle) }
    }

    private fun onStopButtonClick() {
        setState {
            copy(
                status = Contract.Status.Idle,
                path = mutableListOf()
            )
        }
    }
}