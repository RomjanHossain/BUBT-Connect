package com.corethink.bubtconnect.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

// ============================================================ buttons
/** Button states from 1b: Default · Pressed · Disabled · Loading. */
enum class BubtButtonState { Default, Pressed, Disabled, Loading }

private const val PressedOverlay = 0.12f

private fun Color.pressed(): Color = lerp(this, Color.Black, PressedOverlay)

@Composable
private fun BubtButtonSurface(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    state: BubtButtonState?,
    container: Color,
    content: Color,
    disabledContainer: Color,
    disabledContent: Color,
    border: BorderStroke? = null,
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val effective = state ?: when {
        loading -> BubtButtonState.Loading
        !enabled -> BubtButtonState.Disabled
        pressed -> BubtButtonState.Pressed
        else -> BubtButtonState.Default
    }

    val baseContainer = when (effective) {
        BubtButtonState.Disabled -> disabledContainer
        else -> container
    }
    val baseContent = when (effective) {
        BubtButtonState.Disabled -> disabledContent
        else -> content
    }
    val targetContainer = if (effective == BubtButtonState.Pressed) container.pressed() else baseContainer

    val animatedContainer by animateColorAsState(targetContainer, label = "buttonContainer")
    val animatedContent by animateColorAsState(baseContent, label = "buttonContent")

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = effective != BubtButtonState.Disabled && effective != BubtButtonState.Loading,
        interactionSource = interactionSource,
        shape = BubtShapes.Button,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedContainer,
            contentColor = animatedContent,
            disabledContainerColor = animatedContainer,
            disabledContentColor = animatedContent
        )
    ) {
        when (effective) {
            BubtButtonState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = animatedContent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
            else -> {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                }
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** PRIMARY · Continue — default / pressed / disabled / loading ("Saving…"). */
@Composable
fun BubtPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    state: BubtButtonState? = null,
    leadingIcon: ImageVector? = null
) {
    BubtButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        state = state,
        container = MaterialTheme.colorScheme.primary,
        content = MaterialTheme.colorScheme.onPrimary,
        disabledContainer = MaterialTheme.colorScheme.surfaceContainerHighest,
        disabledContent = MaterialTheme.colorScheme.onSurfaceVariant,
        leadingIcon = leadingIcon
    )
}

/** TONAL — primary container fill. */
@Composable
fun BubtTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    state: BubtButtonState? = null,
    leadingIcon: ImageVector? = null
) {
    BubtButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        state = state,
        container = MaterialTheme.colorScheme.primaryContainer,
        content = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainer = MaterialTheme.colorScheme.surfaceContainerHighest,
        disabledContent = MaterialTheme.colorScheme.onSurfaceVariant,
        leadingIcon = leadingIcon
    )
}

/** OUTLINED — primary content, outline border. */
@Composable
fun BubtOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: BubtButtonState? = null,
    leadingIcon: ImageVector? = null
) {
    BubtButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = false,
        state = state,
        container = Color.Transparent,
        content = MaterialTheme.colorScheme.primary,
        disabledContainer = Color.Transparent,
        disabledContent = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        leadingIcon = leadingIcon
    )
}

/** TEXT — no container, primary content. */
@Composable
fun BubtTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: BubtButtonState? = null,
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val effective = state ?: if (!enabled) BubtButtonState.Disabled else if (pressed) BubtButtonState.Pressed else BubtButtonState.Default
    val content by animateColorAsState(
        when {
            effective == BubtButtonState.Disabled -> MaterialTheme.colorScheme.onSurfaceVariant
            effective == BubtButtonState.Pressed -> MaterialTheme.colorScheme.primary.pressed()
            else -> MaterialTheme.colorScheme.primary
        },
        label = "textButtonContent"
    )

    TextButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = BubtShapes.Button,
        colors = ButtonDefaults.textButtonColors(contentColor = content)
    ) {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(BubtSpacing.Sm))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// ============================================================ input fields
/** Field states from 1b: Default · Focused (blue ring) · Error. */
enum class BubtFieldState { Default, Focused, Error }

@Composable
fun BubtTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    stateOverride: BubtFieldState? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val hasError = error != null
    val effectiveState = stateOverride ?: when {
        hasError -> BubtFieldState.Error
        isFocused -> BubtFieldState.Focused
        else -> BubtFieldState.Default
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        enabled = enabled,
        readOnly = readOnly,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
        isError = effectiveState == BubtFieldState.Error,
        singleLine = true,
        shape = BubtShapes.Field,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = BubtError,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            errorLabelColor = BubtError,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            errorTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            errorPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        supportingText = when {
            effectiveState == BubtFieldState.Error && error != null -> {
                { Text(error, style = MaterialTheme.typography.bodySmall, color = bubtErrorText()) }
            }
            supportingText != null -> {
                { Text(supportingText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            else -> null
        }
    )
}

/** Error text tinted for 4.5:1 contrast on the current surface. */
@Composable
fun bubtErrorText(): Color =
    if (MaterialTheme.colorScheme.background == BubtBackgroundLight) BubtErrorTextLight else BubtErrorTextDark

// ============================================================ chips & tabs
/** Chip styles from 1b: Normal · Selected (check) · Supplementary. */
enum class BubtChipStyle { Normal, Selected, Supplementary }

@Composable
fun BubtChip(
    text: String,
    modifier: Modifier = Modifier,
    style: BubtChipStyle = BubtChipStyle.Normal,
    pressed: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val container = when (style) {
        BubtChipStyle.Normal ->
            if (pressed) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surfaceContainer
        BubtChipStyle.Selected -> MaterialTheme.colorScheme.primaryContainer
        BubtChipStyle.Supplementary -> MaterialTheme.colorScheme.errorContainer
    }
    val content = when (style) {
        BubtChipStyle.Normal -> MaterialTheme.colorScheme.onSurface
        BubtChipStyle.Selected -> MaterialTheme.colorScheme.onPrimaryContainer
        BubtChipStyle.Supplementary -> MaterialTheme.colorScheme.onErrorContainer
    }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "chipScale")

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        shape = BubtShapes.Chip,
        color = container,
        contentColor = content,
        border = if (style == BubtChipStyle.Normal && !pressed) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (style == BubtChipStyle.Selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(BubtSpacing.Xs))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Scrollable pill tab row (Routine · Exams · Results · …). */
@Composable
fun BubtTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Surface(
                onClick = { onSelect(index) },
                shape = BubtShapes.Chip,
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Sm)
                )
            }
        }
    }
}

