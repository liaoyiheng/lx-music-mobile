package cn.toside.music.liquid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.ui.adaptive.LiquidWidthSizeClass
import cn.toside.music.liquid.ui.adaptive.LiquidWindowClass
import cn.toside.music.liquid.ui.adaptive.LocalLiquidWindowClass
import cn.toside.music.liquid.ui.adaptive.LocalTopBarClearance
import cn.toside.music.liquid.ui.components.liquidPress
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

@Composable
internal fun StandardPage(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = 1160.dp,
    content: @Composable (availableWidth: Dp, window: LiquidWindowClass) -> Unit,
) {
    val window = LocalLiquidWindowClass.current
    val topClearance = LocalTopBarClearance.current
    val colors = LocalLiquidColors.current
    Box(modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(
            Modifier
                .padding(horizontal = window.horizontalMargin)
                .widthIn(max = maxContentWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(top = topClearance),
        ) {
            content(maxWidth, window)
        }
    }
}

internal val PageGridGap = 14.dp

internal fun LiquidWindowClass.pageColumns(
    availableWidth: Dp,
    minCell: Dp = if (widthSizeClass == LiquidWidthSizeClass.Compact) 146.dp else gridMinCellSize,
    maxColumns: Int = 6,
): Int = ((availableWidth + PageGridGap) / (minCell + PageGridGap)).toInt()
    .coerceIn(1, maxColumns)

@Composable
internal fun <T> PageGridRow(
    entries: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    cell: @Composable (T, Modifier) -> Unit,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PageGridGap)) {
        entries.forEach { entry -> cell(entry, Modifier.weight(1f)) }
        repeat(columns - entries.size) { Spacer(Modifier.weight(1f)) }
    }
}

@Composable
internal fun PageHeading(title: String, actions: @Composable (() -> Unit)? = null) {
    val colors = LocalLiquidColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.displaySmall,
            color = colors.textPrimary,
        )
        actions?.invoke()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PageSourcePicker(
    sources: List<String>,
    selectedSource: String,
    wraps: Boolean,
    onSelect: (String) -> Unit,
) {
    if (wraps) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sources.forEach { source ->
                PageChoiceChip(MusicSources.displayName(source), source == selectedSource) { onSelect(source) }
            }
        }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sources, key = { it }) { source ->
                PageChoiceChip(MusicSources.displayName(source), source == selectedSource) { onSelect(source) }
            }
        }
    }
}

@Composable
internal fun PageChoiceChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalLiquidColors.current
    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clip(CircleShape)
            .background(if (active) colors.accent else colors.chipBackground)
            .semantics { selected = active; role = Role.Button }
            .liquidPress(shape = CircleShape, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (active) Color.White else colors.textPrimary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
internal fun PageMessage(text: String, retry: (() -> Unit)? = null) {
    val colors = LocalLiquidColors.current
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text, color = colors.textSecondary, textAlign = TextAlign.Center)
        if (retry != null) TextButton(onClick = retry) { Text("重新加载", color = colors.accent) }
    }
}

@Composable
internal fun PageSpinner(compact: Boolean = false) {
    val colors = LocalLiquidColors.current
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = if (compact) 12.dp else 52.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.5.dp)
    }
}

@Composable
internal fun PageLoadMore(requestKey: String, loading: Boolean, onLoad: () -> Unit) {
    var attemptedKey by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(requestKey) {
        if (!loading && attemptedKey != requestKey) {
            attemptedKey = requestKey
            onLoad()
        }
    }
    if (loading) {
        PageSpinner(compact = true)
    } else {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(onClick = {
                attemptedKey = requestKey
                onLoad()
            }) { Text("加载更多", color = LocalLiquidColors.current.accent) }
        }
    }
}
