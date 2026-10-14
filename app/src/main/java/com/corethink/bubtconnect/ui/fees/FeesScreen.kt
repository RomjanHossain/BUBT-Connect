package com.corethink.bubtconnect.ui.fees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.FeeStatement
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.FilterPill
import com.corethink.bubtconnect.ui.components.LockedFeature
import com.corethink.bubtconnect.ui.components.formatTaka
import com.corethink.bubtconnect.ui.components.rememberSyncedLabel
import com.corethink.bubtconnect.ui.components.showNotAvailableYet
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.onboarding.OnboardingTopBar

@Composable
fun FeesRoute(
    onBack: () -> Unit,
    onConnectAnnex: () -> Unit,
    viewModel: FeesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    FeesScreen(
        state = state,
        onBack = onBack,
        onConnectAnnex = onConnectAnnex,
        onSelectSemester = viewModel::selectSemester,
        onInfoVisibleChange = viewModel::setInfoVisible,
        onDownloadStatement = { showNotAvailableYet(context, "Fee statement PDF") },
        onPaymentInstructions = { showNotAvailableYet(context, "Payment instructions") }
    )
}

/** 1t · Fees & waivers. ⓘ opens a sheet explaining each line. Requires Annex. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    state: FeesUiState,
    onBack: () -> Unit,
    onConnectAnnex: () -> Unit,
    onSelectSemester: (Int) -> Unit,
    onInfoVisibleChange: (Boolean) -> Unit,
    onDownloadStatement: () -> Unit,
    onPaymentInstructions: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            OnboardingTopBar(
                title = "Fees & Waivers",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = BubtSpacing.Screen),
                trailing = {
                    if (state.isConnected) {
                        IconButton(onClick = { onInfoVisibleChange(true) }) {
                            Icon(Icons.Outlined.Info, contentDescription = "What each line means")
                        }
                    }
                }
            )
            val statement = state.selected
            if (!state.isConnected || statement == null) {
                LockedFeature(
                    title = "Fees & Waivers",
                    message = "Connect your Annex account to see your fee demand, waivers and payments.",
                    onConnect = onConnectAnnex
                )
                return@Column
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Sm),
                    horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)
                ) {
                    items(state.statements.size) { index ->
                        FilterPill(
                            text = state.statements[index].semester,
                            selected = index == state.selectedIndex,
                            onClick = { onSelectSemester(index) }
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(horizontal = BubtSpacing.Screen),
                    verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
                ) {
                    Text(
                        text = "Semester",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BreakdownCard(statement)
                    CurrentDueCard(statement)
                    if (statement.overpaid > 0) {
                        CreditNote(statement.overpaid)
                    }
                    Text(
                        text = rememberSyncedLabel(state.lastSyncedAt).replace("Synced", "Synced from Annex ·"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BubtPrimaryButton(
                    text = "Download Fee Statement (PDF)",
                    onClick = onDownloadStatement,
                    leadingIcon = Icons.Outlined.Download,
                    modifier = Modifier.fillMaxWidth()
                )
                BubtTextButton(text = "Payment Instructions", onClick = onPaymentInstructions)
            }
        }
    }

    if (state.showInfo) {
        ModalBottomSheet(onDismissRequest = { onInfoVisibleChange(false) }) {
            FeeInfoSheet()
        }
    }
}

@Composable
private fun BreakdownCard(statement: FeeStatement) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = BubtSpacing.CardPadding)) {
            FeeLine("Total Demand", "Base tuition + fees", formatTaka(statement.totalDemand))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FeeLine("Waiver", statement.waiverLabel, "– ${formatTaka(statement.waiver)}")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FeeLine("Paid", null, formatTaka(statement.paid))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FeeLine("Overpaid", null, formatTaka(statement.overpaid), muted = statement.overpaid == 0)
        }
    }
}

@Composable
private fun FeeLine(label: String, caption: String?, amount: String, muted: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BubtSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            caption?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall,
            color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CurrentDueCard(statement: FeeStatement) {
    val settled = statement.currentDue == 0
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = if (settled) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primaryContainer,
        contentColor = if (settled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(BubtSpacing.CardPadding),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Current Due", style = MaterialTheme.typography.labelLarge)
                Text(text = formatTaka(statement.currentDue), style = MaterialTheme.typography.displaySmall)
            }
            Text(
                text = if (settled) "Settled" else "by ${statement.dueDate.day}\n${statement.dueDate.monthShort}",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun CreditNote(amount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Field,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(BubtSpacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Savings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(BubtSpacing.Sm))
            Text(
                text = "${formatTaka(amount)} overpaid will be credited to your next semester.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeeInfoSheet() {
    val lines = listOf(
        "Total Demand" to "Everything billed for the semester: base tuition plus semester, lab and other fees.",
        "Waiver" to "Scholarships or discounts (merit, sibling, etc.) deducted from the demand.",
        "Paid" to "Payments received so far for this semester.",
        "Overpaid" to "Anything paid beyond the net amount. It's carried forward as credit.",
        "Current Due" to "Total Demand − Waiver − Paid, payable by the date shown."
    )
    Column(
        modifier = Modifier.padding(start = BubtSpacing.Screen, end = BubtSpacing.Screen, bottom = BubtSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        Text("What each line means", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        lines.forEach { (title, body) ->
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Sm))
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun FeesScreenPreview() {
    BUBTConnectMaterial3Theme {
        FeesScreen(
            state = FeesUiState(
                isConnected = true,
                statements = SampleAcademicsRepository.feeStatements(),
                lastSyncedAt = System.currentTimeMillis() - 5 * 60_000
            ),
            onBack = {},
            onConnectAnnex = {},
            onSelectSemester = {},
            onInfoVisibleChange = {},
            onDownloadStatement = {},
            onPaymentInstructions = {}
        )
    }
}
