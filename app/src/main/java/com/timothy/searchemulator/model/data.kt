package com.timothy.searchemulator.model

const val MOVEMENT_SPEED_BASE = 200L
const val MOVEMENT_SPEED_DEFAULT = 6
const val MOVEMENT_SPEED_MIN = 1
const val MOVEMENT_SPEED_MAX = 20

fun getMovementSpeedDelay(tick:Float):Long{
    return (MOVEMENT_SPEED_BASE / tick).toLong()
}

fun getMovementSpeedTick(delay:Long):Float{
    return (1.toFloat()/delay * MOVEMENT_SPEED_BASE)
}