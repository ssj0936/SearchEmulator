package com.timothy.searchemulator.ui.emulator.algo

import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.BlockIndex
import kotlinx.coroutines.delay
import timber.log.Timber
import java.lang.IllegalStateException
import java.util.LinkedList

class SearchDFS : SearchStrategy() {
    private lateinit var visited: Array<BooleanArray>
    private lateinit var stack: LinkedList<Block>
    private lateinit var prev: Array<Array<Block?>>

    override fun reset() {
        super.reset()

        if (this::visited.isInitialized)
            for(rowIndex in visited.indices){
                for(columnIndex in visited[rowIndex].indices){
                    visited[rowIndex][columnIndex] = false
                }
            }

        if(this::stack.isInitialized)
            stack.clear()

        if (this::prev.isInitialized)
            for(rowIndex in prev.indices){
                for(columnIndex in prev[rowIndex].indices){
                    prev[rowIndex][columnIndex] = null
                }
            }
    }

    override fun init(): SearchStrategy = apply {
        stack = LinkedList<Block>().apply { push(start) }
        prev = Array(sizeW) { Array(sizeH){null} }
        visited = Array(sizeW) { BooleanArray(sizeH) }.apply {
            barriers.forEach { (x, y) ->
                this[x][y] = true
            }
        }

        super.init()
    }

    override fun getType(): SearchAlgo = SearchAlgo.SEARCH_DFS

    override suspend fun search(
        delayBetweenSteps: Long,
        onPause: () -> Unit,
        onProcess: (move: MovementType, block: Block) -> Unit,
        onFinish: (isFound: Boolean, path: List<Block>?) -> Unit
    ) {
        if (!isInit)
            throw IllegalStateException("not init yet")
        isRunning = true

        while (stack.isNotEmpty()){
            delay(delayBetweenSteps)

            if(!isPaused){
                val pop = stack.pop()
                if(visited[pop.first][pop.second]) continue
                if(pop == dest){
                    val path = mutableListOf<Block>()
                    var ptr = dest
                    while (ptr!=start){
                        path.add(ptr.copy())
                        ptr = prev[ptr.first][ptr.second]!!
                    }
                    path.apply {
                        add(start.copy())
                        reverse()
                    }

                    onFinish(true, path)
                    return
                }

                visited[pop.first][pop.second] = true
                onProcess(MovementType.MOVEMENT_STEP_IN, pop)

                for (dir in dirs) {
                    val nX = pop.first + dir[0]
                    val nY = pop.second + dir[1]
                    if (!isValidStep(nX, nY)) continue

                    stack.push(Block(nX, nY))
                    prev[nX][nY] = pop
                }
            }else{
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