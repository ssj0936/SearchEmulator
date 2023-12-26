package com.timothy.searchemulator.ui.emulator

import com.timothy.searchemulator.ui.base.BaseEffect
import com.timothy.searchemulator.ui.base.BaseEvent
import com.timothy.searchemulator.ui.base.BaseState

class Block(val x:Int, val y:Int)

class Contract{
    sealed class Event:BaseEvent{
        object OnSearchBtnClick:Event()
        object OnPauseBtnClick:Event()
        object OnResetBtnClick:Event()
        data class OnBlockPressed(val block:Block):Event()
        data class OnScreenMeasured(val widthInDp:Float, val heightInDp:Float):Event()
    }


    data class State(
        val status:Status,

        val width:Float = 0f,
        val height:Float = 0f,
        val blockSize:Float = 0f,

        val start:Block? = null,
        val dest:Block? = null,
        val barrier:List<Block> = mutableListOf(),
        val pathCnt:Int = 0,
        val path:List<Block> = mutableListOf(),
    ):BaseState

    sealed class Status{
        object Started:Status()
        object Idle:Status()
        object ConditionsMissing:Status()
    }

    sealed class Effect:BaseEffect{
        data class OnSearchFinish(val isSuccess:Boolean):Effect()
    }

}