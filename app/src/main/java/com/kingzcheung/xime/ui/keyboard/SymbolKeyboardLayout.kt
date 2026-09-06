package com.kingzcheung.xime.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.data.RecentUsageStore
import com.kingzcheung.xime.data.SymbolCategory
import com.kingzcheung.xime.data.SymbolData
import kotlinx.coroutines.launch

/** 完整/竖屏第三行：123 + 7 个符号 + 删除，与 QWERTY zxcvbnm 同宽。 */
private const val FULL_ROW3_SYMBOLS = 7

/** 分离布局第三行左右各 4 个符号，与 QWERTY z/x/c/v | v/b/n/m 同宽。 */
private const val SPLIT_ROW3_SYMBOLS = 4

/** 「最近使用」不足一屏时用来铺满的常用符号，排在真实最近记录后面。 */
private val DEFAULT_RECENT_SYMBOLS = listOf(
    "。", "，", "、", "；", "：", "？", "！", ".",
    ",", "?", "!", "\"", "'", "（", "）", "【",
    "】", "《", "》", "@", "#", "%", "&", "*",
    "+", "-", "=", "/",
)

private fun padRecentSymbols(recent: List<String>): List<String> {
    val minCount = 10 + 10 + maxOf(FULL_ROW3_SYMBOLS, SPLIT_ROW3_SYMBOLS * 2)
    if (recent.size >= minCount) return recent
    val extras = DEFAULT_RECENT_SYMBOLS.filter { it !in recent }
    return (recent + extras).distinct()
}

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
    modifier: Modifier = Modifier,
    specialKeyBackgroundColor: Color = accentColor,
    specialKeyTextColor: Color = textColor,
    shadowEnabled: Boolean = true,
    shadowElevation: Dp = 1.dp,
    shadowShapeRadius: Dp = 8.dp,
    onGoToCommon: () -> Unit = onBack,
) {
    val context = LocalContext.current
    var recentSymbols by remember {
        mutableStateOf(RecentUsageStore.get(context, RecentUsageStore.KEY_RECENT_SYMBOLS))
    }
    val displayCategories = remember(recentSymbols) {
        listOf(
            SymbolCategory(
                name = "最近使用",
                id = "recentSymbols",
                symbols = padRecentSymbols(recentSymbols),
            )
        ) + SymbolData.categories
    }
    val configuration = LocalConfiguration.current
    val isLandscape = !isFloatingMode &&
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val split = isLandscape && useSplitLandscape
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { displayCategories.size }
    )
    val shiftWide = if (isLandscape) 1.5f else 1.4f
    val suppressCursorMove = LocalSuppressCursorMove.current

    fun commitSymbol(symbol: String) {
        recentSymbols = RecentUsageStore.record(
            context, RecentUsageStore.KEY_RECENT_SYMBOLS, symbol
        )
        onSelect(symbol)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        ProvidePanelKeyGeometry(
            isLandscape = isLandscape,
            isFloatingMode = isFloatingMode,
            configuredCornerRadiusDp = 8f,
            configuredShadowElevationDp = shadowElevation.value,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        if (isLandscape) {
                            PaddingValues(vertical = 2.dp, horizontal = 50.dp)
                        } else {
                            PaddingValues(start = 4.dp, end = 4.dp, bottom = 8.dp)
                        }
                    ),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f),
                ) { page ->
                    val category = displayCategories[page]
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val rowHeight = maxHeight / 3
                        if (split) {
                            SymbolSplitKeyArea(
                                symbols = category.symbols,
                                rowHeight = rowHeight,
                                keyBgColor = keyBgColor,
                                textColor = textColor,
                                specialKeyBackgroundColor = specialKeyBackgroundColor,
                                specialKeyTextColor = specialKeyTextColor,
                                shadowEnabled = shadowEnabled,
                                shadowElevation = shadowElevation,
                                shadowShapeRadius = shadowShapeRadius,
                                suppressCursorMove = suppressCursorMove,
                                onCommit = ::commitSymbol,
                                onGoToCommon = onGoToCommon,
                                onDelete = { onSelect("delete") },
                            )
                        } else {
                            SymbolFullKeyArea(
                                symbols = category.symbols,
                                rowHeight = rowHeight,
                                shiftWide = shiftWide,
                                keyBgColor = keyBgColor,
                                textColor = textColor,
                                specialKeyBackgroundColor = specialKeyBackgroundColor,
                                specialKeyTextColor = specialKeyTextColor,
                                shadowEnabled = shadowEnabled,
                                shadowElevation = shadowElevation,
                                shadowShapeRadius = shadowShapeRadius,
                                suppressCursorMove = suppressCursorMove,
                                onCommit = ::commitSymbol,
                                onGoToCommon = onGoToCommon,
                                onDelete = { onSelect("delete") },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (split) {
                        val backWeight = 0.42f * 1.5f / 5.5f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(start = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            KeyButton(
                                text = "返回",
                                onClick = onBack,
                                backgroundColor = specialKeyBackgroundColor,
                                textColor = specialKeyTextColor,
                                modifier = Modifier.weight(backWeight),
                                shadowEnabled = shadowEnabled,
                                shadowElevation = shadowElevation,
                                shadowShapeRadius = shadowShapeRadius,
                                fontSize = FUNCTION_KEY_FONT_SP.sp,
                            )
                            SymbolCategoryTabRow(
                                categories = displayCategories,
                                currentPage = pagerState.currentPage,
                                onSelectPage = { index ->
                                    scope.launch { pagerState.scrollToPage(index) }
                                },
                                backgroundColor = backgroundColor,
                                textColor = textColor,
                                selectedBackgroundColor = accentColor,
                                modifier = Modifier
                                    .weight(1f - backWeight)
                                    .fillMaxHeight(),
                            )
                        }
                    } else {
                        KeyButton(
                            text = "返回",
                            onClick = onBack,
                            backgroundColor = specialKeyBackgroundColor,
                            textColor = specialKeyTextColor,
                            modifier = Modifier.weight(shiftWide),
                            shadowEnabled = shadowEnabled,
                            shadowElevation = shadowElevation,
                            shadowShapeRadius = shadowShapeRadius,
                            fontSize = FUNCTION_KEY_FONT_SP.sp,
                        )
                        SymbolCategoryTabRow(
                            categories = displayCategories,
                            currentPage = pagerState.currentPage,
                            onSelectPage = { index ->
                                scope.launch { pagerState.scrollToPage(index) }
                            },
                            backgroundColor = backgroundColor,
                            textColor = textColor,
                            selectedBackgroundColor = accentColor,
                            modifier = Modifier
                                .weight(8.5f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }

        if (bottomPaddingDp > 0) {
            Spacer(modifier = Modifier.height(bottomPaddingDp.dp))
        }
    }
}

@Composable
private fun SymbolFullKeyArea(
    symbols: List<String>,
    rowHeight: Dp,
    shiftWide: Float,
    keyBgColor: Color,
    textColor: Color,
    specialKeyBackgroundColor: Color,
    specialKeyTextColor: Color,
    shadowEnabled: Boolean,
    shadowElevation: Dp,
    shadowShapeRadius: Dp,
    suppressCursorMove: androidx.compose.runtime.MutableState<Boolean>,
    onCommit: (String) -> Unit,
    onGoToCommon: () -> Unit,
    onDelete: () -> Unit,
) {
    val row1 = symbols.take(10)
    val row2 = symbols.drop(10).take(10)
    val row3 = symbols.drop(20).take(FULL_ROW3_SYMBOLS)
    val overflow = symbols.drop(20 + FULL_ROW3_SYMBOLS)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        SymbolPlainKeyRow(
            keys = row1,
            slots = 10,
            keyBgColor = keyBgColor,
            textColor = textColor,
            shadowEnabled = shadowEnabled,
            shadowElevation = shadowElevation,
            shadowShapeRadius = shadowShapeRadius,
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
            onCommit = onCommit,
        )
        SymbolPlainKeyRow(
            keys = row2,
            slots = 10,
            keyBgColor = keyBgColor,
            textColor = textColor,
            shadowEnabled = shadowEnabled,
            shadowElevation = shadowElevation,
            shadowShapeRadius = shadowShapeRadius,
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
            onCommit = onCommit,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
        ) {
            KeyButton(
                text = "123",
                onClick = onGoToCommon,
                backgroundColor = specialKeyBackgroundColor,
                textColor = specialKeyTextColor,
                modifier = Modifier.weight(shiftWide),
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                fontSize = FUNCTION_KEY_FONT_SP.sp,
            )
            SymbolPlainKeyRow(
                keys = row3,
                slots = FULL_ROW3_SYMBOLS,
                keyBgColor = keyBgColor,
                textColor = textColor,
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                modifier = Modifier
                    .weight(FULL_ROW3_SYMBOLS.toFloat())
                    .fillMaxHeight(),
                onCommit = onCommit,
            )
            SymbolDeleteKey(
                specialKeyBackgroundColor = specialKeyBackgroundColor,
                specialKeyTextColor = specialKeyTextColor,
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                showClearLabel = true,
                suppressCursorMove = suppressCursorMove,
                modifier = Modifier.weight(shiftWide),
                onDelete = onDelete,
            )
        }
        overflow.chunked(10).forEach { extra ->
            SymbolPlainKeyRow(
                keys = extra,
                slots = 10,
                keyBgColor = keyBgColor,
                textColor = textColor,
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                onCommit = onCommit,
            )
        }
    }
}

@Composable
private fun SymbolSplitKeyArea(
    symbols: List<String>,
    rowHeight: Dp,
    keyBgColor: Color,
    textColor: Color,
    specialKeyBackgroundColor: Color,
    specialKeyTextColor: Color,
    shadowEnabled: Boolean,
    shadowElevation: Dp,
    shadowShapeRadius: Dp,
    suppressCursorMove: androidx.compose.runtime.MutableState<Boolean>,
    onCommit: (String) -> Unit,
    onGoToCommon: () -> Unit,
    onDelete: () -> Unit,
) {
    val splitHalf = 0.5f
    val splitWide = 1.5f
    val row1 = symbols.take(10)
    val row2 = symbols.drop(10).take(10)
    val row3 = symbols.drop(20).take(SPLIT_ROW3_SYMBOLS * 2)
    val overflow = symbols.drop(20 + SPLIT_ROW3_SYMBOLS * 2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // 第一行与 QWERTY 的 qwert/yuiop 一样靠外，左缘与第三行 123、底栏返回对齐。
        SymbolSplitPlainRow(
            leftKeys = row1.take(5),
            rightKeys = row1.drop(5),
            indentStart = false,
            rowHeight = rowHeight,
            splitHalf = splitHalf,
            keyBgColor = keyBgColor,
            textColor = textColor,
            shadowEnabled = shadowEnabled,
            shadowElevation = shadowElevation,
            shadowShapeRadius = shadowShapeRadius,
            onCommit = onCommit,
        )
        // 第二行与 QWERTY 的 asdfg/hjkl 一样错开半键。
        SymbolSplitPlainRow(
            leftKeys = row2.take(5),
            rightKeys = row2.drop(5),
            indentStart = true,
            rowHeight = rowHeight,
            splitHalf = splitHalf,
            keyBgColor = keyBgColor,
            textColor = textColor,
            shadowEnabled = shadowEnabled,
            shadowElevation = shadowElevation,
            shadowShapeRadius = shadowShapeRadius,
            onCommit = onCommit,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
        ) {
            Row(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
                    .padding(start = 4.dp),
            ) {
                KeyButton(
                    text = "123",
                    onClick = onGoToCommon,
                    backgroundColor = specialKeyBackgroundColor,
                    textColor = specialKeyTextColor,
                    modifier = Modifier.weight(splitWide),
                    shadowEnabled = shadowEnabled,
                    shadowElevation = shadowElevation,
                    shadowShapeRadius = shadowShapeRadius,
                    fontSize = FUNCTION_KEY_FONT_SP.sp,
                )
                SymbolPlainKeyRow(
                    keys = row3.take(SPLIT_ROW3_SYMBOLS),
                    slots = SPLIT_ROW3_SYMBOLS,
                    keyBgColor = keyBgColor,
                    textColor = textColor,
                    shadowEnabled = shadowEnabled,
                    shadowElevation = shadowElevation,
                    shadowShapeRadius = shadowShapeRadius,
                    modifier = Modifier
                        .weight(SPLIT_ROW3_SYMBOLS.toFloat())
                        .fillMaxHeight(),
                    onCommit = onCommit,
                )
            }
            Spacer(modifier = Modifier.weight(0.16f))
            Row(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
                    .padding(end = 4.dp),
            ) {
                SymbolPlainKeyRow(
                    keys = row3.drop(SPLIT_ROW3_SYMBOLS),
                    slots = SPLIT_ROW3_SYMBOLS,
                    keyBgColor = keyBgColor,
                    textColor = textColor,
                    shadowEnabled = shadowEnabled,
                    shadowElevation = shadowElevation,
                    shadowShapeRadius = shadowShapeRadius,
                    modifier = Modifier
                        .weight(SPLIT_ROW3_SYMBOLS.toFloat())
                        .fillMaxHeight(),
                    onCommit = onCommit,
                )
                SymbolDeleteKey(
                    specialKeyBackgroundColor = specialKeyBackgroundColor,
                    specialKeyTextColor = specialKeyTextColor,
                    shadowEnabled = shadowEnabled,
                    shadowElevation = shadowElevation,
                    shadowShapeRadius = shadowShapeRadius,
                    showClearLabel = false,
                    suppressCursorMove = suppressCursorMove,
                    modifier = Modifier.weight(splitWide),
                    onDelete = onDelete,
                )
            }
        }
        overflow.chunked(10).forEach { extra ->
            SymbolSplitPlainRow(
                leftKeys = extra.take(5),
                rightKeys = extra.drop(5),
                indentStart = false,
                rowHeight = rowHeight,
                splitHalf = splitHalf,
                keyBgColor = keyBgColor,
                textColor = textColor,
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                onCommit = onCommit,
            )
        }
    }
}

@Composable
private fun SymbolSplitPlainRow(
    leftKeys: List<String>,
    rightKeys: List<String>,
    indentStart: Boolean,
    rowHeight: Dp,
    splitHalf: Float,
    keyBgColor: Color,
    textColor: Color,
    shadowEnabled: Boolean,
    shadowElevation: Dp,
    shadowShapeRadius: Dp,
    onCommit: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight),
    ) {
        Row(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
                .padding(start = 4.dp),
        ) {
            if (indentStart) Spacer(modifier = Modifier.weight(splitHalf))
            SymbolPlainKeyRow(
                keys = leftKeys,
                slots = 5,
                keyBgColor = keyBgColor,
                textColor = textColor,
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight(),
                onCommit = onCommit,
            )
            if (!indentStart) Spacer(modifier = Modifier.weight(splitHalf))
        }
        Spacer(modifier = Modifier.weight(0.16f))
        Row(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
                .padding(end = 4.dp),
        ) {
            if (!indentStart) Spacer(modifier = Modifier.weight(splitHalf))
            SymbolPlainKeyRow(
                keys = rightKeys,
                slots = 5,
                keyBgColor = keyBgColor,
                textColor = textColor,
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight(),
                onCommit = onCommit,
            )
            if (indentStart) Spacer(modifier = Modifier.weight(splitHalf))
        }
    }
}

