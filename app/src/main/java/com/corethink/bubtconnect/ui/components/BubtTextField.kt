package com.corethink.bubtconnect.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import com.corethink.bubtconnect.ui.design.BubtTextField

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
    BubtTextField(
        value = value,
        onValueChange = onValueChange,
        label = label ?: "",
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        supportingText = helper,
        enabled = enabled,
        readOnly = readOnly,
        keyboardType = keyboardOptions.keyboardType,
        visualTransformation = visualTransformation,
        trailingIcon = if (trailingIcon != null) {
            {
                IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                    Icon(imageVector = trailingIcon, contentDescription = null)
                }
            }
        } else null
    )
}