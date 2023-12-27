package com.timothy.searchemulator.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface BaseState
interface BaseEvent
interface BaseEffect

abstract class BaseViewModel<State:BaseState, Event:BaseEvent, Effect:BaseEffect>:ViewModel(){

    private val initState by lazy { createInitState() }

    private val _state:MutableStateFlow<State> = MutableStateFlow(initState)
    val state = _state.asStateFlow()
    val currentState:State
        get() = _state.value

    private val _event:MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    private val _effect:MutableSharedFlow<Effect> = MutableSharedFlow()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            event.collect{eventHandle(it)}
        }
    }


    fun setState(reduce:State.()->State){
        val newState = _state.value.reduce()
        _state.value = newState
    }
    fun setEvent(event: Event) = viewModelScope.launch {
        _event.emit(event)
    }

    fun setEffect(effect: Effect) = viewModelScope.launch {
        _effect.emit(effect)
    }

    abstract fun createInitState():State
    abstract fun eventHandle(event: Event)
}