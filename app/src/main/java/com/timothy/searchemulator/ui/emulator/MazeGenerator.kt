package com.timothy.searchemulator.ui.emulator

import com.timothy.searchemulator.model.dirs

abstract class MazeGenerator<T> {
    protected var width: Int = 0
    protected var height: Int = 0
    protected var isSurroundedByWalls = true

    fun setWidth(width: Int): MazeGenerator<T>{
        this.width = width
        return this
    }

    fun setHeight(height: Int): MazeGenerator<T>{
        this.height = height
        return this
    }

    fun setIsSurroundedByBarriers(isSurroundedByWalls: Boolean): MazeGenerator<T>{
        this.isSurroundedByWalls = isSurroundedByWalls
        return this
    }

    abstract fun generateBarriers(): T
}

class MazeGeneratorImpl : MazeGenerator<HashSet<Block>>() {

    private val UNVISITED = -1
    private val PATH = 0
    private val WALL = 1

    override fun generateBarriers(): HashSet<Block> {
        val mazeWalls = Array(width) { IntArray(height) { UNVISITED } }
        val blockCnt = width * height

        if (blockCnt == 0) return hashSetOf()

        if (isSurroundedByWalls) {
            for (i in 0 until height) {
                mazeWalls[0][i] = WALL//left
                mazeWalls[width - 1][i] = WALL//right
            }

            for (i in 0 until width) {
                mazeWalls[i][0] = WALL//top
                mazeWalls[i][height - 1] = WALL//bottom
            }

            if (blockCnt - (width * 2 + height * 2 - 4) == 0)
                return mazeWalls.toWallSet()
        }

        val startX = if (isSurroundedByWalls) 1 else 0
        val startY = if (isSurroundedByWalls) 1 else 0

        fun recursiveFindPath(x: Int, y: Int) {
            if(x !in 0 until width || y !in 0 until height) return
            if (mazeWalls[x][y] != UNVISITED) return

            val neighborPathCnt = run {
                var cnt = 0
                for (dir in dirs) {
                    if (x + dir[0] in 0 until width
                        && y + dir[1] in 0 until height
                        && mazeWalls[x + dir[0]][y + dir[1]] == PATH
                    ) {
                        ++cnt
                    }
                }
                cnt
            }

            if (neighborPathCnt > 1 && isWallRandom()) {
                mazeWalls[x][y] = WALL
            } else {
                mazeWalls[x][y] = PATH
                for(dir in dirs.clone().apply {shuffle()}){
                    recursiveFindPath(x+dir[0], y+dir[1])
                }
            }
        }
        recursiveFindPath(startX, startY)
        return mazeWalls.toWallSet()
    }

    //reduce some walls for preventing one-path maze
    private fun isWallRandom(percent:Int = 90) = (0 until 100).random()<percent

    private fun Array<IntArray>.toWallSet(): HashSet<Block> {
        val result = hashSetOf<Block>()
        this.forEachIndexed { x, rows ->
            rows.forEachIndexed { y, value ->
                if (value == WALL)
                    result.add(Block(x, y))
            }
        }
        return result
    }
}