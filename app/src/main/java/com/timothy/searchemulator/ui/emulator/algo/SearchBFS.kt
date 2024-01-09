package com.timothy.searchemulator.ui.emulator.algo

import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.BlockIndex
import kotlinx.coroutines.delay
import java.lang.IllegalStateException
import java.util.LinkedList

class SearchBFS : SearchStrategy() {
    private lateinit var queue: LinkedList<MutableList<Block>>
    private lateinit var visited: Array<BooleanArray>

    override fun reset() {
        super.reset()

        if(this::queue.isInitialized)
            queue.clear()

        if(this::visited.isInitialized)
            visited.all { false }
    }

    override fun init(): SearchStrategy = apply {
        queue = LinkedList<MutableList<Block>>().apply { offer(mutableListOf(start)) }
        visited = Array(sizeW) { BooleanArray(sizeH) }.apply {
            this[start.first][start.second] = true

            barriers.forEach { (x,y)->
                this[x][y] = true
            }
        }

        super.init()
    }

    override fun getType(): SearchAlgo = SearchAlgo.SEARCH_BFS

    override suspend fun search(
        delayBetweenSteps : Long,
        onPause: () -> Unit,
        onProcess: (move: MovementType, block: Block) -> Unit,
        onFinish: (isFound: Boolean, path: List<Block>?) -> Unit
    ) {
        if (!isInit)
            throw IllegalStateException("not init yet")

        isRunning = true

        while (queue.isNotEmpty()) {
            delay(delayBetweenSteps)

            if (!isPaused) {
                val path = queue.poll()!!
                val node = path.last()
                onProcess(MovementType.MOVEMENT_STEP_IN, node)
                if (node == dest) {
                    onFinish(true, path)
                    return
                }
                for (dir in dirs) {
                    val nX = node.first + dir[0]
                    val nY = node.second + dir[1]
                    if (!isValidStep(nX, nY)) continue

                    visited[nX][nY] = true
                    queue.offer(path.toMutableList().apply{add(Block(nX, nY))})
                }
            } else {
                onPause()
                return
            }

        }
        onFinish(false, null)
    }

    override fun isValidStep(x: BlockIndex, y: BlockIndex): Boolean {
        return x in 0 until sizeW && y in 0 until sizeH && !visited[x][y]
    }
}