package com.corethink.bubtconnect.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.design.BubtWarning

/** 1c · Welcome. Matches 1c from Student App UI.dc.html exactly. */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onAnnexLogin: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            WelcomeIllustration()

            Spacer(modifier = Modifier.height(28.dp))

            // Brand row: 'B' badge (28x28dp, 9dp radius) + BUBT Companion
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "B",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BUBT Companion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Your whole campus, in one pocket.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Scan assignments with cover pages, track your routine, check results and share notes.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Pagination indicator: 22x6dp pill, 6x6dp dot, 6x6dp dot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp, 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }

            // Primary button: "Get Started" text with arrow_forward icon on the RIGHT (trailing)
            BubtPrimaryButton(
                text = "Get Started",
                onClick = onGetStarted,
                trailingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
            )

            Spacer(modifier = Modifier.height(6.dp))

            BubtTextButton(
                text = "I have an Annex account",
                onClick = onAnnexLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )
        }
    }
}

/** 1c Illustration matching HTML design exactly. */
@Composable
private fun WelcomeIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft primary container circle in center
        Box(
            modifier = Modifier
                .size(220.dp, 234.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )

        // Rotated paper sheet (-8deg)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            modifier = Modifier
                .offset(x = (-34).dp, y = (-5).dp)
                .size(108.dp, 140.dp)
                .graphicsLayer { rotationZ = -8f }
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.CenterHorizontally)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCBD5E1))
                        .align(Alignment.CenterHorizontally)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(2.dp))
                PaperLine(fraction = 1f)
                PaperLine(fraction = 0.85f)
                PaperLine(fraction = 0.7f)
                PaperLine(fraction = 0.8f)
            }
        }

        // Floating event card (SUN 11)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            modifier = Modifier
                .offset(x = 58.dp, y = (-48).dp)
                .width(92.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "SUN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "11",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Success check badge
        Surface(
            shape = CircleShape,
            color = BubtSuccess,
            shadowElevation = 8.dp,
            modifier = Modifier
                .offset(x = 54.dp, y = 63.dp)
                .size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Decorative floating bits
        Box(
            modifier = Modifier
                .offset(x = (-105).dp, y = (-100).dp)
                .size(10.dp)
                .background(BubtWarning, CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = (-109).dp, y = 72.dp)
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
                .graphicsLayer { rotationZ = 20f }
        )
        Box(
            modifier = Modifier
                .offset(x = 116.dp, y = 19.dp)
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
        )
    }
}

@Composable
private fun PaperLine(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(fraction)
            .height(3.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WelcomeScreenPreview() {
    BUBTConnectMaterial3Theme {
        WelcomeScreen(onGetStarted = {}, onAnnexLogin = {})
    }
}