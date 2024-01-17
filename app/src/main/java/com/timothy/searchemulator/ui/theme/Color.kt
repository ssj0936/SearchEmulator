package com.timothy.searchemulator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val ThemeColor00 = Color(0xFF262927)
val ThemeColor01 = Color(0xFF344E41)
val ThemeColor02 = Color(0xFF3A5A40)
val ThemeColor025 = Color(0xFF567A5C)
val ThemeColor03 = Color(0xFF89B388)

val ThemeColor04 = Color(0xFFA3B18A)
val ThemeColor045 = Color(0xFFBDC9A9)
val ThemeColor05 = Color(0xFFDAD7CD)
val ThemeColor055 = Color(0xFFE7E5DC)
val ThemeColor06 = Color(0xFFFFEB3B)
val ThemeColor07 = Color(0xFFFFDA6C)
val ThemeColor08 = Color(0xFFFAF6EB)

val ThemeColorPrimary = Color(0xFF3A5A40)
val ThemeColorSecondary = Color(0xFF665542)
val ThemeColorTertiary = Color(0xFFFFB952)

val DarkColorScheme = darkColorScheme(
    primary = ThemeColor03,
    secondary = ThemeColorSecondary,
    tertiary = ThemeColorTertiary,
    background = ThemeColor00

)

val LightColorScheme = lightColorScheme(
    primary = ThemeColorPrimary,
    secondary = ThemeColorSecondary,
    tertiary = ThemeColorTertiary,
)

data class SearchingColor(
    val colorBlockBackground: Color = ThemeColor01,
    val colorBlockStart: Color = ThemeColor03,
    val colorBlockDest: Color = ThemeColor06,
    val colorBlockBarrier: Color = ThemeColor02,
    val colorBlockPassed: Color = ThemeColor05,
    val colorBlockPath: Color = ThemeColor04,
    val colorBlockTail: Color = ThemeColor07,

    val buttonColors: Color = ThemeColor02,
    val buttonContentColors: Color = ThemeColor05,
    val buttonOutlineColors: Color = ThemeColor02,
    val buttonOutlineContentColors: Color = ThemeColor02,
    val buttonOutlinePressedColors: Color = ThemeColor02,
    val buttonOutlineContentPressedColors: Color = ThemeColor05,

    val sliderThumbColors: Color = ThemeColor02,
    val sliderTrackColors: Color = ThemeColor03,
    val sliderInactiveTrackColors: Color = ThemeColor05
)

fun searchingColorLight(
    colorBlockBackground: Color = ThemeColor01,
    colorBlockStart: Color = ThemeColor03,
    colorBlockDest: Color = ThemeColor06,
    colorBlockBarrier: Color = ThemeColor02,
    colorBlockPassed: Color = ThemeColor05,
    colorBlockPath: Color = ThemeColor04,
    colorBlockTail: Color = ThemeColor07,
    buttonColors: Color = ThemeColor02,
    buttonContentColors: Color = ThemeColor05,
    buttonOutlineColors: Color = ThemeColor02,
    buttonOutlineContentColors: Color = ThemeColor02,
    buttonOutlinePressedColors: Color = ThemeColor02,
    buttonOutlineContentPressedColors: Color = ThemeColor05,
    themeColorPrimary: Color = ThemeColor02,
    sliderThumbColors: Color = themeColorPrimary,
    sliderTrackColors: Color = ThemeColor03,
    sliderInactiveTrackColors: Color = ThemeColor055
): SearchingColor = SearchingColor(
    colorBlockBackground = colorBlockBackground,
    colorBlockStart = colorBlockStart,
    colorBlockDest = colorBlockDest,
    colorBlockBarrier = colorBlockBarrier,
    colorBlockPassed = colorBlockPassed,
    colorBlockPath = colorBlockPath,
    colorBlockTail = colorBlockTail,
    buttonColors = buttonColors,
    buttonContentColors = buttonContentColors,
    buttonOutlineColors = buttonOutlineColors,
    buttonOutlineContentColors = buttonOutlineContentColors,
    buttonOutlinePressedColors = buttonOutlinePressedColors,
    buttonOutlineContentPressedColors = buttonOutlineContentPressedColors,
    sliderThumbColors = sliderThumbColors,
    sliderTrackColors = sliderTrackColors,
    sliderInactiveTrackColors = sliderInactiveTrackColors
)

fun searchingColorDark(
    colorBlockBackground: Color = ThemeColor08,
    colorBlockStart: Color = ThemeColor03,
    colorBlockDest: Color = ThemeColor06,
    colorBlockBarrier: Color = ThemeColor08,
    colorBlockPassed: Color = ThemeColor045,
    colorBlockPath: Color = ThemeColor025,
    colorBlockTail: Color = ThemeColor07,
    buttonColors: Color = ThemeColor04,
    buttonContentColors: Color = ThemeColor05,
    buttonOutlineColors: Color = ThemeColor04,
    buttonOutlineContentColors: Color = ThemeColor04,
    buttonOutlinePressedColors: Color = ThemeColor04,
    buttonOutlineContentPressedColors: Color = ThemeColor05,
    themeColorPrimary: Color = ThemeColor04,
    sliderThumbColors: Color = themeColorPrimary,
    sliderTrackColors: Color = ThemeColor03,
    sliderInactiveTrackColors: Color = ThemeColor05
): SearchingColor = SearchingColor(
    colorBlockBackground = colorBlockBackground,
    colorBlockStart = colorBlockStart,
    colorBlockDest = colorBlockDest,
    colorBlockBarrier = colorBlockBarrier,
    colorBlockPassed = colorBlockPassed,
    colorBlockPath = colorBlockPath,
    colorBlockTail = colorBlockTail,
    buttonColors = buttonColors,
    buttonContentColors = buttonContentColors,
    buttonOutlineColors = buttonOutlineColors,
    buttonOutlineContentColors = buttonOutlineContentColors,
    buttonOutlinePressedColors = buttonOutlinePressedColors,
    buttonOutlineContentPressedColors = buttonOutlineContentPressedColors,
    sliderThumbColors = sliderThumbColors,
    sliderTrackColors = sliderTrackColors,
    sliderInactiveTrackColors = sliderInactiveTrackColors
)

internal val LocalSearchingColor = compositionLocalOf { SearchingColor() }
val MaterialTheme.color: SearchingColor
    @Composable
    @ReadOnlyComposable
    get() = LocalSearchingColor.current