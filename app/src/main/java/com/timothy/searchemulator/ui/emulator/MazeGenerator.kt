package com.timothy.searchemulator.ui.emulator

import com.timothy.searchemulator.model.dirs

interface MazeGenerator<T> {
    fun setWidth(width: Int): MazeGenerator<T>
    fun setHeight(height: Int): MazeGenerator<T>
    fun setIsSurroundedByWalls(isSurroundedByWalls: Boolean): MazeGenerator<T>
    fun generateWalls(): T
}

class MazeGeneratorImpl : MazeGenerator<HashSet<Block>> {
    private var width: Int = 0
    private var height: Int = 0
    private var isSurroundedByWalls = true

    override fun setWidth(width: Int): MazeGenerator<HashSet<Block>> {
        this.width = width
        return this
    }

    override fun setHeight(height: Int): MazeGenerator<HashSet<Block>> {
        this.height = height
        return this
    }

    override fun setIsSurroundedByWalls(isSurroundedByWalls: Boolean): MazeGenerator<HashSet<Block>> {
        this.isSurroundedByWalls = isSurroundedByWalls
        return this
    }

    private val UNVISITED = -1
    private val PATH = 0
    private val WALL = 1

    override fun generateWalls(): HashSet<Block> {
//        val mazeWalls =hashSetOf<Block>()
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