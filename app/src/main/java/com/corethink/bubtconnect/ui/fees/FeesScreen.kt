package com.corethink.bubtconnect.ui.fees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Event
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
import androidx.compose.ui.unit.sp
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
import com.corethink.bubtconnect.ui.design.BubtOnSuccessContainerLight
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccessContainerLight
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

/** 1v · Fees & waivers. Matches HTML 1v exactly. */
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
                modifier = Modifier.padding(horizontal = 20.dp),
                trailing = {
                    if (state.isConnected) {
                        IconButton(onClick = { onInfoVisibleChange(true) }) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "What each line means",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CurrentDueCard(statement)
                    BreakdownCard(statement)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Synced from Annex · ${rememberSyncedLabel(state.lastSyncedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BubtPrimaryButton(
                    text = "Download Fee Statement (PDF)",
                    onClick = onDownloadStatement,
                    leadingIcon = Icons.Outlined.Download,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                BubtTextButton(
                    text = "Payment Instructions",
                    onClick = onPaymentInstructions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                )
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
private fun CurrentDueCard(statement: FeeStatement) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-50).dp)
                    .size(150.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "CURRENT DUE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = formatTaka(statement.currentDue),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    lineHeight = 42.sp,
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Event,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Due by 31 Oct · 20 days left",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White)
                    )
                }

                Text(
                    text = "68% of net payable cleared",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BreakdownCard(statement: FeeStatement) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
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
                tileColor = BubtSuccessContainerLight,
                tint = BubtOnSuccessContainerLight,
                label = "Waiver",
                caption = statement.waiverLabel,
                amount = "− ${formatTaka(statement.waiver)}",
                amountColor = BubtOnSuccessContainerLight
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            FeeLine(
                icon = Icons.Outlined.CheckCircle,
                tileColor = MaterialTheme.colorScheme.primaryContainer,
                tint = MaterialTheme.colorScheme.primary,
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
                amount = formatTaka(statement.overpaid)
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
    caption: String,
    amount: String,
    amountColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tileColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = amountColor
        )
    }
}

@Composable
private fun FeeInfoSheet() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Fee breakdown explained",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Current Due = Total Demand − Waiver − Paid. Overpaid fees, if any, carry forward to your next semester demand.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun FeesScreenPreview() {
    BUBTConnectMaterial3Theme {
        FeesScreen(
            state = FeesUiState(
                isConnected = true,
                statements = SampleAcademicsRepository.feeStatements()
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