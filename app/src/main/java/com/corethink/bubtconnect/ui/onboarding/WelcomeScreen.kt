package com.corethink.bubtconnect.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.corethink.bubtconnect.R
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPagerIndicator
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtTextButton

private data class WelcomeSlide(
    val title: String?,
    val body: String,
    val icon: ImageVector?
)

private val slides = listOf(
    WelcomeSlide(
        "BUBT Companion",
        "Your whole campus, in one pocket.\nScan assignments with cover pages, track your routine, check results and share notes.",
        null
    ),
    WelcomeSlide("Never miss a class", "Your routine, exams and calendar\nsynced in one place.", Icons.Outlined.CalendarMonth),
    WelcomeSlide("Scan & share", "Scan notices and notes, then share\nthem with your section.", Icons.Outlined.QrCodeScanner)
)

/** 1b · Welcome splash. "Have an Annex account?" skips straight to 1e. */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onAnnexLogin: () -> Unit
) {
    val pagerState = rememberPagerState { slides.size }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                WelcomeSlideContent(slides[page])
            }

            BubtPagerIndicator(count = slides.size, selected = pagerState.currentPage)
            Spacer(modifier = Modifier.height(BubtSpacing.Xl))

            BubtPrimaryButton(
                text = "Get Started",
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(BubtSpacing.Sm))
            BubtTextButton(text = "I have an Annex account", onClick = onAnnexLogin)
        }
    }
}

@Composable
private fun WelcomeSlideContent(slide: WelcomeSlide) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (slide.icon == null) {
            BubtLogo(size = 140.dp)
        } else {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Xl))
        Text(
            text = slide.title ?: stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Md))
        Text(
            text = slide.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
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

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    BUBTConnectMaterial3Theme {
        WelcomeScreen(onGetStarted = {}, onAnnexLogin = {})
    }
}