@Composable
private fun SymbolPlainKeyRow(
    keys: List<String>,
    slots: Int,
    keyBgColor: Color,
    textColor: Color,
    shadowEnabled: Boolean,
    shadowElevation: Dp,
    shadowShapeRadius: Dp,
    modifier: Modifier,
    onCommit: (String) -> Unit,
) {
    Row(modifier = modifier) {
        keys.forEach { symbol ->
            KeyButton(
                text = symbol,
                onClick = { onCommit(symbol) },
                backgroundColor = keyBgColor,
                textColor = textColor,
                modifier = Modifier.weight(1f),
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
            )
        }
        repeat((slots - keys.size).coerceAtLeast(0)) {
            Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun SymbolDeleteKey(
    specialKeyBackgroundColor: Color,
    specialKeyTextColor: Color,
    shadowEnabled: Boolean,
    shadowElevation: Dp,
    shadowShapeRadius: Dp,
    showClearLabel: Boolean,
    suppressCursorMove: androidx.compose.runtime.MutableState<Boolean>,
    modifier: Modifier,
    onDelete: () -> Unit,
) {
    SwipeableIconKeyButton(
        icon = rememberVectorPainter(Icons.AutoMirrored.Filled.Backspace),
        onClick = onDelete,
        backgroundColor = specialKeyBackgroundColor,
        iconColor = specialKeyTextColor,
        modifier = modifier.fillMaxHeight(),
        swipeText = if (showClearLabel) "清空" else null,
        onSwipe = if (showClearLabel) ({ onDelete() }) else null,
        onLongClick = onDelete,
        swipeUpLabel = "上滑清空",
        swipeDownLabel = "下滑撤回",
        onSwipeLeft = { suppressCursorMove.value = true; onDelete() },
        shadowEnabled = shadowEnabled,
        shadowElevation = shadowElevation,
        shadowShapeRadius = shadowShapeRadius,
    )
}

@Composable
private fun SymbolCategoryTabRow(
    categories: List<SymbolCategory>,
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    backgroundColor: Color,
    textColor: Color,
    selectedBackgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        categories.forEachIndexed { index, category ->
            SymbolCategoryTab(
                name = category.name,
                isSelected = index == currentPage,
                onClick = { onSelectPage(index) },
                backgroundColor = backgroundColor,
                textColor = textColor,
                selectedBackgroundColor = selectedBackgroundColor,
            )
        }
    }
}

@Composable
private fun SymbolCategoryTab(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    selectedBackgroundColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) selectedBackgroundColor
                else backgroundColor
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (isSelected) textColor else textColor.copy(alpha = 0.55f)
        )
    }
}
