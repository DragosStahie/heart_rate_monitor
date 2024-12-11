package com.dragosstahie.heartratemonitor.ui.common

import android.graphics.PorterDuff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.component
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.common.shape.dashedShape
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.random.Random


private val x = (1..100).toList()


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
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            while (isActive) {
                modelProducer.runTransaction {
                    /* Learn more:
                    https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
                    lineSeries { series(x = x, y = x.map { Random.nextFloat() * 30 - 10 }) }
                }
                delay(2000)
            }
        }
    }

    HeartRateChart(modelProducer, modifier)
}

@Composable
private fun HeartRateChart(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
    val colors = chartColors
    val marker = rememberMarker()
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider =
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill =
                        remember(colors) {
                            LineCartesianLayer.LineFill.double(fill(colors[0]), fill(colors[1]))
                        },
                        areaFill =
                        remember(colors) {
                            LineCartesianLayer.AreaFill.double(
                                topFill =
                                fill(
                                    DynamicShader.compose(
                                        DynamicShader.component(
                                            component =
                                            shapeComponent(
                                                fill = fill(colors[0]),
                                                shape = CorneredShape.Pill,
                                                margins = dimensions(1.dp),
                                            ),
                                            componentSize = 6.dp,
                                        ),
                                        DynamicShader.verticalGradient(
                                            arrayOf(
                                                Color.Black,
                                                Color.Transparent
                                            )
                                        ),
                                        PorterDuff.Mode.DST_IN,
                                    )
                                ),
                                bottomFill =
                                fill(
                                    DynamicShader.compose(
                                        DynamicShader.component(
                                            component =
                                            shapeComponent(
                                                fill = fill(colors[1]),
                                                shape = Shape.Rectangle,
                                                margins = dimensions(horizontal = 2.dp),
                                            ),
                                            componentSize = 5.dp,
                                            checkeredArrangement = false,
                                        ),
                                        DynamicShader.verticalGradient(
                                            arrayOf(
                                                Color.Transparent,
                                                Color.Black
                                            )
                                        ),
                                        PorterDuff.Mode.DST_IN,
                                    )
                                ),
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