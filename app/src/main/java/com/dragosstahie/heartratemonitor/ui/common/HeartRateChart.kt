package com.dragosstahie.heartratemonitor.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dragosstahie.heartratemonitor.ui.theme.chartColorNegative
import com.dragosstahie.heartratemonitor.ui.theme.chartColorPositive
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shape.dashedShape
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.sample.showcase.rememberMarker


private val x =
    (1..50).toList() + (50 downTo 1).toList() + (1..50).toList() + (50 downTo 1).toList()


private val chartColors
    @ReadOnlyComposable
    @Composable
    get() =
        listOf(
            chartColorPositive,
            chartColorNegative,
        )

@Composable
internal fun HeartRateChart(modifier: Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) { modelProducer.runTransaction { lineSeries { series(x) } } }

    HeartRateChart(modelProducer, modifier)
}

@Composable
private fun HeartRateChart(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
    val colors = chartColors
    val marker = rememberMarker()
    CartesianChartHost(
        scrollState = rememberVicoScrollState(
            scrollEnabled = true,
            initialScroll = Scroll.Absolute.End,
            autoScroll = Scroll.Absolute.End,
        ),
        zoomState = rememberVicoZoomState(
            zoomEnabled = false,
            initialZoom = Zoom.x(200.0),
        ),
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider =
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill =
                        remember(colors) {
                            LineCartesianLayer.LineFill.single(
                                fill(
                                    DynamicShader.horizontalGradient(
                                        android.graphics.Color.BLUE,
                                        android.graphics.Color.GREEN,
                                        android.graphics.Color.YELLOW,
                                        android.graphics.Color.RED,
                                    )
                                )
                            )
                        },
                        areaFill =
                        remember(colors) {
                            LineCartesianLayer.AreaFill.single(
                                fill(
                                    DynamicShader.horizontalGradient(
                                        android.graphics.Color.BLUE,
                                        android.graphics.Color.GREEN,
                                        android.graphics.Color.YELLOW,
                                        android.graphics.Color.RED,
                                    )
                                )
                            )
                        },
                    )
                )
            ),
            startAxis =
            VerticalAxis.rememberStart(
                label =
                rememberAxisLabelComponent(
                    color = MaterialTheme.colorScheme.onBackground,
                    margins = dimensions(end = 8.dp),
                    padding = dimensions(6.dp, 2.dp),
                    background =
                    rememberShapeComponent(
                        fill = Fill.Transparent,
                        shape = CorneredShape.Pill,
                        strokeFill = fill(MaterialTheme.colorScheme.outlineVariant),
                        strokeThickness = 1.dp,
                    ),
                ),
                line = null,
                tick = null,
                guideline =
                rememberLineComponent(
                    fill = fill(MaterialTheme.colorScheme.outlineVariant),
                    shape = dashedShape(
                        shape = CorneredShape.Pill,
                        dashLength = 4.dp,
                        gapLength = 8.dp
                    ),
                ),
                itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 4 }) },
            ),
            bottomAxis =
            HorizontalAxis.rememberBottom(
                guideline = null,
                itemPlacer =
                remember {
                    HorizontalAxis.ItemPlacer.aligned(spacing = 3, addExtremeLabelPadding = true)
                },
            ),
            marker = marker,
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

private fun List<Int>.findMinMaxPoints(): List<Int> {
    val result = mutableListOf<Int>()

    if (size < 3) return emptyList()

    for (index in 2..<size) {
        val first = get(index - 2)
        val second = get(index - 1)
        val third = get(index)

        if(setOf(first, second, third).size < 2) {
            continue
        }

        if (first <= second && second >= third) {
            result += (index - 1)
            continue
        }

        if (first >= second && second <= third) {
            result += (index - 1)
            continue
        }
    }

    return result
}