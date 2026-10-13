package com.corethink.bubtconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corethink.bubtconnect.ui.theme.BubtTheme

@Composable
fun BubtTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    error: String? = null,
    helper: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onTrailingIconClick: (() -> Unit)? = null
) {
    val colors = BubtTheme.colors
    val spacing = BubtTheme.spacing
    val radius = BubtTheme.radius
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        !enabled -> colors.border.copy(alpha = 0.5f)
        error != null -> colors.error
        isFocused -> colors.primary
        else -> colors.border
    }

    val backgroundColor = when {
        !enabled -> colors.surface.copy(alpha = 0.5f)
        else -> colors.surfaceElevated
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = spacing.sm.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(radius.md.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(radius.md.dp))
                .padding(horizontal = spacing.lg.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isFocused) colors.primary else colors.textTertiary,
                    modifier = Modifier.padding(end = spacing.sm.dp)
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                maxLines = maxLines,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                interactionSource = interactionSource,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = if (enabled) colors.textPrimary else colors.textTertiary,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { innerTextField ->
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            color = colors.textTertiary,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.padding(start = spacing.sm.dp)
                )
            }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(spacing.xs.dp))
            Text(
                text = error,
                fontSize = 12.sp,
                color = colors.error,
                modifier = Modifier.padding(start = spacing.sm.dp)
            )
        } else if (helper != null) {
            Spacer(modifier = Modifier.height(spacing.xs.dp))
            Text(
                text = helper,
                fontSize = 12.sp,
                color = colors.textTertiary,
                modifier = Modifier.padding(start = spacing.sm.dp)
            )
        }
    }
}