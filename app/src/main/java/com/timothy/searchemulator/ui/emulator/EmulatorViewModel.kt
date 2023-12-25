package com.timothy.searchemulator.ui.emulator

import com.timothy.searchemulator.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmulatorViewModel @Inject constructor():BaseViewModel<Contract.State, Contract.Event, Contract.Effect>(){
    override fun createInitState(): Contract.State = Contract.State(status = Contract.Status.ConditionsMissing)

    override fun eventHandle(event: Contract.Event) {
        TODO("Not yet implemented")
    }
}