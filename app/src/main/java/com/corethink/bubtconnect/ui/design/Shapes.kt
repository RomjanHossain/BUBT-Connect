package com.corethink.bubtconnect.ui.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shape & spacing from "Student App UI.dc":
 *
 * Card 16 · Button 28 · Field 12 · Chip full
 * 4 8 12 16 20 24 dp · screen padding 20, card padding 16
 */
object BubtShapes {
    val Card: Shape = RoundedCornerShape(16.dp)
    val Button: Shape = RoundedCornerShape(28.dp)
    val Field: Shape = RoundedCornerShape(12.dp)
    val Chip: Shape = RoundedCornerShape(50)
    val Skeleton: Shape = RoundedCornerShape(8.dp)
}

/** Spacing ramp: 4 · 8 · 12 · 16 · 20 · 24 dp. */
object BubtSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Screen = 20.dp
    val Xl = 24.dp
    val CardPadding = 16.dp
}
