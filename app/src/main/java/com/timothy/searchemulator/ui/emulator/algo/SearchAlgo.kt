package com.timothy.searchemulator.ui.emulator.algo

import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.BlockIndex


enum class MovementType {
    MOVEMENT_STEP_IN, MOVEMENT_REVERSE
}

enum class SearchAlgo{
    SEARCH_BFS, SEARCH_DFS
}

abstract class SearchStrategy {
    val dirs = arrayOf(
        intArrayOf(0, -1)/*up*/,
        intArrayOf(1, 0)/*right*/,
        intArrayOf(0, 1)/*down*/,
        intArrayOf(-1, 0)/*left*/
    )

    protected var isPaused: Boolean = false
    protected var isRunning = false
    protected var isFinish = false
    protected var isInit = false


    protected var sizeW: Int = 0
    protected var sizeH: Int = 0
    protected var start: Block = Block(0, 0)
    protected var dest: Block = Block(0, 0)
    protected var barriers: List<Block> = emptyList()


    fun setSizeW(sizeW: Int): SearchStrategy = apply {
        this.sizeW = sizeW
    }

    fun setSizeH(sizeH: Int): SearchStrategy = apply {
        this.sizeH = sizeH
    }

    fun setStart(block: Block): SearchStrategy = apply {
        this.start = block
    }

    fun setDest(block: Block): SearchStrategy = apply {
        this.dest = block
    }

    fun setBarriers(barriers: List<Block>): SearchStrategy = apply {
        this.barriers = barriers
    }

    open fun init(): SearchStrategy = apply {
        isInit = true
    }

    abstract fun getType():SearchAlgo

    abstract suspend fun search(
        delayBetweenSteps: Long,
        onPause: () -> Unit,
        onProcess: (move: MovementType, block: Block) -> Unit,
        onFinish: (isFound: Boolean, path:List<Block>?) -> Unit
    )

    abstract fun isValidStep(x: BlockIndex, y: BlockIndex):Boolean

    open fun reset() {
        isPaused = false
        isRunning = false
        isFinish = false
    }

    open fun onPause() {
        isPaused = true
    }

    fun onResume() {
        isPaused = false
    }

    fun onStop() {
        reset()
    }
}