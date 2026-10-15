package com.corethink.bubtconnect.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.corethink.bubtconnect.ui.design.BubtOutlinedButton
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.design.BubtTonalButton

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
    when (variant) {
        BubtButtonVariant.Primary, BubtButtonVariant.Success, BubtButtonVariant.Danger -> {
            BubtPrimaryButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                loading = loading,
                leadingIcon = leadingIcon
            )
        }
        BubtButtonVariant.Secondary, BubtButtonVariant.Ghost -> {
            BubtTonalButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                loading = loading,
                leadingIcon = leadingIcon
            )
        }
        BubtButtonVariant.Outline -> {
            BubtOutlinedButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                leadingIcon = leadingIcon
            )
        }
        BubtButtonVariant.Text -> {
            BubtTextButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                leadingIcon = leadingIcon
            )
        }
    }
}