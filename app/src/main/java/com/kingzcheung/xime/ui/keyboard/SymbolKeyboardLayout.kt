package com.kingzcheung.xime.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.data.RecentUsageStore
import com.kingzcheung.xime.data.SymbolCategory
import com.kingzcheung.xime.data.SymbolData
import kotlinx.coroutines.launch

@Composable
fun SymbolKeyboardLayout(
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    accentColor: Color,
    keyBgColor: Color,
    bottomPaddingDp: Int = 0,
    useSplitLandscape: Boolean = true,
    isFloatingMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // 最近使用（LRU）：作为第一个分类页，点击符号时置顶记录
    var recentSymbols by remember {
        mutableStateOf(RecentUsageStore.get(context, RecentUsageStore.KEY_RECENT_SYMBOLS))
    }
    val displayCategories = remember(recentSymbols) {
        listOf(SymbolCategory(name = "最近使用", id = "recentSymbols", symbols = recentSymbols)) +
            SymbolData.categories
    }
    // 图标按钮容器色：surface 与 primary 的混合色调（带种子色但不过于强烈）
    val iconButtonContainer = androidx.compose.ui.graphics.lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.primary,
        0.15f
    )
    val configuration = LocalConfiguration.current
    val isLandscape = !isFloatingMode &&
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= TABLET_SMALLEST_WIDTH_DP
    val split = isLandscape && useSplitLandscape
    // 平板或横屏按字母键尺寸排；手机竖屏仍用 8 列方键。
    val useLetterSizedGrid = isTablet || isLandscape
    val panelCorner = if (isLandscape) PANEL_LANDSCAPE_CORNER_DP.dp else PANEL_PORTRAIT_CORNER_DP.dp
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { displayCategories.size }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        // 导航区：返回按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (useLetterSizedGrid) 40.dp else 50.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconButtonContainer)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "返回",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 内容区：符号网格 + HorizontalPager
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    horizontal = when {
                        split -> 50.dp
                        useLetterSizedGrid -> 8.dp
                        else -> 4.dp
                    }
                )
                .padding(bottom = 4.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            CompositionLocalProvider(LocalKeyCornerRadius provides panelCorner) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        if (isLandscape && !split) {
                            Modifier.widthIn(max = QWERTY_FULL_LANDSCAPE_MAX_WIDTH_DP.dp)
                        } else Modifier
                    )
                    .fillMaxWidth()
            ) { page ->
                val category = displayCategories[page]
                val columns = if (useLetterSizedGrid) 10 else 8
                val rowSpacing = if (isLandscape) {
                    PANEL_LANDSCAPE_GRID_GAP_DP.dp
                } else if (useLetterSizedGrid) {
                    PANEL_PORTRAIT_GRID_GAP_DP.dp
                } else {
                    2.dp
                }
                val keyRowHeight: Dp? = if (useLetterSizedGrid) {
                    if (isLandscape) PANEL_LANDSCAPE_KEY_HEIGHT_DP.dp
                    else PANEL_PORTRAIT_KEY_HEIGHT_DP.dp
                } else null

                if (category.symbols.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无最近使用",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    SymbolCategoryGrid(
                        symbols = category.symbols,
                        columns = columns,
                        rowHeight = keyRowHeight,
                        rowSpacing = rowSpacing,
                        split = split,
                        square = !useLetterSizedGrid,
                        textColor = textColor,
                        keyBgColor = keyBgColor,
                        onSelect = { symbol ->
                            recentSymbols = RecentUsageStore.record(
                                context, RecentUsageStore.KEY_RECENT_SYMBOLS, symbol
                            )
                            onSelect(symbol)
                        },
                    )
                }
            }
            }
        }

        // 底部：分类 Tab + 删除按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = if (isLandscape) 8.dp else 4.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                displayCategories.forEachIndexed { index, category ->
                    SymbolCategoryTab(
                        name = category.name,
                        isSelected = index == pagerState.currentPage,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        backgroundColor = backgroundColor,
                        textColor = textColor,
                        selectedBackgroundColor = accentColor
                    )
                }
            }

            KeyButton(
                text = "删除",
                onClick = { onSelect("delete") },
                backgroundColor = backgroundColor,
                textColor = textColor,
                modifier = Modifier.width(48.dp),
                fontSize = 12.sp
            )
        }

        // 底部留空（至少覆盖导航栏 inset 与键盘底部内边距）
        Spacer(modifier = Modifier.height(bottomPaddingDp.dp))
    }
}

@Composable
private fun SymbolCategoryGrid(
    symbols: List<String>,
    columns: Int,
    rowHeight: Dp?,
    rowSpacing: Dp,
    split: Boolean,
    square: Boolean,
    textColor: Color,
    keyBgColor: Color,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        if (split) {
            symbols.chunked(10).forEach { rowSymbols ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (rowHeight != null) Modifier.height(rowHeight) else Modifier),
                ) {
                    SymbolKeyRow(
                        keys = rowSymbols.take(5),
                        slots = 5,
                        square = square,
                        textColor = textColor,
                        keyBgColor = keyBgColor,
                        rowSpacing = rowSpacing,
                        modifier = Modifier.weight(0.42f).fillMaxHeight(),
                        onSelect = onSelect,
                    )
                    Spacer(modifier = Modifier.weight(0.16f))
                    SymbolKeyRow(
                        keys = rowSymbols.drop(5),
                        slots = 5,
                        square = square,
                        textColor = textColor,
                        keyBgColor = keyBgColor,
                        rowSpacing = rowSpacing,
                        modifier = Modifier.weight(0.42f).fillMaxHeight(),
                        onSelect = onSelect,
                    )
                }
            }
        } else {
            symbols.chunked(columns).forEach { rowSymbols ->
                SymbolKeyRow(
                    keys = rowSymbols,
                    slots = columns,
                    square = square,
                    textColor = textColor,
                    keyBgColor = keyBgColor,
                    rowSpacing = rowSpacing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (rowHeight != null) Modifier.height(rowHeight) else Modifier),
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun SymbolKeyRow(
    keys: List<String>,
    slots: Int,
    square: Boolean,
    textColor: Color,
    keyBgColor: Color,
    rowSpacing: Dp,
    modifier: Modifier,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        keys.forEach { symbol ->
            SymbolButton(
                symbol = symbol,
                onClick = { onSelect(symbol) },
                modifier = Modifier.weight(1f),
                textColor = textColor,
                backgroundColor = keyBgColor,
                square = square,
            )
        }
        repeat((slots - keys.size).coerceAtLeast(0)) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SymbolButton(
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    backgroundColor: Color,
    square: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val corner = LocalKeyCornerRadius.current
    Box(
        modifier = modifier
            .then(if (square) Modifier.aspectRatio(1f) else Modifier.fillMaxHeight())
            .clip(RoundedCornerShape(corner))
            .background(
                if (isPressed) androidx.compose.ui.graphics.lerp(backgroundColor, Color.Black, 0.2f)
                else backgroundColor
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = if (square) 16.sp else 18.sp,
            textAlign = TextAlign.Center,
            color = textColor,
        )
    }
}

@Composable
private fun SymbolCategoryTab(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    selectedBackgroundColor: Color = textColor.copy(alpha = 0.15f),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) selectedBackgroundColor
                else backgroundColor
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (isSelected) textColor else textColor.copy(alpha = 0.5f)
        )
    }
}
