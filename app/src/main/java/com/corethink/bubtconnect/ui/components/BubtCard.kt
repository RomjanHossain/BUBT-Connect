package com.corethink.bubtconnect.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.corethink.bubtconnect.ui.design.BubtCard
import com.corethink.bubtconnect.ui.design.BubtCardStyle

enum class BubtCardVariant {
    Elevated,
    Outlined,
    Filled,
    Flat
}

@Composable
fun BubtCard(
    modifier: Modifier = Modifier,
    variant: BubtCardVariant = BubtCardVariant.Elevated,
    content: @Composable ColumnScope.() -> Unit
) {
    val style = when (variant) {
        BubtCardVariant.Elevated -> BubtCardStyle.Elevated
        BubtCardVariant.Outlined -> BubtCardStyle.Outlined
        BubtCardVariant.Filled, BubtCardVariant.Flat -> BubtCardStyle.Filled
    }
    BubtCard(
        modifier = modifier,
        style = style,
        content = content
    )
}