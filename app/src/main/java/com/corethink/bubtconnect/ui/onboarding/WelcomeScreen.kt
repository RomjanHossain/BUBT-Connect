package com.corethink.bubtconnect.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.corethink.bubtconnect.R
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.design.BubtWarning

/** 1c · Welcome splash. Left-aligned single screen with a floating day/event illustration. */
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
                .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Xl),
            horizontalAlignment = Alignment.Start
        ) {
            WelcomeIllustration()
            Spacer(modifier = Modifier.height(BubtSpacing.Xl))

            Row(verticalAlignment = Alignment.CenterVertically) {
                BubtLogo(size = 40.dp)
                Spacer(modifier = Modifier.width(BubtSpacing.Sm))
                Text(
                    text = "BUBT Companion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Md))
            Text(
                text = "Your whole campus, in one pocket.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(BubtSpacing.Sm))
            Text(
                text = "Scan assignments with cover pages, track your routine, check results and share notes.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            BubtPrimaryButton(
                text = "Get Started",
                onClick = onGetStarted,
                leadingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = MaterialTheme.shapes.medium,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    )
            )
            Spacer(modifier = Modifier.height(BubtSpacing.Sm))
            BubtTextButton(
                text = "I have an Annex account",
                onClick = onAnnexLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/** Soft circle with a rotated paper card, floating event card and a success check badge — stands in for the Lottie loop. */
@Composable
private fun WelcomeIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp, 234.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        )

        // Rotated paper sheet with a logo dot and placeholder lines.
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
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
                PaperLine(fraction = 0.8f)
                PaperLine(fraction = 0.55f)
                Spacer(modifier = Modifier.height(8.dp))
                PaperLine(fraction = 1f)
                PaperLine(fraction = 0.85f)
                PaperLine(fraction = 0.7f)
                PaperLine(fraction = 0.8f)
            }
        }

        // Floating day/event card.
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
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

        // Success check badge.
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

        // Decorative confetti bits.
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
                .clip(MaterialTheme.shapes.extraSmall)
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
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

/** The BUBT crest on a white disc — the crest artwork assumes a white ground in both themes. */
@Composable
fun BubtLogo(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = "BUBT logo",
            // Adaptive-icon art keeps the crest inside the centre 66%; scale it to fill the disc.
            modifier = Modifier
                .size(size)
                .graphicsLayer { scaleX = 1.45f; scaleY = 1.45f }
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WelcomeScreenPreview() {
    BUBTConnectMaterial3Theme {
        WelcomeScreen(onGetStarted = {}, onAnnexLogin = {})
    }
}
