package com.timothy.searchemulator.ui.emulator

import com.timothy.searchemulator.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmulatorViewModel @Inject constructor():BaseViewModel<Contract.State, Contract.Event, Contract.Effect>(){
    override fun createInitState(): Contract.State = Contract.State(status = Contract.Status.ConditionsMissing)

    override fun eventHandle(event: Contract.Event) {
        when(event){
            is Contract.Event.OnSearchBtnClick->{onStartButtonClick()}
            is Contract.Event.OnPauseBtnClick->{onPauseButtonClick()}
            is Contract.Event.OnResetBtnClick->{onStopButtonClick()}
            is Contract.Event.OnBlockPressed -> {}
            is Contract.Event.OnScreenMeasured->{
                onScreenMeasured(event.heightInDp, event.widthInDp)
            }
        }
    }

    private fun onScreenMeasured(height:Float, width:Float){
        setState { copy(
            width = width,
            height = height,
            blockSize = minOf(width, height)/15
        ) }
    }

    private fun onStartButtonClick(){
        setState { copy(status = Contract.Status.Started) }
    }

    private fun onPauseButtonClick(){
        setState { copy(status = Contract.Status.Idle) }
    }

    private fun onStopButtonClick(){
        setState { copy(status = Contract.Status.Idle) }
    }
}