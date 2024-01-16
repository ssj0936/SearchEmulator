package com.timothy.searchemulator.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val ThemeColor01 = Color(0xFF344E41)
val ThemeColor02 = Color(0xFF3A5A40)
val ThemeColor03 = Color(0xFF89B388)

val ThemeColor04 = Color(0xFFA3B18A)
val ThemeColor05 = Color(0xFFDAD7CD)
val ThemeColor06 = Color(0xFFFFEB3B)
val ThemeColor07 = Color(0xFFFFDA6C)

data class SearchingColor(
    val colorBlockBackground:Color = ThemeColor01,
    val colorBlockStart:Color = ThemeColor03,
    val colorBlockDest:Color = ThemeColor06,
    val colorBlockBarrier:Color = ThemeColor02,
    val colorBlockPassed:Color = ThemeColor05,
    val colorBlockPath:Color = ThemeColor04,
    val colorBlockTail:Color = ThemeColor07,

    val buttonOutlineColors: Color = ThemeColor02,
    val buttonOutlineContentColors: Color = ThemeColor02,
    val buttonPressedColors: Color = ThemeColor02,
    val buttonPressedContentColors: Color = ThemeColor05
)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

)
    @Stable
data class SearchingColorLight(
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