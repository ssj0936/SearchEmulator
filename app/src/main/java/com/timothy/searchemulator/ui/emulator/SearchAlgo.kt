package com.timothy.searchemulator.ui.emulator

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.LinkedList
import kotlin.coroutines.Continuation
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine

const val MOVEMENT_STEP_IN = 1
const val MOVEMENT_REVERSE = 0
const val MOVEMENT_SPEED = 300L

enum class MovementType{
    MOVEMENT_STEP_IN, MOVEMENT_REVERSE
}

abstract class SearchStrategy{
    var isPaused:Boolean = false
    var isRunning = false

    open fun pause(){isPaused = true}
    fun resume(){isPaused = false}

    abstract suspend fun search(sizeW:Int, sizeH:Int,
                                start:Pair<Int, Int>, dest:Pair<Int, Int>,
                                state:Contract.State,
                                onPause:()->Unit,
                                onProcess:(move:MovementType, block:Block)->Unit,
                                onFinish:(isFound:Boolean)->Unit)

}

val dirs = arrayOf(intArrayOf(0,-1)/*up*/,intArrayOf(1,0)/*right*/,intArrayOf(0,1)/*down*/,intArrayOf(-1,0)/*left*/)
class SearchBFS: SearchStrategy() {
    private var continuation: Continuation<Unit>? = null

    override fun pause() {
        super.pause()
        continuation = suspendCoroutine { cont ->
            continuation = cont
        }

    }

    override suspend fun search(
        sizeW:Int,
        sizeH:Int,
        start: Block,
        dest: Block,
        state:Contract.State,
        onPause:()->Unit,
        onProcess: (move:MovementType, block:Block) -> Unit,
        onFinish: (isFound:Boolean) -> Unit
    ) {
        isRunning = true

        val queue = LinkedList<Block>().apply {offer(start)}
        val visited = Array(sizeW){BooleanArray(sizeH)}.apply { this[start.first][start.second] = true }
        while (queue.isNotEmpty()){
//            if (!coroutineContext[Job]?.isActive!!) {
//                println("Additional action in suspend function.")
//                isRunning = false
//            }

            if(!isPaused){
                val pop:Block = queue.poll()!!
                onProcess(MovementType.MOVEMENT_STEP_IN, pop)
                if(pop == dest) {
//                    isRunning = false
                    onFinish(true)
                    return
                }
                delay(MOVEMENT_SPEED)
                for(dir in dirs){
                    val nX = pop.first + dir[0]
                    val nY = pop.second + dir[1]
                    if(nX !in 0 until sizeW || nY !in 0 until sizeH || visited[nX][nY]) continue

                    visited[nX][nY] = true
                    queue.offer(Block(nX, nY))
                }
            }else{
                suspendCoroutine {
                    onPause()
                }
            }

        }
//        isRunning = false
        onFinish(false)
    }
}