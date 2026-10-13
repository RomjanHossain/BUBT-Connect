package com.corethink.bubtconnect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corethink.bubtconnect.ui.theme.BubtTheme

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
    val colors = BubtTheme.colors
    val radius = BubtTheme.radius
    val elevation = BubtTheme.elevation
    val spacing = BubtTheme.spacing

    when (variant) {
        BubtCardVariant.Elevated -> {
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceElevated,
                    contentColor = colors.onSurfaceElevated
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation.sm.dp),
                shape = RoundedCornerShape(radius.lg.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg.dp),
                    content = content
                )
            }
        }
        BubtCardVariant.Outlined -> {
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface,
                    contentColor = colors.onSurface
                ),
                border = BorderStroke(1.dp, colors.border),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation.none.dp),
                shape = RoundedCornerShape(radius.lg.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg.dp),
                    content = content
                )
            }
        }
        BubtCardVariant.Filled -> {
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface,
                    contentColor = colors.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation.none.dp),
                shape = RoundedCornerShape(radius.lg.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg.dp),
                    content = content
                )
            }
        }
        BubtCardVariant.Flat -> {
            Surface(
                modifier = modifier,
                color = Color.Transparent,
                contentColor = colors.onSurface,
                shape = RoundedCornerShape(radius.lg.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun BubtCardHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = BubtTheme.colors
    val spacing = BubtTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = colors.textPrimary
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = spacing.xs.dp)
            )
        }
        if (trailing != null) {
            trailing()
        }
    }
}