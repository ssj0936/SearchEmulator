package com.timothy.searchemulator.ui.emulator.algo

import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.BlockIndex
import kotlinx.coroutines.delay
import timber.log.Timber
import java.lang.IllegalStateException
import java.util.LinkedList

class SearchDFS : SearchStrategy()  {
    private lateinit var visited: Array<BooleanArray>
    private lateinit var path : LinkedList<Block>

    override fun init(): SearchStrategy = apply{
        path = LinkedList<Block>()

        visited = Array(sizeW) { BooleanArray(sizeH) }.apply {
            this[start.first][start.second] = true

            barriers.forEach { (x,y)->
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

        suspend fun dfs(node:Block):Boolean{
            Timber.d("node:$node")
            if(!isPaused){
                delay(delayBetweenSteps)

                visited[node.first][node.second] = true
                path.push(node)
                onProcess(MovementType.MOVEMENT_STEP_IN, node)

                if(node == dest) {
                    return true
                }else{
                    for (dir in dirs) {
                        val nX = node.first + dir[0]
                        val nY = node.second + dir[1]
                        if (!isValidStep(nX, nY)) continue

                        if(dfs(Block(nX, nY)))
                            return true
                    }
                }

//                visited[node.first][node.second] = false
                path.pop()
                return false
            }else{
                return false
            }
        }

        val node:Block = path.peek()?:start
        val isFoundRoute = dfs(node)

        if(!isFoundRoute && path.isEmpty()){
            onFinish(false, null)
        }else if(isFoundRoute){
            onFinish(true, path)
        }else{
            onPause()
        }

    }

    override fun isValidStep(x: BlockIndex, y: BlockIndex): Boolean {
        return x in 0 until sizeW && y in 0 until sizeH && !visited[x][y]
    }
}