package com.corethink.bubtconnect.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * "Student App UI.dc" — 1a Foundations + 1b Components & states.
 */
@Composable
fun DesignSystemShowcase() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Colors", "Typography", "Components", "Loading", "Themes")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "BUBT Companion — Design System",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(BubtSpacing.Screen)
            )

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ColorPaletteSection()
                1 -> TypographySection()
                2 -> ComponentsSection()
                3 -> LoadingSection()
                4 -> ThemesSection()
            }
        }
    }
}

// ============================================================== 1a · colors
@Composable
private fun ColorPaletteSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(BubtSpacing.Screen),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Lg)
    ) {
        SectionHeader("Color tokens · Light")

        TokenRow("Primary", "#2563EB", BubtPrimaryLight)
        TokenRow("Primary container", "#DBEAFE", BubtPrimaryContainerLight)
        TokenRow("Surface", "#FFFFFF", BubtSurfaceLight)
        TokenRow("Surface container", "#F3F5F9", BubtSurfaceContainerLight)
        TokenRow("Card (elevated)", "#FFFFFF + shadow", BubtCardElevatedLight)
        TokenRow("On surface", "#0F172A", BubtOnSurfaceLight)
        TokenRow("On surface variant", "#64748B", BubtOnSurfaceVariantLight)
        TokenRow("Outline", "#E2E8F0", BubtOutlineLight)

        SectionHeader("Color tokens · Dark")

        TokenRow("Primary", "#60A5FA", BubtPrimaryDark)
        TokenRow("Primary container", "#1B2B52", BubtPrimaryContainerDark)
        TokenRow("Surface", "#121212", BubtSurfaceDark)
        TokenRow("Surface container", "#1C1C20", BubtSurfaceContainerDark)
        TokenRow("Card (elevated)", "#1E1E23 tonal", BubtCardElevatedDark)
        TokenRow("On surface", "#EDEFF3", BubtOnSurfaceDark)
        TokenRow("On surface variant", "#9AA4B2", BubtOnSurfaceVariantDark)
        TokenRow("Outline", "#33343A", BubtOutlineDark)

        SectionHeader("Status · tinted containers (18% in dark)")

        TokenRow("Success", "#10B981", BubtSuccess)
        TokenRow("Success container", "#D1FAE5 / #123026", BubtSuccessContainerLight)
        TokenRow("Warning", "#F59E0B", BubtWarning)
        TokenRow("Warning container", "#FEF3C7 / #3B2B11", BubtWarningContainerLight)
        TokenRow("Error / Supplementary", "#EF4444", BubtError)
        TokenRow("Error container", "#FEE2E2 / #3A1B1B", BubtErrorContainerLight)

        Text(
            "Status colors render as tinted containers (18% in dark) with darker/lighter text for 4.5:1 contrast.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionHeader("Shape & spacing")

        Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Md)) {
            ShapeChip("Card 16", BubtShapes.Card)
            ShapeChip("Button 28", BubtShapes.Button)
            ShapeChip("Field 12", BubtShapes.Field)
            ShapeChip("Chip full", BubtShapes.Chip)
        }
        Text(
            "4 · 8 · 12 · 16 · 20 · 24 dp — screen padding 20, card padding 16",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun TokenRow(name: String, hex: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(BubtShapes.Field)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, BubtShapes.Field)
        )
        Spacer(modifier = Modifier.width(BubtSpacing.Md))
        Column {
            Text(name, style = MaterialTheme.typography.titleSmall)
            Text(
                hex,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ShapeChip(label: String, shape: androidx.compose.ui.graphics.Shape) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ========================================================== 1a · typography
@Composable
private fun TypographySection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(BubtSpacing.Screen),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        SectionHeader("Typography · Poppins / Hind Siliguri")

        TypeSample("H1 · 24/700", "Good morning", MaterialTheme.typography.headlineSmall)
        TypeSample("H2 · 20/600", "Data Structures", MaterialTheme.typography.titleLarge)
        TypeSample("Title · 16/600", "Present Courses", MaterialTheme.typography.titleMedium)
        TypeSample("Body · 16/400", "Room 4-302, Building 4", MaterialTheme.typography.bodyLarge)
        TypeSample("Body S · 13/400", "1.2k views · 2 days ago", MaterialTheme.typography.bodySmall)
        TypeSample("Label · 11/600", "NEXT CLASS", MaterialTheme.typography.labelSmall)
        TypeSample("Bengali · 16", "শুভ সকাল, রাফি", BubtTextStyles.Bengali)
    }
}

