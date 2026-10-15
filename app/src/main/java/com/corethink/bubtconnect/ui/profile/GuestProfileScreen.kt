package com.corethink.bubtconnect.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.StudentProfile
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.design.BubtWarning

@Composable
fun GuestProfileRoute(
    onEditProfile: () -> Unit,
    onConnected: () -> Unit,
    onMaybeLater: () -> Unit,
    profileViewModel: GuestProfileViewModel = viewModel()
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    GuestProfileScreen(
        profile = profile,
        onConnectAnnex = onConnected,
        onMaybeLater = onMaybeLater
    )
}

/** 1e · Guest profile + Connect Annex CTA. Matches HTML design 1e exactly. */
@Composable
fun GuestProfileScreen(
    profile: StudentProfile?,
    onConnectAnnex: () -> Unit,
    onMaybeLater: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // App Bar: "Profile" title on left, "Guest mode" badge on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guest mode",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // User header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(72.dp)) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Face,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(BubtWarning)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = profile?.name ?: "Rafi Ahmed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Guest · data stored on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Locked Profile Summary Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryRow(label = "Roll", value = profile?.roll ?: "22235103045")
                    SummaryRow(label = "Department", value = profile?.department ?: "CSE")
                    SummaryRow(label = "Semester", value = "${profile?.semester ?: "5th"} · Fall 2026")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Connect Annex Card (1e) — No input fields, CTA button navigates to 1f
            ConnectAnnexHeroCard(onConnect = onConnectAnnex)

            Spacer(modifier = Modifier.height(12.dp))

            BubtTextButton(
                text = "Maybe later",
                onClick = onMaybeLater,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(40.dp)
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = "Locked",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

/** Hero CTA Card for 1e — Primary filled card with feature pills and "Connect Annex" white button. */
@Composable
fun ConnectAnnexHeroCard(
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Box {
            // Semi-transparent decorative circle top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Connect your Annex account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Use your BUBT Annex ID and password to unlock:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FeatureTag("Attendance")
                    FeatureTag("Results")
                    FeatureTag("Class routine")
                }

                Surface(
                    onClick = onConnect,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connect Annex",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureTag(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.18f),
        contentColor = Color.White
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuestProfileScreenPreview() {
    BUBTConnectMaterial3Theme {
        GuestProfileScreen(
            profile = StudentProfile(roll = "22235103045", department = "CSE", semester = "5th"),
            onConnectAnnex = {},
            onMaybeLater = {}
        )
    }
}