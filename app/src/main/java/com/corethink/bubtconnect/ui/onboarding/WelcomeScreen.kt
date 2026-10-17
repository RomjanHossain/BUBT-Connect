package com.corethink.bubtconnect.ui.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtTextButton
import com.corethink.bubtconnect.ui.design.BubtWarning
import kotlinx.coroutines.launch

/** 1c · Welcome, as a three-slide carousel (the indicator always showed three dots). */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onAnnexLogin: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val onLastPage = pagerState.currentPage == 2

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 16.dp)
        ) {
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
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    when (page) {
                        0 -> WelcomeIllustration()
                        1 -> ScanIllustration()
                        else -> CommunityIllustration()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    SlideCopy(page)
                }
            }

            // Pagination indicator: 22x6dp pill for the active slide, 6x6dp dots otherwise
            SlideDots(current = pagerState.currentPage)

            // Primary button advances the carousel; the last slide starts onboarding.
            BubtPrimaryButton(
                text = if (onLastPage) "Get Started" else "Next",
                onClick = {
                    if (onLastPage) {
                        onGetStarted()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                trailingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                modifier = Modifier.fillMaxWidth()
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

/** Headline + supporting copy for each slide, styled exactly like the 1c mock. */
@Composable
private fun SlideCopy(page: Int) {
    Text(
        text = when (page) {
            0 -> "Your whole campus, in one pocket."
            1 -> "Scan assignments, cover included."
            else -> "Share notes with your batchmates."
        },
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.42).sp,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = when (page) {
            0 -> "Scan assignments with cover pages, track your routine, check results and share notes."
            1 -> "Auto-detect page edges, straighten and enhance them, then merge everything into one searchable PDF."
            else -> "Publish your scanned references, tag them, and let classmates find quality work — anonymously if you prefer."
        },
        style = MaterialTheme.typography.bodyLarge,
        fontSize = 15.sp,
        lineHeight = 22.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SlideDots(current: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val active = index == current
            val width by animateDpAsState(
                targetValue = if (active) 22.dp else 6.dp,
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                label = "dotWidth"
            )
            Box(
                modifier = Modifier
                    .size(width, 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
            if (index != 2) Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

/** Soft easing used by every floating badge across the three illustrations. */
@Composable
private fun rememberFloat(period: Int, distance: Float = -8f): Float {
    val transition = rememberInfiniteTransition(label = "welcomeFloat")
    val value by transition.animateFloat(
        initialValue = 0f,
        targetValue = distance,
        animationSpec = infiniteRepeatable(
            animation = tween(period, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    return value
}

/** Slide 1 · 1c Illustration matching HTML design exactly. */
@Composable
private fun WelcomeIllustration(modifier: Modifier = Modifier) {
    val floatF1 = rememberFloat(2000)
    val floatF2 = rememberFloat(2500)
    val floatF3 = rememberFloat(1800)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(width = 280.dp, height = 270.dp)
        ) {
            // Soft primary container circle in center (inset: 18px 30px => left 30.dp, top 18.dp, 220x234dp)
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 18.dp)
                    .size(220.dp, 234.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            // Rotated paper sheet (-8deg) (.f1: left 52px, top 60px, width 108px, height 140px)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .offset(x = 52.dp, y = (60 + floatF1).dp)
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
                            .background(Color(0xFF2563EB))
                            .align(Alignment.CenterHorizontally)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.80f)
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
                    Spacer(modifier = Modifier.height(1.dp))
                    PaperLine(fraction = 1f)
                    PaperLine(fraction = 0.85f)
                    PaperLine(fraction = 0.70f)
                    PaperLine(fraction = 0.80f)
                }
            }

            // Floating event card (SUN 11) (.f2: right 36px, top 44px, width 92px => left = 280-36-92 = 152.dp)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .offset(x = 152.dp, y = (44 + floatF2).dp)
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
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "11",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Success check badge (.f3: right 58px, bottom 44px, size 56px => left = 280-58-56 = 166.dp, top = 270-44-56 = 170.dp)
            Surface(
                shape = CircleShape,
                color = BubtSuccess,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .offset(x = 166.dp, y = (170 + floatF3).dp)
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
            // Dot 1 (warning yellow): left 30px, top 30px, size 10px
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 30.dp)
                    .size(10.dp)
                    .background(BubtWarning, CircleShape)
            )
            // Square 2 (blue 20deg): left 24px, bottom 56px => top = 270-56-14 = 200.dp
            Box(
                modifier = Modifier
                    .offset(x = 24.dp, y = 200.dp)
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .graphicsLayer { rotationZ = 20f }
            )
            // Dot 3 (blue 50% opacity): right 20px => left = 280-20-8 = 252.dp, top 150px
            Box(
                modifier = Modifier
                    .offset(x = 252.dp, y = 150.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

/**
 * Slide 2 · the scanner: a page locked by detection brackets, a PDF chip and a
 * check badge — the capture screen's visual language in miniature.
 */
@Composable
private fun ScanIllustration(modifier: Modifier = Modifier) {
    val floatPaper = rememberFloat(2000)
    val floatPdf = rememberFloat(2500)
    val floatCheck = rememberFloat(1800)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(width = 280.dp, height = 270.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 18.dp)
                    .size(220.dp, 234.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            // The page, tilted like on a desk
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .offset(x = 62.dp, y = (54 + floatPaper).dp)
                    .size(110.dp, 142.dp)
                    .graphicsLayer { rotationZ = -6f }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.CenterHorizontally)
                    )
                    PaperLine(fraction = 0.7f)
                    Spacer(modifier = Modifier.height(1.dp))
                    PaperLine(fraction = 1f)
                    PaperLine(fraction = 0.85f)
                    PaperLine(fraction = 0.70f)
                    PaperLine(fraction = 0.80f)
                    PaperLine(fraction = 0.55f)
                }
            }

            // Detection brackets, accent blue, over the page
            val bracketColor = MaterialTheme.colorScheme.primary
            Canvas(
                modifier = Modifier
                    .offset(x = 52.dp, y = (44 + floatPaper).dp)
                    .size(130.dp, 162.dp)
            ) {
                val arm = 24.dp.toPx()
                val stroke = 4.dp.toPx()
                val color = bracketColor
                val corners = listOf(
                    Offset(0f, 0f) to Offset(1f, 1f),
                    Offset(size.width, 0f) to Offset(-1f, 1f),
                    Offset(0f, size.height) to Offset(1f, -1f),
                    Offset(size.width, size.height) to Offset(-1f, -1f)
                )
                corners.forEach { (corner, direction) ->
                    drawLine(color, corner, corner + Offset(arm * direction.x, 0f), stroke, StrokeCap.Round)
                    drawLine(color, corner, corner + Offset(0f, arm * direction.y), stroke, StrokeCap.Round)
                }
            }

            // PDF chip card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .offset(x = 186.dp, y = (58 + floatPdf).dp)
                    .width(80.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Ready-to-merge check badge
            Surface(
                shape = CircleShape,
                color = BubtSuccess,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .offset(x = 170.dp, y = (168 + floatCheck).dp)
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

            // Decorative bits
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 30.dp)
                    .size(10.dp)
                    .background(BubtWarning, CircleShape)
            )
            Box(
                modifier = Modifier
                    .offset(x = 252.dp, y = 150.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

/**
 * Slide 3 · the community: a shared note with tag chips and a share badge —
 * matches the publish card on the scanner's success step.
 */
@Composable
private fun CommunityIllustration(modifier: Modifier = Modifier) {
    val floatNote = rememberFloat(2000)
    val floatTags = rememberFloat(2500)
    val floatShare = rememberFloat(1800)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(width = 280.dp, height = 270.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 18.dp)
                    .size(220.dp, 234.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            // The captured page being published
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .offset(x = 56.dp, y = (52 + floatNote).dp)
                    .size(118.dp, 150.dp)
                    .graphicsLayer { rotationZ = -7f }
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    PaperLine(fraction = 1f)
                    PaperLine(fraction = 0.9f)
                    PaperLine(fraction = 0.8f)
                    PaperLine(fraction = 0.95f)
                    PaperLine(fraction = 0.6f)
                }
            }

            // Tag chips card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .offset(x = 150.dp, y = (74 + floatTags).dp)
                    .width(112.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        TagChip(mini = true, text = "#CSE231")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        TagChip(mini = true, text = "#Notes")
                        TagChip(mini = true, text = "#Data")
                    }
                }
            }

            // Share badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .offset(x = 168.dp, y = (168 + floatShare).dp)
                    .size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Decorative bits
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 30.dp)
                    .size(10.dp)
                    .background(BubtWarning, CircleShape)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .offset(x = 196.dp, y = 6.dp)
                    .width(66.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "9:41",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TagChip(mini: Boolean, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
