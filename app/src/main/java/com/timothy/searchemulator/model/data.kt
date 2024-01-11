package com.timothy.searchemulator.model

//speed
const val MOVEMENT_SPEED_BASE = 200L
const val MOVEMENT_SPEED_DEFAULT = 18
const val MOVEMENT_SPEED_MIN = 1
const val MOVEMENT_SPEED_MAX = 20

//size
const val BOARD_SIZE_DEFAULT = 1
const val BOARD_SIZE_MIN = 1
const val BOARD_SIZE_MAX = 4
const val BOARD_SIZE_BASE = 20

fun getMovementSpeedDelay(tick:Float):Long{
    return (MOVEMENT_SPEED_BASE / tick).toLong()
}

fun getMovementSpeedTick(delay:Long):Float{
    return (1.toFloat()/delay * MOVEMENT_SPEED_BASE)
}

fun getBoardSize(tick:Float):Int = BOARD_SIZE_BASE * tick.toInt()
fun getBoardSizeTick(blockCount:Int):Float = blockCount.toFloat()/BOARD_SIZE_BASE