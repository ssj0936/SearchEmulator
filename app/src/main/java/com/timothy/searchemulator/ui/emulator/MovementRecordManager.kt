package com.timothy.searchemulator.ui.emulator

import java.util.LinkedList
import javax.inject.Inject

enum class OperationType {
    NORMAL,
    UNDO_START, REDO_START,
    UNDO_DEST, REDO_DEST,
    UNDO_BARRIERS, REDO_BARRIERS,
    GENERATE_MAZE
}

sealed class OperationUnit {
    data class MoveStart(var from: Block? = null, var to: Block? = null) : OperationUnit()
    data class MoveDest(var from: Block? = null, var to: Block? = null) : OperationUnit()
    data class DrawBarrier(var drawPath: List<Block> = emptyList()) : OperationUnit()
    data class GenerateMaze(
        var barriersDiff: List<Block> = emptyList(),
        var startFrom: Block? = null,
        var startTo: Block? = null,
        var destFrom: Block? = null,
        var destTo: Block? = null
    ) : OperationUnit()
}

interface MovementRecordManager {
    fun onCreate()
    fun onDestroy()

    fun recordMovement(operationUnit: OperationUnit)
    fun getLastMovement(): OperationUnit?

    fun hasUndoMovement(): Boolean
    fun hasRedoMovement(): Boolean

    fun undoLastMovement(): OperationUnit?
    fun redoLastMovement(): OperationUnit?

    fun getCurrentMovement(): MutableList<Block>
    fun startRecording(operationUnit: OperationUnit): MovementRecordManager
    fun finishRecording(): MovementRecordManager
    fun record(block: Block): MovementRecordManager
    fun record(blocks: Collection<Block>): MovementRecordManager

}

class MovementRecordManagerImpl @Inject constructor() : MovementRecordManager {
    private val barrierDrawingUndoBuffer: LinkedList<OperationUnit> = LinkedList()
    private val barrierDrawingRedoBuffer: LinkedList<OperationUnit> = LinkedList()
    private val currentRecordingMovement = mutableListOf<Block>()

    //    private lateinit var currentMovementType:MovementType
    private lateinit var mOperationUnit: OperationUnit
    private var isRecording = false

    override fun onCreate() {
        barrierDrawingUndoBuffer.clear()
        barrierDrawingRedoBuffer.clear()
    }

    override fun onDestroy() {
        barrierDrawingUndoBuffer.clear()
        barrierDrawingRedoBuffer.clear()
    }

    override fun recordMovement(operationUnit: OperationUnit) {
        barrierDrawingUndoBuffer.push(operationUnit)
    }

    override fun getLastMovement(): OperationUnit? {
        return barrierDrawingUndoBuffer.peek()
    }

    override fun hasUndoMovement(): Boolean = barrierDrawingUndoBuffer.isNotEmpty()

    override fun hasRedoMovement(): Boolean = barrierDrawingRedoBuffer.isNotEmpty()

    override fun undoLastMovement(): OperationUnit? {
        val lastMovement = barrierDrawingUndoBuffer.pop()
        barrierDrawingRedoBuffer.push(lastMovement)
        return lastMovement
    }

    override fun redoLastMovement(): OperationUnit? {
        val lastUndoMovement = barrierDrawingRedoBuffer.pop()
        barrierDrawingUndoBuffer.push(lastUndoMovement)
        return lastUndoMovement
    }

    override fun startRecording(operationUnit: OperationUnit): MovementRecordManager {
        if (!isRecording) {
            isRecording = true
//            currentMovementType = type
            mOperationUnit = operationUnit
            currentRecordingMovement.clear()
        }
        return this
    }

    override fun record(block: Block): MovementRecordManager {
        currentRecordingMovement.add(block)
        return this
    }

    override fun record(blocks: Collection<Block>): MovementRecordManager {
        currentRecordingMovement.addAll(blocks)
        return this
    }

    override fun getCurrentMovement(): MutableList<Block> {
        return currentRecordingMovement
    }

    override fun finishRecording(): MovementRecordManager {
        if (isRecording) {
            isRecording = false

            when (mOperationUnit) {
                is OperationUnit.MoveStart -> {
                    (mOperationUnit as OperationUnit.MoveStart).from =
                        currentRecordingMovement.first()
                    (mOperationUnit as OperationUnit.MoveStart).to = currentRecordingMovement.last()
                }

                is OperationUnit.MoveDest -> {
                    (mOperationUnit as OperationUnit.MoveDest).from =
                        currentRecordingMovement.first()
                    (mOperationUnit as OperationUnit.MoveDest).to = currentRecordingMovement.last()
                }

                is OperationUnit.DrawBarrier -> {
                    (mOperationUnit as OperationUnit.DrawBarrier).drawPath =
                        currentRecordingMovement.toList()
                }

                is OperationUnit.GenerateMaze -> {
                    (mOperationUnit as OperationUnit.GenerateMaze).barriersDiff =
                        currentRecordingMovement.toList()
                }

                else -> {}
            }
            barrierDrawingUndoBuffer.push(mOperationUnit)

            barrierDrawingRedoBuffer.clear()
        }
        return this
    }

}