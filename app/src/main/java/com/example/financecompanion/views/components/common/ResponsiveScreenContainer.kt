package com.example.financecompanion.views.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.financecompanion.R

@Composable
fun ResponsiveScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val maxContentWidth = dimensionResource(R.dimen.screen_max_content_width)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = maxContentWidth)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}
