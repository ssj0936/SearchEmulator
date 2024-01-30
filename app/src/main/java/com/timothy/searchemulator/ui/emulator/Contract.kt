package com.timothy.searchemulator.ui.emulator

import androidx.compose.ui.geometry.Offset
import com.timothy.searchemulator.ui.emulator.algo.SearchStrategy
import com.timothy.searchemulator.ui.base.BaseEffect
import com.timothy.searchemulator.ui.base.BaseEvent
import com.timothy.searchemulator.ui.base.BaseState
import com.timothy.searchemulator.ui.base.BaseStatus
import com.timothy.searchemulator.ui.emulator.algo.SearchAlgo

typealias Block = Pair<Int,Int>
typealias BlockIndex = Int
val Block.x:Int get() = this.first
val Block.y:Int get() = this.second

class Contract{
    sealed class Event:BaseEvent{
        object OnSearchBtnClick:Event()
        object OnPauseBtnClick:Event()
        object OnResetBtnClick:Event()
        data class OnScreenMeasured(val widthInPx:Int, val heightInPx:Int):Event()

        data class OnSizeSliderChange(val value:Float):Event()
        data class OnSpeedSliderChange(val value:Float):Event()

        data class OnSearchStrategyChange(val strategy: SearchAlgo):Event()

        //drawing
        data class OnDraggingStart(val offset: Offset):Event()
        data class OnDragging(val block: Block):Event()
        object OnDraggingEnd:Event()
        data class OnPressed(val offset: Offset):Event()
        data class OnTap(val offset: Offset):Event()

        object OnBarrierClearButtonClicked:Event()
        object OnBarrierUndoButtonClicked:Event()
        object OnBarrierRedoButtonClicked:Event()
    }

    data class State(
        //status
//        val status:Status,

        //in px
        val width:Int = 0,
        val height:Int = 0,
        val blockSize:Int = 0,

        //in block
        val minSideBlockCnt:Int,
        val matrixW:Int = 0,
        val matrixH:Int = 0,

        val start:Block? = null,
        val dest:Block? = null,
        val barrier:HashSet<Block> = hashSetOf(),

        val searchStrategy: SearchStrategy,
        //blocks walked through
        val passed:List<Block> = emptyList(),
        //final path
        val path:List<Block> = emptyList(),

        val lastMovement: StatusType = StatusType.Normal,
        //animation
        val searchProcessDelay:Long,
    ):BaseState

    interface DrawingType

    sealed class Status: BaseStatus{
        object Started:Status()
        object Paused:Status()
        object Idle:Status()
//        object ConditionsMissing:Status()
        object SearchFinish:Status()

        object BarrierDrawing:Status(),DrawingType
        object StartDragging:Status(),DrawingType
        object DestDragging:Status(),DrawingType
    }

    sealed class Effect:BaseEffect{
        data class OnSearchFinish(val isSuccess:Boolean):Effect()
//        data class OnStartPosChange(val block:Block):Effect()
        data class OnUndoEvent(val movement:Movement):Effect()
        data class OnRedoEvent(val movement:Movement):Effect()
        object OnBarrierCleaned:Effect()
    }

}