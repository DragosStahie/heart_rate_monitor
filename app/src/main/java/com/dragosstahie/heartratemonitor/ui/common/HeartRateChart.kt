package com.dragosstahie.heartratemonitor.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


private val zone1Color = Color(87, 162, 241)
private val zone2Color = Color(123, 237, 224)
private val zone3Color = Color(203, 251, 80)
private val zone4Color = Color(238, 136, 53)
private val zone5Color = Color(234, 55, 111)


@Composable
fun HeartRateChart(
    hearRateReadings: List<Int>,
    modifier: Modifier = Modifier,
) {
    val maxHeartRate by remember(hearRateReadings) {
        derivedStateOf {
            hearRateReadings.maxOrNull() ?: -1
        }
    }

    val lazyState = rememberLazyListState()

    LaunchedEffect(hearRateReadings.size) {
        if (hearRateReadings.isNotEmpty()) {
            lazyState.animateScrollToItem(hearRateReadings.size - 1)
        }
    }

    LazyRow(
        state = lazyState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        items(hearRateReadings) { reading ->
            ChartBar(
                percentFilled = reading.toFloat() / maxHeartRate,
                color = reading.getZoneColor()
            )
        }
    }
}

@Composable
private fun ChartBar(
    percentFilled: Float,
    color: Color,
) {
    Spacer(
        modifier = Modifier
            .fillMaxHeight(percentFilled)
            .width(4.dp)
            .clip(RoundedCornerShape(1.dp, 1.dp, 0.dp, 0.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.5f)
                    )
                ),
            )
    )
}


private fun Int.getZoneColor(): Color = when (this) {
    in 0..135 -> zone1Color
    in 136..149 -> zone2Color
    in 150..163 -> zone3Color
    in 164..177 -> zone4Color
    in 150..Int.MAX_VALUE -> zone5Color
    else -> Color.Gray
}

@Preview(showBackground = true)
@Composable
fun HeartRateChartPreview(modifier: Modifier = Modifier) {
    HeartRateChart(
        hearRateReadings = (50..165).toList() + (160 downTo 80).toList() + (90..196).toList() + (195 downTo 50).toList(),
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp),
    )
}