package com.timothy.searchemulator.model

import com.timothy.searchemulator.R

const val MOVEMENT_SPEED_BASE = 200L
const val MOVEMENT_SPEED_DEFAULT = 6
const val MOVEMENT_SPEED_MIN = 1
const val MOVEMENT_SPEED_MAX = 20

const val ID_BUTTON_START = 0
const val ID_BUTTON_PAUSE = 1
const val ID_BUTTON_STOP = 2

fun getMovementSpeedDelay(tick:Float):Long{
    return (MOVEMENT_SPEED_BASE / tick).toLong()
}

fun getMovementSpeedTick(delay:Long):Float{
    return (1.toFloat()/delay * MOVEMENT_SPEED_BASE)
}

class ControlPanelButtonWrapper(
    val title: String,
    val icon: Int,
    val iconPressed: Int,
    val id: Int
)

val controlPanelButtonWrappers = listOf(
    ControlPanelButtonWrapper(
        "Start",
        R.drawable.ic_play_24,
        R.drawable.ic_play_circle_pressed_24,
        ID_BUTTON_START
    ),
    ControlPanelButtonWrapper(
        "Pause",
        R.drawable.ic_pause_24,
        R.drawable.ic_pause_circle_pressed_24,
        ID_BUTTON_PAUSE
    ),
    ControlPanelButtonWrapper(
        "Stop",
        R.drawable.ic_stop_24,
        R.drawable.ic_stop_circle_pressed_24,
        ID_BUTTON_STOP
    )
)