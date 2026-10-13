package com.corethink.bubtconnect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.corethink.bubtconnect.ui.theme.BubtTheme

enum class BubtButtonVariant {
    Primary,
    Secondary,
    Success,
    Danger,
    Outline,
    Ghost,
    Text
}

enum class BubtButtonSize {
    Small,
    Medium,
    Large
}

@Composable
fun BubtButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    variant: BubtButtonVariant = BubtButtonVariant.Primary,
    size: BubtButtonSize = BubtButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val colors = BubtTheme.colors
    val spacing = BubtTheme.spacing
    val radius = BubtTheme.radius

    val height = when (size) {
        BubtButtonSize.Small -> 36.dp
        BubtButtonSize.Medium -> 44.dp
        BubtButtonSize.Large -> 48.dp
    }

    val contentPadding = when (size) {
        BubtButtonSize.Small -> PaddingValues(horizontal = spacing.lg.dp, vertical = spacing.xs.dp)
        BubtButtonSize.Medium -> PaddingValues(horizontal = spacing.xl.dp, vertical = spacing.sm.dp)
        BubtButtonSize.Large -> PaddingValues(horizontal = spacing.xxl.dp, vertical = spacing.md.dp)
    }

    val textSize = when (size) {
        BubtButtonSize.Small -> BubtTheme.typography.buttonSmall
        BubtButtonSize.Medium -> BubtTheme.typography.buttonMedium
        BubtButtonSize.Large -> BubtTheme.typography.buttonLarge
    }

    val shape = RoundedCornerShape(radius.md.dp)

    val buttonModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)

    when (variant) {
        BubtButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.border,
                    disabledContentColor = colors.textTertiary
                ),
                shape = shape,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textSize,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    color = colors.onPrimary
                )
            }
        }
        BubtButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.secondary,
                    contentColor = colors.onSecondary,
                    disabledContainerColor = colors.border,
                    disabledContentColor = colors.textTertiary
                ),
                shape = shape,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textSize,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    color = colors.onSecondary
                )
            }
        }
        BubtButtonVariant.Success -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.success,
                    contentColor = colors.onSuccess,
                    disabledContainerColor = colors.border,
                    disabledContentColor = colors.textTertiary
                ),
                shape = shape,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textSize,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    color = colors.onSuccess
                )
            }
        }
        BubtButtonVariant.Danger -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error,
                    contentColor = colors.onError,
                    disabledContainerColor = colors.border,
                    disabledContentColor = colors.textTertiary
                ),
                shape = shape,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textSize,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    color = colors.onError
                )
            }
        }
        BubtButtonVariant.Outline -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled && !loading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primary,
                    disabledContentColor = colors.textTertiary
                ),
                border = BorderStroke(1.dp, if (enabled) colors.border else colors.border.copy(alpha = 0.5f)),
                shape = shape,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textSize,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    color = if (enabled) colors.primary else colors.textTertiary
                )
            }
        }
        BubtButtonVariant.Ghost -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.primary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = colors.textTertiary
                ),
                shape = shape,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textSize,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    color = if (enabled) colors.primary else colors.textTertiary
                )
            }
        }
        BubtButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled && !loading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.primary,
                    disabledContentColor = colors.textTertiary
                ),
                shape = shape,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textSize,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    color = if (enabled) colors.primary else colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    textStyle: TextStyle,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    loading: Boolean,
    color: Color
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = color,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = textStyle, fontWeight = FontWeight.Medium)
    } else {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = textStyle, fontWeight = FontWeight.Medium)
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color
            )
        }
    }
}