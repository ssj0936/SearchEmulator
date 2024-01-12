package com.timothy.searchemulator.ui.emulator

import java.util.LinkedList

interface MovementRecordManager{
    sealed class Movement{
        data class MoveStart(val from:Block, val to:Block):Movement()
        data class MoveDest(val from:Block, val to:Block):Movement()
        data class DrawBarrier(val drawPath:List<Block>):Movement()
    }
    fun onCreate()
    fun onDestroy()

    fun recordMovement(movement:Movement)
    fun getLastMovement():Movement
    fun removeLastMovement()
}

class MovementRecordManagerImpl:MovementRecordManager {
    private val barrierDrawingBuffer: LinkedList<List<Block>> = LinkedList()

}