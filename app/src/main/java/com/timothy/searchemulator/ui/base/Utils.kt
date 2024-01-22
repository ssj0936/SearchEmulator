package com.timothy.searchemulator.ui.base

import androidx.compose.ui.geometry.Offset
import com.timothy.searchemulator.ui.emulator.Block
import com.timothy.searchemulator.ui.emulator.x
import com.timothy.searchemulator.ui.emulator.y

fun Offset.toBlock(blockSize:Int):Block{
    assert(this.isValid())
    return (this.x / blockSize).toInt() to (this.y/blockSize).toInt()
}

fun Block.toOffset(blockSize:Int):Offset{
    return Offset(
        blockSize * this.x.toFloat(),
        blockSize * this.y.toFloat()
    )
}