@Composable
private fun TypeSample(spec: String, sample: String, style: androidx.compose.ui.text.TextStyle) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            spec,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Xs))
        Text(sample, style = style, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ========================================================= 1b · components
@Composable
private fun ComponentsSection() {
    var roll by remember { mutableStateOf("") }
    val rollError = if (roll.isNotEmpty() && roll.length != 11) "Must be 11 digits" else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(BubtSpacing.Screen),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Xl)
    ) {
        // ------------------------------------------------ buttons
        Column(verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
            SectionHeader("Buttons · Primary")
            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                BubtPrimaryButton(text = "Continue", onClick = {}, state = BubtButtonState.Default)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                BubtPrimaryButton(text = "Continue", onClick = {}, state = BubtButtonState.Pressed)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                BubtPrimaryButton(text = "Continue", onClick = {}, state = BubtButtonState.Disabled)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                BubtPrimaryButton(text = "Saving", onClick = {}, state = BubtButtonState.Loading)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
            BubtTonalButton(text = "Tonal", onClick = {})
            BubtOutlinedButton(text = "Outlined", onClick = {})
            BubtTextButton(text = "Text", onClick = {})
        }

        // ------------------------------------------------ input fields
        Column(verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
            SectionHeader("Input fields")

            Text("Default", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BubtTextField(
                value = roll,
                onValueChange = { roll = it },
                label = "Roll number",
                placeholder = "e.g. 22235103045",
                error = rollError,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )

            Text("Focused", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BubtTextField(
                value = "2223510",
                onValueChange = {},
                label = "Roll number",
                stateOverride = BubtFieldState.Focused,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )

            Text("Error", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BubtTextField(
                value = "2223",
                onValueChange = {},
                label = "Roll number",
                error = "Must be 11 digits",
                stateOverride = BubtFieldState.Error,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        }

        // ------------------------------------------------ chips & tabs
        Column(verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
            SectionHeader("Chips · Toggles · Tabs")

            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                BubtChip(text = "CSE 231", style = BubtChipStyle.Selected)
                BubtChip(text = "CSE 233", style = BubtChipStyle.Normal)
                BubtChip(text = "CSE 233", style = BubtChipStyle.Normal, pressed = true)
                BubtChip(text = "Supp.", style = BubtChipStyle.Supplementary)
            }

            var tab by remember { mutableIntStateOf(0) }
            BubtTabRow(
                tabs = listOf("Routine", "Exams", "Results", "Attendance", "Calendar"),
                selectedIndex = tab,
                onSelect = { tab = it }
            )
        }

        // ------------------------------------------------ cards
        Column(verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
            SectionHeader("Cards")

            Text("Elevated · alerts, feed items", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BubtCard(style = BubtCardStyle.Elevated) {
                Text("NEXT CLASS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(BubtSpacing.Xs))
                Text("Data Structures", style = MaterialTheme.typography.titleLarge)
                Text("CSE 231 · Section 2 · Dr. M. Rahman", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text("Filled · quick actions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BubtCard(style = BubtCardStyle.Filled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DocumentScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(BubtSpacing.Md))
                    Text("Scan Document", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Text("Outlined · lists, settings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BubtCard(style = BubtCardStyle.Outlined) {
                Text("Class reminder", style = MaterialTheme.typography.titleMedium)
                Text("10 min before", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ------------------------------------------------ status
        Column(verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
            SectionHeader("Status")
            Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                BubtStatusBadge(text = "Completed", type = BubtStatusType.Success)
                BubtStatusBadge(text = "Low attendance", type = BubtStatusType.Warning)
                BubtStatusBadge(text = "Supplementary CSE", type = BubtStatusType.Supplementary)
            }
        }

        // ------------------------------------------------ course card
        Column(verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
            SectionHeader("Course card · attendance")
            CourseCard(courseCode = "CSE 231", courseTitle = "Data Structures", attendancePercentage = 92)
            CourseCard(courseCode = "MAT 231", courseTitle = "Calculus II", attendancePercentage = 85)
            CourseCard(courseCode = "EEE 203", courseTitle = "Electronics", attendancePercentage = 60)
        }
    }
}

// ============================================================== loading
@Composable
private fun LoadingSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(BubtSpacing.Screen),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        SectionHeader("Loading · shimmer skeleton")
        Text(
            "Shimmer sweeps left to right every 1.4s. Skeleton matches the loaded layout exactly so nothing jumps when data arrives.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SkeletonCard()
        SkeletonCard()
        SkeletonCard()
    }
}

// ================================================================ themes
@Composable
private fun ThemesSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(BubtSpacing.Screen),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        SectionHeader("Light mode · crisp")

        BUBTConnectMaterial3Theme(darkTheme = false) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shape = BubtShapes.Card
            ) {
                Column(modifier = Modifier.padding(BubtSpacing.CardPadding)) {
                    Text("Good morning, Rafi", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(BubtSpacing.Xs))
                    Text("শুভ সকাল · Sunday, 11 Oct", style = BubtTextStyles.Bengali, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(BubtSpacing.Md))
                    Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                        BubtPrimaryButton(text = "Continue", onClick = {})
                        BubtTonalButton(text = "Tonal", onClick = {})
                    }
                    Spacer(modifier = Modifier.height(BubtSpacing.Md))
                    BubtStatusBadge(text = "Completed", type = BubtStatusType.Success)
                }
            }
        }

        SectionHeader("Dark mode · eye-friendly (#121212, not pure black)")

        BUBTConnectMaterial3Theme(darkTheme = true) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shape = BubtShapes.Card
            ) {
                Column(modifier = Modifier.padding(BubtSpacing.CardPadding)) {
                    Text("Good morning, Rafi", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(BubtSpacing.Xs))
                    Text("শুভ সকাল · Sunday, 11 Oct", style = BubtTextStyles.Bengali, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(BubtSpacing.Md))
                    Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)) {
                        BubtPrimaryButton(text = "Continue", onClick = {})
                        BubtTonalButton(text = "Tonal", onClick = {})
                    }
                    Spacer(modifier = Modifier.height(BubtSpacing.Md))
                    BubtStatusBadge(text = "Completed", type = BubtStatusType.Success)
                }
            }
        }
    }
}