// ============================================================ cards
/** Card styles from 1b: Elevated (alerts, feed) · Filled (quick actions) · Outlined (lists, settings). */
enum class BubtCardStyle { Elevated, Filled, Outlined }

@Composable
fun BubtCard(
    modifier: Modifier = Modifier,
    style: BubtCardStyle = BubtCardStyle.Elevated,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val container = when (style) {
        BubtCardStyle.Elevated -> if (dark) BubtCardElevatedDark else BubtCardElevatedLight
        BubtCardStyle.Filled -> MaterialTheme.colorScheme.primaryContainer
        BubtCardStyle.Outlined -> MaterialTheme.colorScheme.surface
    }
    val elevation = when {
        style != BubtCardStyle.Elevated -> 0.dp
        dark -> 0.dp // tonal in dark — no shadow
        else -> 3.dp
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (elevation > 0.dp) Modifier.shadow(
                    elevation = elevation,
                    shape = BubtShapes.Card,
                    ambientColor = Color(0x14000000),
                    spotColor = Color(0x24000000)
                ) else Modifier
            ),
        shape = BubtShapes.Card,
        color = container,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (style == BubtCardStyle.Outlined) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Column(modifier = Modifier.padding(BubtSpacing.CardPadding), content = content)
    }
}

// ============================================================ status
/** Status badges from 1b: Completed (green) · Low attendance (amber) · Supplementary (red). */
enum class BubtStatusType { Success, Warning, Supplementary }

@Composable
fun BubtStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    type: BubtStatusType = BubtStatusType.Success,
    icon: ImageVector? = null
) {
    val (container, content, defaultIcon) = when (type) {
        BubtStatusType.Success ->
            Triple(bubtSuccessContainer(), bubtOnSuccessContainer(), Icons.Filled.CheckCircle)
        BubtStatusType.Warning ->
            Triple(bubtWarningContainer(), bubtOnWarningContainer(), Icons.Filled.WarningAmber)
        BubtStatusType.Supplementary ->
            Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Icons.Filled.PriorityHigh)
    }

    Surface(
        modifier = modifier,
        shape = BubtShapes.Chip,
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Xs + 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon ?: defaultIcon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = content
            )
            Spacer(modifier = Modifier.width(BubtSpacing.Xs))
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun bubtSuccessContainer(): Color =
    if (MaterialTheme.colorScheme.background == BubtBackgroundLight) BubtSuccessContainerLight else BubtSuccessContainerDark

@Composable
private fun bubtOnSuccessContainer(): Color =
    if (MaterialTheme.colorScheme.background == BubtBackgroundLight) BubtOnSuccessContainerLight else BubtOnSuccessContainerDark

@Composable
private fun bubtWarningContainer(): Color =
    if (MaterialTheme.colorScheme.background == BubtBackgroundLight) BubtWarningContainerLight else BubtWarningContainerDark

@Composable
private fun bubtOnWarningContainer(): Color =
    if (MaterialTheme.colorScheme.background == BubtBackgroundLight) BubtOnWarningContainerLight else BubtOnWarningContainerDark

// ============================================================ course card
@Composable
fun CourseCard(
    courseCode: String,
    courseTitle: String,
    attendancePercentage: Int,
    modifier: Modifier = Modifier,
    style: BubtCardStyle = BubtCardStyle.Elevated
) {
    val ringColor = when {
        attendancePercentage >= 75 -> BubtSuccess
        attendancePercentage >= 60 -> BubtWarning
        else -> BubtError
    }
    BubtCard(modifier = modifier, style = style) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = courseCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(BubtSpacing.Xs))
                Text(
                    text = courseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { attendancePercentage / 100f },
                    modifier = Modifier.size(44.dp),
                    color = ringColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "$attendancePercentage",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ============================================================ loading (skeleton)
/** Shimmer sweeps left to right every 1.4s (1n). */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    val high = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val low = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)

    background(
        brush = Brush.linearGradient(
            colors = listOf(low, high, low),
            start = Offset(translateAnim, 0f),
            end = Offset(translateAnim + 500f, 0f)
        )
    )
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    BubtCard(modifier = modifier, style = BubtCardStyle.Elevated) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .height(12.dp)
                .clip(BubtShapes.Skeleton)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Sm))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .clip(BubtShapes.Skeleton)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(12.dp)
                    .clip(BubtShapes.Skeleton)
                    .shimmerEffect()
            )
        }
    }
}

