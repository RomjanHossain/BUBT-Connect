package com.corethink.bubtconnect.ui.cover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.corethink.bubtconnect.data.model.CoverDetails
import com.corethink.bubtconnect.data.model.CoverTemplate
import com.corethink.bubtconnect.data.model.SimpleDate
import com.corethink.bubtconnect.data.repository.SettingsRepository
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.onboarding.OnboardingTopBar
import com.corethink.bubtconnect.ui.scanner.CoverPreview

/** Profile → "Default cover page". The pick pre-selects the scanner's 1o tile. */
@Composable
fun DefaultCoverRoute(onBack: () -> Unit) {
    val settings by SettingsRepository.Default.settings.collectAsStateWithLifecycle()
    DefaultCoverScreen(
        selected = settings.defaultCoverTemplate,
        onBack = onBack,
        onSelect = SettingsRepository.Default::setDefaultCoverTemplate
    )
}

/**
 * The five predesigned covers, each drawn by [com.corethink.bubtconnect.data.scan.CoverRenderer]
 * so the card shows the real layout — one tap makes it the scanner's starting template.
 */
@Composable
fun DefaultCoverScreen(
    selected: CoverTemplate,
    onBack: () -> Unit,
    onSelect: (CoverTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            OnboardingTopBar(
                title = "Default cover page",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
            ) {
                Text(
                    text = "The scanner opens with this cover pre-selected. You can still switch templates for each new document.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CoverTemplate.entries.filter(CoverTemplate::hasCover).forEach { template ->
                    CoverTemplateCard(
                        template = template,
                        selected = template == selected,
                        onClick = { onSelect(template) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverTemplateCard(
    template: CoverTemplate,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White, // the paper stays white in dark mode — it's the printed output
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp
            ) {
                CoverPreview(
                    template = template,
                    details = sampleDetails(template),
                    modifier = Modifier.width(92.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = template.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = CoverDescriptions[template].orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (selected) "Current default" else null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private val CoverDescriptions = mapOf(
    CoverTemplate.Assignment to "Course code and title over Submitted By and Submitted To cards.",
    CoverTemplate.LabReport to "Experiment date and number with a five-line student card.",
    CoverTemplate.ProjectReport to "BUBT letterhead, supervisor block and a team-member table.",
    CoverTemplate.IndexPage to "Underlined identity fields over a 16-row experiment table.",
    CoverTemplate.InternshipReport to "Centered supervised-by and submitted-by blocks, no frame."
)

/** Filled sample so every card previews a realistic cover instead of a blank form. */
private fun sampleDetails(template: CoverTemplate) = CoverDetails(
    courseCode = "CSE 231",
    courseTitle = "Data Structures",
    title = when (template) {
        CoverTemplate.LabReport -> "Singly Linked List"
        CoverTemplate.InternshipReport -> "RFID Attendance System"
        CoverTemplate.ProjectReport -> "RFID Attendance System"
        else -> "Linked List Implementation"
    },
    date = SimpleDate.today(),
    submittedTo = "Dr. M. Rahman, Assoc. Prof.",
    submittedBy = "Rafi Ahmed · 22235103045"
)

@Preview(showBackground = true)
@Composable
private fun DefaultCoverScreenPreview() {
    BUBTConnectMaterial3Theme {
        DefaultCoverScreen(selected = CoverTemplate.Assignment, onBack = {}, onSelect = {})
    }
}
