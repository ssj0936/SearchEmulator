package com.timothy.searchemulator.ui.emulator

import kotlinx.coroutines.delay
import java.lang.IllegalStateException
import java.util.LinkedList

const val MOVEMENT_SPEED = 300L

enum class MovementType {
    MOVEMENT_STEP_IN, MOVEMENT_REVERSE
}

val dirs = arrayOf(
    intArrayOf(0, -1)/*up*/,
    intArrayOf(1, 0)/*right*/,
    intArrayOf(0, 1)/*down*/,
    intArrayOf(-1, 0)/*left*/
)

abstract class SearchStrategy {
    protected var isPaused: Boolean = false
    protected var isRunning = false
    protected var isFinish = false
    protected var isInit = false


    protected var sizeW: Int = 0
    protected var sizeH: Int = 0
    protected var start: Block = Block(0, 0)
    protected var dest: Block = Block(0, 0)


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

    open fun init(): SearchStrategy = apply {
        isInit = true
    }

    abstract suspend fun search(
        onPause: () -> Unit,
        onProcess: (move: MovementType, block: Block) -> Unit,
        onFinish: (isFound: Boolean) -> Unit
    )

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

class SearchBFS : SearchStrategy() {
    private lateinit var queue: LinkedList<Block>
    private lateinit var visited: Array<BooleanArray>

    override fun reset() {
        super.reset()
        queue.clear()
        visited.all { false }
    }

    override fun init(): SearchStrategy = apply {
        queue = LinkedList<Block>().apply { offer(start) }
        visited = Array(sizeW) { BooleanArray(sizeH) }.apply {
            this[start.first][start.second] = true
        }

        super.init()
    }

    override suspend fun search(
        onPause: () -> Unit,
        onProcess: (move: MovementType, block: Block) -> Unit,
        onFinish: (isFound: Boolean) -> Unit
    ) {
        if (!isInit) throw IllegalStateException("not init yet")
        isRunning = true

        while (queue.isNotEmpty()) {

            if (!isPaused) {
                val pop: Block = queue.poll()!!
                onProcess(MovementType.MOVEMENT_STEP_IN, pop)
                if (pop == dest) {
                    onFinish(true)
                    return
                }
                delay(MOVEMENT_SPEED)
                for (dir in dirs) {
                    val nX = pop.first + dir[0]
                    val nY = pop.second + dir[1]
                    if (nX !in 0 until sizeW || nY !in 0 until sizeH || visited[nX][nY]) continue

                    visited[nX][nY] = true
                    queue.offer(Block(nX, nY))
                }
            } else {
                onPause()
                return
            }

        }
        onFinish(false)
    }


}