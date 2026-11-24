package cn.toside.music.liquid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.bouncyClick
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

    val featuredBoards = state.boards.take(2)
    val restBoards = if (state.boards.size > 2) state.boards.drop(2) else emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(bottom = bottomClearanceDp.dp),
    ) {
        // Editorial Large Title Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp, bottom = 10.dp),
            ) {
                Text(
                    text = "TOP CHARTS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp,
                    ),
                    color = colors.accent,
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "排行榜",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.8).sp,
                        ),
                        color = colors.textPrimary,
                    )
                    LiquidGlassSurface(
                        shape = CircleShape,
                        useBlur = false,
                        modifier = Modifier
                            .size(38.dp)
                            .bouncyClick { vm.loadCharts(state.source) },
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "刷新榜单",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                }
            }
        }

        // Source Pills
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                items(MusicSources.ONLINE, key = { it }) { src ->
                    val selected = state.source == src
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) colors.accent else colors.chipBackground)
                            .border(
                                width = 0.7.dp,
                                color = if (selected) Color.Transparent else colors.glassBorder,
                                shape = CircleShape,
                            )
                            .bouncyClick { vm.selectChartsSource(src) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = MusicSources.displayName(src),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (selected) Color.White else colors.textPrimary,
                        )
                    }
                }
            }
        }

        if (state.loading && state.boards.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.accent, strokeWidth = 2.6.dp)
                }
            }
        } else if (state.error != null && state.boards.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.error ?: "排行榜加载失败",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.accent)
                            .bouncyClick { vm.loadCharts(state.source) }
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                    ) {
                        Text("重新加载", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            // Editorial Featured Top 2 Spotlight Cards
            if (featuredBoards.isNotEmpty()) {
                item {
                    Text(
                        text = "编辑聚焦 · 官方热榜",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        featuredBoards.forEachIndexed { idx, board ->
                            FeaturedChartCard(
                                item = board,
                                gradient = EditorialGradients[idx % EditorialGradients.size],
                                onClick = { vm.openLeaderboard(board) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (featuredBoards.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                }
            }

            // Remaining Charts Grid (2 columns)
            if (restBoards.isNotEmpty()) {
                item {
                    Text(
                        text = "${MusicSources.displayName(state.source)}全部榜单",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                }

                val rows = restBoards.chunked(2)
                items(rows.size, key = { "chart_row_${state.source}_$it" }) { rowIdx ->
                    val pair = rows[rowIdx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        pair.forEachIndexed { colIdx, board ->
                            val gradIdx = (rowIdx * 2 + colIdx + 2) % EditorialGradients.size
                            ChartGridCard(
                                item = board,
                                fallbackGradient = EditorialGradients[gradIdx],
                                onClick = { vm.openLeaderboard(board) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(Modifier.weight(1f))
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
            .aspectRatio(0.86f)
            .shadow(14.dp, shape, ambientColor = gradient.first().copy(alpha = 0.25f), spotColor = gradient.first().copy(alpha = 0.35f))
            .clip(shape)
            .background(Brush.linearGradient(gradient))
            .bouncyClick(onClick = onClick),
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

        // Top editorial badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
                .padding(horizontal = 9.dp, vertical = 4.dp),
        ) {
            Text(
                text = MusicSources.displayName(item.source),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }

        // Bottom title + play glyph
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
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "每日实时更新",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.82f),
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
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "播放",
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
        modifier = modifier.bouncyClick(onClick = onClick),
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = MusicSources.shortBadge(item.source),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        ),
                        color = Color.White.copy(alpha = 0.82f),
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp,
                        ),
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
                    imageVector = Icons.Rounded.PlayArrow,
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
        Text(
            text = "${MusicSources.displayName(item.source)}官方榜",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}
