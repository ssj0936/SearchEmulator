package com.timothy.searchemulator.ui.base

import androidx.compose.ui.geometry.Offset
import com.timothy.searchemulator.ui.emulator.Block

fun Offset.toBlock(blockSize:Int):Block{
    assert(this.isValid())
    return (this.x / blockSize).toInt() to (this.y/blockSize).toInt()
}