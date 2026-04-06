package com.example.financecompanion.views.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financecompanion.R
import com.example.financecompanion.views.components.common.AppIconCard
import com.example.financecompanion.views.components.common.PrimaryButton
import com.example.financecompanion.views.components.onboarding.OnboardingFeatureCard

@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit
) {
    val horizontalPadding = onboardingHorizontalPadding()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding)
        ) {
            val shortHeight = maxHeight < 760.dp
            val veryShortHeight = maxHeight < 700.dp
            val narrowWidth = maxWidth < 360.dp

            val logoCardSize = when {
                veryShortHeight -> 76.dp
                shortHeight -> 84.dp
                else -> 92.dp
            }

            val logoImageSize = when {
                veryShortHeight -> 28.dp
                shortHeight -> 32.dp
                else -> 36.dp
            }

            val titleSize = when {
                veryShortHeight -> 18.sp
                shortHeight -> 20.sp
                else -> 22.sp
            }

            val subtitleSize = when {
                veryShortHeight -> 14.sp
                shortHeight -> 15.sp
                else -> 16.sp
            }

            val subtitleLineHeight = when {
                veryShortHeight -> 20.sp
                shortHeight -> 22.sp
                else -> 24.sp
            }

            val footerSize = 13.sp

            val topSpacerWeight = if (veryShortHeight) 0.20f else 0.35f
            val betweenHeaderAndCardsWeight = if (veryShortHeight) 0.20f else 0.30f
            val betweenCardsAndButtonWeight = if (veryShortHeight) 0.25f else 0.45f

            val titleSubtitleGap = if (veryShortHeight) 8.dp else 12.dp
            val buttonFooterGap = if (veryShortHeight) 12.dp else 18.dp
            val footerBottomGap = if (veryShortHeight) 10.dp else 16.dp
            val cardsSpacing = if (veryShortHeight) 12.dp else 18.dp

            val contentMaxWidth = if (maxWidth > 500.dp) 460.dp else Dp.Unspecified

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (contentMaxWidth != Dp.Unspecified) {
                            Modifier.widthIn(max = contentMaxWidth)
                        } else {
                            Modifier
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(topSpacerWeight))

                AppIconCard(
                    containerSize = logoCardSize,
                    imageSize = logoImageSize,
                    cornerRadius = if (veryShortHeight) 20.dp else 24.dp,
                    backgroundColor = Color.White
                )

                Spacer(modifier = Modifier.weight(0.18f))

                Text(
                    text = "Finance Companion",
                    color = Color.White,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.padding(top = titleSubtitleGap))

                Text(
                    text = "Your personal guide to better money habits and financial clarity",
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = subtitleSize,
                    lineHeight = subtitleLineHeight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(
                        if (narrowWidth) 0.98f else 0.90f
                    )
                )

                Spacer(modifier = Modifier.weight(betweenHeaderAndCardsWeight))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(cardsSpacing)
                ) {
                    OnboardingFeatureCard(
                        title = "Track Daily Habits",
                        description = "Understand where your money goes with simple transaction tracking",
                        icon = painterResource(id = R.drawable.wallet),
                        compact = shortHeight || narrowWidth
                    )

                    OnboardingFeatureCard(
                        title = "Set Meaningful Goals",
                        description = "Stay motivated with savings goals and spending challenges",
                        icon = painterResource(id = R.drawable.goal),
                        compact = shortHeight || narrowWidth
                    )

                    OnboardingFeatureCard(
                        title = "Get Smart Insights",
                        description = "Discover patterns and make better financial decisions",
                        icon = painterResource(id = R.drawable.statistics),
                        compact = shortHeight || narrowWidth
                    )
                }

                Spacer(modifier = Modifier.weight(betweenCardsAndButtonWeight))

                PrimaryButton(
                    text = "Get Started",
                    onClick = onGetStartedClick,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    height = if (veryShortHeight) 52.dp else 56.dp,
                    cornerRadius = if (veryShortHeight) 24.dp else 28.dp,
                    textSize = if (veryShortHeight) 16.sp else 18.sp
                )

                Spacer(modifier = Modifier.padding(top = buttonFooterGap))

                Text(
                    text = "All your data stays on your device • No account needed",
                    color = Color.White,
                    fontSize = footerSize,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(
                        if (narrowWidth) 1f else 0.95f
                    )
                )

                Spacer(modifier = Modifier.padding(top = footerBottomGap))
            }
        }
    }
}

@Composable
private fun onboardingHorizontalPadding(): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    return when {
        screenWidth < 360 -> 16.dp
        screenWidth < 420 -> 24.dp
        else -> 32.dp
    }
}