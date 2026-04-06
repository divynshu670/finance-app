package com.example.financecompanion.views.components.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingFeatureCard(
    title: String,
    description: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val iconSize = if (compact) 34.dp else 44.dp
    val horizontalPadding = if (compact) 16.dp else 20.dp
    val verticalPadding = if (compact) 14.dp else 18.dp
    val titleSize = if (compact) 18.sp else 22.sp
    val descriptionSize = if (compact) 14.sp else 16.sp
    val descriptionLineHeight = if (compact) 20.sp else 22.sp
    val minHeight = if (compact) 88.dp else 108.dp
    val titleBottomSpace = if (compact) 4.dp else 6.dp
    val shapeRadius = if (compact) 20.dp else 24.dp
    val iconTextSpace = if (compact) 14.dp else 16.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(shapeRadius))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.12f))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = icon,
            contentDescription = title,
            modifier = Modifier.size(iconSize),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(iconTextSpace))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.background,
                fontSize = titleSize,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(titleBottomSpace))

            Text(
                text = description,
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                fontSize = descriptionSize,
                lineHeight = descriptionLineHeight
            )
        }
    }
}