package com.timothy.searchemulator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

data class SearchingColor(
    val colorBlockBackground:Color = Color.Black,
    val colorBlockStart:Color = Color.Red,
    val colorBlockDest:Color = Color.Green,
    val colorBlockBarrier:Color = Color.DarkGray,
    val colorBlockPassed:Color = Color.Yellow,
    val colorBlockPath:Color = Color.LightGray,
)

val LocalSearchingColor = compositionLocalOf { SearchingColor() }
val MaterialTheme.color:SearchingColor
    @Composable
    @ReadOnlyComposable
    get() = LocalSearchingColor.current