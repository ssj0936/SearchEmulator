package com.timothy.searchemulator.ui.emulator

import java.util.LinkedList
import javax.inject.Inject

enum class MovementType{
    MOVE_START, MOVE_DEST, DRAWING_BARRIER
}

sealed class Movement{
    data class MoveStart(val from:Block? = null, val to:Block):Movement()
    data class MoveDest(val from:Block, val to:Block):Movement()
    data class DrawBarrier(val drawPath:List<Block>):Movement()
}

interface MovementRecordManager{
    fun onCreate()
    fun onDestroy()

    fun recordMovement(movement:Movement)
    fun getLastMovement():Movement?

    fun hasUndoMovement():Boolean
    fun hasRedoMovement():Boolean

    fun undoLastMovement():Movement?
    fun redoLastMovement():Movement?
    
    fun getCurrentMovement():MutableList<Block>
    fun startRecording(type: MovementType):MovementRecordManager
    fun record(block:Block):MovementRecordManager
    fun record(blocks:List<Block>):MovementRecordManager
    fun stopRecording():MovementRecordManager
}

class MovementRecordManagerImpl @Inject constructor():MovementRecordManager {
    private val barrierDrawingUndoBuffer: LinkedList<Movement> = LinkedList()
    private val barrierDrawingRedoBuffer: LinkedList<Movement> = LinkedList()
    private val currentRecordingMovement = mutableListOf<Block>()
    private lateinit var currentMovementType:MovementType
    private var isRecording = false

    override fun onCreate() {
        barrierDrawingUndoBuffer.clear()
        barrierDrawingRedoBuffer.clear()
    }
    override fun onDestroy() {
        barrierDrawingUndoBuffer.clear()
        barrierDrawingRedoBuffer.clear()
    }

    override fun recordMovement(movement: Movement) {
        barrierDrawingUndoBuffer.push(movement)
    }

    override fun getLastMovement(): Movement? {
        return barrierDrawingUndoBuffer.peek()
    }

    override fun hasUndoMovement(): Boolean = barrierDrawingUndoBuffer.isNotEmpty()

    override fun hasRedoMovement(): Boolean = barrierDrawingRedoBuffer.isNotEmpty()

    override fun undoLastMovement(): Movement? {
        val lastMovement = barrierDrawingUndoBuffer.pop()
        barrierDrawingRedoBuffer.push(lastMovement)
        return lastMovement
    }

    override fun redoLastMovement(): Movement? {
        val lastUndoMovement = barrierDrawingRedoBuffer.pop()
        barrierDrawingUndoBuffer.push(lastUndoMovement)
        return lastUndoMovement
    }

    override fun startRecording(type: MovementType):MovementRecordManager {
        if(!isRecording) {
            isRecording = true
            currentMovementType = type
            currentRecordingMovement.clear()
        }
        return this
    }

    override fun record(block: Block):MovementRecordManager {
        currentRecordingMovement.add(block)
        return this
    }

    override fun record(blocks: List<Block>): MovementRecordManager {
        currentRecordingMovement.addAll(blocks)
        return this
    }

    override fun getCurrentMovement(): MutableList<Block> {
        return currentRecordingMovement
    }

    override fun stopRecording():MovementRecordManager {
        if(isRecording) {

            isRecording = false
            barrierDrawingUndoBuffer.push(
                when (currentMovementType) {
                    MovementType.MOVE_START -> {
                        Movement.MoveStart(
                            currentRecordingMovement.first(),
                            currentRecordingMovement.last()
                        )
                    }

                    MovementType.MOVE_DEST -> {
                        Movement.MoveDest(
                            currentRecordingMovement.first(),
                            currentRecordingMovement.last()
                        )
                    }

                    MovementType.DRAWING_BARRIER -> {
                        Movement.DrawBarrier(currentRecordingMovement.toMutableList())
                    }
                }
            )

            barrierDrawingRedoBuffer.clear()
        }
        return this
    }

}