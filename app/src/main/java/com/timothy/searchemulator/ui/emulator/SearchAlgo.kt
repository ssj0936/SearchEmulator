package com.timothy.searchemulator.ui.emulator

import java.util.LinkedList

const val MOVEMENT_STEP_IN = 1
const val MOVEMENT_REVERSE = 0

enum class MovementType{
    MOVEMENT_STEP_IN, MOVEMENT_REVERSE
}

interface SearchStrategy{
    fun search(sizeW:Int, sizeH:Int, start:Pair<Int, Int>, dest:Pair<Int, Int>, onProcess:(move:MovementType, block:Block)->Unit, onFinish:(isFound:Boolean)->Unit)
}

val dirs = arrayOf(intArrayOf(0,-1)/*up*/,intArrayOf(1,0)/*right*/,intArrayOf(0,1)/*down*/,intArrayOf(-1,0)/*left*/)
class SearchBFS:SearchStrategy{
    override fun search(
        sizeW:Int,
        sizeH:Int,
        start: Block,
        dest: Block,
        onProcess: (move:MovementType, block:Block) -> Unit,
        onFinish: (isFound:Boolean) -> Unit
    ) {
        val queue = LinkedList<Block>().apply {offer(start)}
        val visited = Array(sizeW){BooleanArray(sizeH)}.apply { this[start.first][start.second] = true }
        while (queue.isNotEmpty()){
            val pop:Block = queue.poll()
            onProcess(MovementType.MOVEMENT_STEP_IN, pop)
            if(pop == dest) {
                onFinish(true)
                return
            }

            for(dir in dirs){
                val nX = pop.first + dir[0]
                val nY = pop.second + dir[1]
                if(nX !in 0 until sizeW || nY !in 0 until sizeH || visited[nX][nY]) continue

                visited[nX][nY] = true
                queue.offer(Block(nX, nY))
            }
        }

        onFinish(false)
    }
}