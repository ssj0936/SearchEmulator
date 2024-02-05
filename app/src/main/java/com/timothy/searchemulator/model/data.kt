package com.timothy.searchemulator.model

import kotlin.math.ln

val dirs = arrayOf(
    intArrayOf(0, -1),//up
    intArrayOf(1, 0),//right
    intArrayOf(0, 1),//bottom
    intArrayOf(-1, 0),//left
)

//speed
const val MOVEMENT_SPEED_BASE = 50L
const val MOVEMENT_SPEED_DEFAULT = 3

//size
const val BOARD_SIZE_DEFAULT = 1
const val BOARD_SIZE_BASE = 18
const val BOARD_SIZE_MULTIPLIER = 8

class RangeChooserData(
    val value:Int,
    val displayContent:String
)
val boardSizeRange = listOf<RangeChooserData>(
    RangeChooserData(1, "Normal Board"),
    RangeChooserData(2, "Big Board"),
    RangeChooserData(3, "Large Size"),
)

val speedRange = listOf<RangeChooserData>(
    RangeChooserData(1, "Slowest"),
    RangeChooserData(2, "Slow"),
    RangeChooserData(3, "Normal"),
    RangeChooserData(4, "Fast"),
    RangeChooserData(5, "Fast as fxxx"),
)

//Speed Of final path animation
const val MS_PER_PATH_BLOCK:Int = 20

private fun powOf(base:Int, pow:Int):Float{
    var r = 1f
    repeat(pow){ r*=base}
    return r
}

fun getMovementSpeedDelay(tick:Int):Long{
    return (MOVEMENT_SPEED_BASE / powOf(2, tick -1)).toLong()
}

fun getMovementSpeedTick(delay:Long):Float{
    val tmp =  1.toDouble() / (delay.toDouble() / MOVEMENT_SPEED_BASE)
    return ((ln(tmp) / ln(2.0))+1).toFloat()
}

fun getBoardSize(tick:Int):Int = BOARD_SIZE_BASE + (BOARD_SIZE_MULTIPLIER * (tick-1))
fun getBoardSizeTick(blockCount:Int):Int = (blockCount - BOARD_SIZE_BASE)/BOARD_SIZE_MULTIPLIER +1

