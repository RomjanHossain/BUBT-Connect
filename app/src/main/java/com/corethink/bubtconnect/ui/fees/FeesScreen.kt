package com.corethink.bubtconnect.ui.fees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Sync
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.corethink.bubtconnect.ui.design.BubtSuccess
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

/** 1v · Fees & waivers. ⓘ opens a sheet explaining each line. Requires Annex. */
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
                    CurrentDueCard(statement)
                    BreakdownCard(statement)
                    SyncedRow(state.lastSyncedAt)
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
            FeeLine(
                icon = Icons.Outlined.ReceiptLong,
                tileColor = MaterialTheme.colorScheme.surfaceContainer,
                tint = MaterialTheme.colorScheme.onSurface,
                label = "Total Demand",
                caption = "Base tuition + fees",
                amount = formatTaka(statement.totalDemand)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FeeLine(
                icon = Icons.Outlined.Sell,
                tileColor = MaterialTheme.colorScheme.tertiaryContainer,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                label = "Waiver",
                caption = statement.waiverLabel,
                amount = "– ${formatTaka(statement.waiver)}",
                amountColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FeeLine(
                icon = Icons.Outlined.CheckCircle,
                tileColor = MaterialTheme.colorScheme.primaryContainer,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                label = "Paid",
                caption = "${statement.installments} installments",
                amount = formatTaka(statement.paid)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FeeLine(
                icon = Icons.Outlined.Savings,
                tileColor = MaterialTheme.colorScheme.surfaceContainer,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                label = "Overpaid",
                caption = "Carried to next semester",
                amount = formatTaka(statement.overpaid),
                amountColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeeLine(
    icon: ImageVector,
    tileColor: Color,
    tint: Color,
    label: String,
    caption: String?,
    amount: String,
    amountColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tileColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(BubtSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            caption?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(BubtSpacing.Sm))
        Text(text = amount, style = MaterialTheme.typography.titleSmall, color = amountColor)
    }
}

/** Solid-primary hero that flips to success green and reads "Credit" when overpaid (1v). */
@Composable
private fun CurrentDueCard(statement: FeeStatement) {
    val credit = statement.overpaid > 0
    val settled = statement.currentDue == 0 && !credit
    val container = when {
        credit -> BubtSuccess
        settled -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.primary
    }
    val onContainer = when {
        credit -> Color.White
        settled -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onPrimary
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = onContainer
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = (-60).dp)
                    .size(150.dp)
                    .background(onContainer.copy(alpha = 0.1f), CircleShape)
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = when {
                        credit -> "CREDIT"
                        settled -> "SETTLED"
                        else -> "CURRENT DUE"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = onContainer.copy(alpha = 0.85f)
                )
                Text(
                    text = formatTaka(if (credit) statement.overpaid else statement.currentDue),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                val dueInDays = statement.dueDate.daysUntil()
                Text(
                    text = when {
                        credit -> "Carried to next semester"
                        settled -> "Nothing due this semester"
                        dueInDays >= 0 -> "Due by ${statement.dueDate.day} ${statement.dueDate.monthShort} · $dueInDays days left"
                        else -> "Was due ${statement.dueDate.day} ${statement.dueDate.monthShort}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = BubtSpacing.Xs)
                )
                Spacer(modifier = Modifier.height(BubtSpacing.Md))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(onContainer.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(statement.percentCleared / 100f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(onContainer)
                    )
                }
                Text(
                    text = "${statement.percentCleared}% of net payable cleared",
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainer.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = BubtSpacing.Sm)
                )
            }
        }
    }
}

@Composable
private fun SyncedRow(lastSyncedAt: Long?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Xs)
    ) {
        Icon(
            imageVector = Icons.Outlined.Sync,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = rememberSyncedLabel(lastSyncedAt).replace("Synced", "Synced from Annex ·"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
