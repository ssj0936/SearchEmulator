package com.timothy.searchemulator.ui.emulator

import com.timothy.searchemulator.model.MOVEMENT_SPEED_DEFAULT
import com.timothy.searchemulator.model.getMovementSpeedDelay
import com.timothy.searchemulator.ui.base.BaseEffect
import com.timothy.searchemulator.ui.base.BaseEvent
import com.timothy.searchemulator.ui.base.BaseState

typealias Block = Pair<Int,Int>

class Contract{
    sealed class Event:BaseEvent{
        object OnSearchBtnClick:Event()
        object OnPauseBtnClick:Event()
        object OnResetBtnClick:Event()
        data class OnBlockPressed(val block:Block):Event()
        data class OnScreenMeasured(val widthInPx:Int, val heightInPx:Int):Event()

        data class OnSizeSliderChange(val value:Float):Event()
        data class OnSpeedSliderChange(val value:Float):Event()
    }


    data class State(
        val status:Status,

        /*in px*/
        val width:Int = 0,
        val height:Int = 0,
        val blockSize:Int = 0,
        val minSideBlockCnt:Int = 20,

        val matrixW:Int = 0,
        val matrixH:Int = 0,

        val start:Block? = null,
        val dest:Block? = null,
        val barrier:List<Block> = mutableListOf(),

        val searchStrategy: SearchStrategy = SearchBFS(),
        val pathCnt:Int = 0,
        val passed:List<Block> = mutableListOf(),
        val path:List<Block> = mutableListOf(),

        val searchProcessDelay:Long = getMovementSpeedDelay(MOVEMENT_SPEED_DEFAULT.toFloat())
    ):BaseState

    sealed class Status{
        object Started:Status()
        object Paused:Status()
        object Idle:Status()
        object ConditionsMissing:Status()
        object SearchFinish:Status()
    }

    sealed class Effect:BaseEffect{
        data class OnSearchFinish(val isSuccess:Boolean):Effect()
    }

}