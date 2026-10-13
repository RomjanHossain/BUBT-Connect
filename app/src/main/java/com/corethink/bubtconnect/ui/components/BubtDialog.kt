package com.corethink.bubtconnect.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.corethink.bubtconnect.ui.theme.BubtTheme

@Composable
fun BubtAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDanger: Boolean = false
) {
    val colors = BubtTheme.colors
    val spacing = BubtTheme.spacing
    val radius = BubtTheme.radius

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(radius.lg.dp),
            color = colors.surfaceElevated,
            contentColor = colors.onSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.xxl.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(spacing.md.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(spacing.xxl.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm.dp)
                ) {
                    BubtButton(
                        onClick = onDismiss,
                        text = dismissText,
                        variant = BubtButtonVariant.Outline,
                        modifier = Modifier.weight(1f)
                    )

                    BubtButton(
                        onClick = onConfirm,
                        text = confirmText,
                        variant = if (isDanger) BubtButtonVariant.Danger else BubtButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BubtInfoDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    confirmText: String = "OK"
) {
    val colors = BubtTheme.colors
    val spacing = BubtTheme.spacing
    val radius = BubtTheme.radius

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(radius.lg.dp),
            color = colors.surfaceElevated,
            contentColor = colors.onSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.xxl.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(spacing.md.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(spacing.xxl.dp))

                BubtButton(
                    onClick = onDismiss,
                    text = confirmText,
                    variant = BubtButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}