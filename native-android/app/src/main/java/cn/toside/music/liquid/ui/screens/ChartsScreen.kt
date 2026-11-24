package cn.toside.music.liquid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.LeaderboardItem
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.GlassCircleIconButton
import cn.toside.music.liquid.ui.components.liquidCardPress
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

private val EditorialGradients = listOf(
    listOf(Color(0xFFFA243C), Color(0xFFFF6B6B)),
    listOf(Color(0xFF3A5AFE), Color(0xFF6B8CFF)),
    listOf(Color(0xFF8E2DE2), Color(0xFFDA22FF)),
    listOf(Color(0xFF00A896), Color(0xFF02C39A)),
    listOf(Color(0xFFFF7B00), Color(0xFFFFB703)),
    listOf(Color(0xFF1D3557), Color(0xFF457B9D)),
)

@Composable
fun ChartsScreen(
    vm: MainViewModel,
    bottomClearanceDp: Float,
) {
    val colors = LocalLiquidColors.current
    val state by vm.charts.collectAsState()

    StandardPage { availableWidth, window ->
        val columns = window.pageColumns(availableWidth)
        val featureColumns = window.pageColumns(availableWidth, minCell = 146.dp, maxColumns = 2)
        val featuredBoards = state.boards.take(2)
        val restBoards = state.boards.drop(2)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = bottomClearanceDp.dp + 16.dp),
        ) {
            item("heading") {
                PageHeading("排行榜") {
                    GlassCircleIconButton(onClick = { vm.loadCharts(state.source) }, size = 48.dp, useBlur = false) {
                        Icon(Icons.Rounded.Refresh, "刷新榜单", tint = colors.textPrimary)
                    }
                }
            }
            item("sources") {
                Column(Modifier.padding(bottom = 16.dp)) {
                    PageSourcePicker(MusicSources.ONLINE, state.source, availableWidth >= 440.dp, vm::selectChartsSource)
                }
            }

            when {
                state.loading -> item("loading") { PageSpinner() }
                state.error != null -> item("error") {
                    PageMessage(state.error ?: "排行榜加载失败") { vm.loadCharts(state.source) }
                }
                state.boards.isEmpty() -> item("empty") { PageMessage("当前音源暂无榜单") }
                else -> {
                    item("spotlight-heading") {
                        Text("热门榜单", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    }
                    itemsIndexed(featuredBoards.chunked(featureColumns), key = { index, _ -> "featured_${state.source}_$index" }) { index, boards ->
                        PageGridRow(boards, featureColumns, Modifier.widthIn(max = 620.dp).padding(top = 8.dp, bottom = 10.dp)) { board, cellModifier ->
                            FeaturedChartCard(
                                item = board,
                                gradient = EditorialGradients[(index * featureColumns + boards.indexOf(board)) % EditorialGradients.size],
                                onClick = { vm.openLeaderboard(board) },
                                modifier = cellModifier,
                            )
                        }
                    }
                    if (restBoards.isNotEmpty()) {
                        item("other-charts-heading") {
                            Text(
                                "更多榜单",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                            )
                        }
                        itemsIndexed(restBoards.chunked(columns), key = { index, _ -> "charts_${state.source}_$index" }) { index, boards ->
                            PageGridRow(boards, columns, Modifier.padding(vertical = 7.dp)) { board, cellModifier ->
                                val gradientIndex = (index * columns + boards.indexOf(board) + 2) % EditorialGradients.size
                                ChartGridCard(board, EditorialGradients[gradientIndex], { vm.openLeaderboard(board) }, cellModifier)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedChartCard(
    item: LeaderboardItem,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .shadow(14.dp, shape, ambientColor = gradient.first().copy(alpha = 0.25f), spotColor = gradient.first().copy(alpha = 0.35f))
            .clip(shape)
            .background(Brush.linearGradient(gradient))
            .liquidCardPress(shape = shape, onClick = onClick),
    ) {
        if (!item.coverImg.isNullOrBlank()) {
            CoverArt(
                url = item.coverImg,
                contentDescription = item.name,
                cornerRadius = 0.dp,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to gradient.first().copy(alpha = 0.12f),
                            0.55f to Color.Black.copy(alpha = 0.22f),
                            1f to Color.Black.copy(alpha = 0.80f),
                        )
                    ),
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.AutoGraph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(56.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 20.sp,
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ChartGridCard(
    item: LeaderboardItem,
    fallbackGradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier.liquidCardPress(shape = shape, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(Brush.linearGradient(fallbackGradient))
                .border(0.6.dp, colors.glassBorder, shape),
        ) {
            if (!item.coverImg.isNullOrBlank()) {
                CoverArt(
                    url = item.coverImg,
                    contentDescription = item.name,
                    cornerRadius = 0.dp,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.AutoGraph,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.64f),
                    modifier = Modifier.align(Alignment.Center).size(50.dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.36f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(Modifier.height(7.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}
