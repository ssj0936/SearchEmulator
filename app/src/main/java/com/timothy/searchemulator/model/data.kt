package com.timothy.searchemulator.model

import kotlin.math.ln

//speed
const val MOVEMENT_SPEED_BASE = 100L
const val MOVEMENT_SPEED_MIN = 1
const val MOVEMENT_SPEED_MAX = 5
const val MOVEMENT_SPEED_STEP = 3
const val MOVEMENT_SPEED_DEFAULT = 3

//size
const val BOARD_SIZE_DEFAULT = 1
const val BOARD_SIZE_MIN = 1
const val BOARD_SIZE_MAX = 3
const val BOARD_SIZE_BASE = 18
const val BOARD_SIZE_MULTIPLIER = 8
const val BOARD_SIZE_STEP = 1

private fun powOf(base:Int, pow:Int):Float{
    var r = 1f
    repeat(pow){ r*=base}
    return r
}

fun getMovementSpeedDelay(tick:Float):Long{
    return (MOVEMENT_SPEED_BASE / powOf(2,tick.toInt()-1)).toLong()
}

fun getMovementSpeedTick(delay:Long):Float{
    val tmp =  1.toDouble() / (delay.toDouble() / MOVEMENT_SPEED_BASE)
    return ((ln(tmp) / ln(2.0))+1).toFloat()
}

fun getBoardSize(tick:Float):Int = BOARD_SIZE_BASE + (BOARD_SIZE_MULTIPLIER * (tick-1)).toInt()
fun getBoardSizeTick(blockCount:Int):Float = (blockCount - BOARD_SIZE_BASE).toFloat()/BOARD_SIZE_MULTIPLIER +1

