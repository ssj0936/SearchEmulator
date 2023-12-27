package com.timothy.searchemulator.ui.emulator

import com.timothy.searchemulator.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EmulatorViewModel @Inject constructor():BaseViewModel<Contract.State, Contract.Event, Contract.Effect>(){
    override fun createInitState(): Contract.State = Contract.State(status = Contract.Status.ConditionsMissing)

    fun refresh(){

    }

    override fun eventHandle(event: Contract.Event) {
        when(event){
            is Contract.Event.OnSearchBtnClick->{onStartButtonClick()}
            is Contract.Event.OnPauseBtnClick->{onPauseButtonClick()}
            is Contract.Event.OnResetBtnClick->{onStopButtonClick()}
            is Contract.Event.OnBlockPressed -> {}
            is Contract.Event.OnScreenMeasured->{
                onScreenMeasured(event.heightInPx, event.widthInPx)
            }
        }
    }

    private fun onScreenMeasured(height:Int, width:Int){
        val blockSize = minOf(width, height)/15

        setState { copy(
            width = width,
            height = height,
            blockSize = blockSize,
            matrixW = (width/blockSize),
            matrixH = (height/blockSize)
        ) }

        Timber.d("${state.value}")
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