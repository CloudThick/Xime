package com.kingzcheung.xime.ui.menubar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.keyboard.ToolbarButton
import com.kingzcheung.xime.keyboard.ToolbarButtonItem
import com.kingzcheung.xime.ui.keyboard.ToolbarButtonIcon

internal fun toolbarCustomizeColumnCount(
    availableWidthDp: Float,
    isLandscape: Boolean,
): Int = when {
    isLandscape && availableWidthDp >= 840f -> 8
    isLandscape && availableWidthDp >= 600f -> 6
    availableWidthDp >= 600f -> 5
    availableWidthDp >= 420f -> 5
    else -> 4
}

@Composable
fun ToolbarCustomizeView(
    toolbarButtons: List<String>,
    pluginButtons: List<ToolbarButtonItem.Plugin> = emptyList(),
    keyTextColor: Color,
    backgroundColor: Color,
    accentColor: Color,
    keyBgColor: Color,
    onUpdateToolbarButtons: ((List<String>) -> Unit)?,
    onDismiss: () -> Unit,
    bottomPaddingDp: Int = 0,
    modifier: Modifier = Modifier
) {
    val builtinButtons = ToolbarButton.entries.filter { button ->
        if (button == ToolbarButton.HANDWRITING_LOOKUP) {
            com.kingzcheung.xime.handwriting.HandwritingEngine.hasModel(LocalContext.current)
        } else true
    }.map { ToolbarButtonItem.Builtin(it) }
    val allButtons = builtinButtons + pluginButtons
    var selectedIds by remember(toolbarButtons) { mutableStateOf(toolbarButtons) }
    val enabledIds = selectedIds.toSet()

    fun toggleButton(item: ToolbarButtonItem) {
        val next = if (item.id in enabledIds) {
            selectedIds.filterNot { it == item.id }
        } else {
            selectedIds + item.id
        }
        selectedIds = next
        onUpdateToolbarButtons?.invoke(next)
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val horizontalPadding = if (isLandscape) 24.dp else 12.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 固定左右槽位，中间提示不会再被已选按钮数量挤歪或挤出屏幕。
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = horizontalPadding),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "完成",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "点击图标添加或移除",
                color = keyTextColor.copy(alpha = 0.68f),
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.align(Alignment.Center),
            )

            Text(
                text = "已选 ${selectedIds.size}",
                color = keyTextColor.copy(alpha = 0.56f),
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = horizontalPadding, vertical = 4.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 960.dp),
            ) {
                val columns = toolbarCustomizeColumnCount(maxWidth.value, isLandscape)
                val rows = 2
                val itemsPerPage = columns * rows
                val pages = remember(allButtons, itemsPerPage) {
                    allButtons.chunked(itemsPerPage).ifEmpty { listOf(emptyList()) }
                }
                val pagerState = rememberPagerState(pageCount = { pages.size })
                val iconSize = when {
                    columns >= 8 -> 44.dp
                    columns >= 5 -> 48.dp
                    else -> 46.dp
                }
                val glyphSize = if (columns >= 8) 21.dp else 22.dp
                val cellHeight = iconSize + 30.dp
                val gridHeight = cellHeight * rows

                LaunchedEffect(itemsPerPage) {
                    if (pagerState.currentPage != 0) pagerState.scrollToPage(0)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(keyBgColor)
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight)
                            .clipToBounds(),
                    ) { page ->
                        val pageItems = pages[page]
                        Column(modifier = Modifier.fillMaxWidth()) {
                            repeat(rows) { rowIndex ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(cellHeight),
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    repeat(columns) { columnIndex ->
                                        val item = pageItems.getOrNull(rowIndex * columns + columnIndex)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            contentAlignment = Alignment.TopCenter,
                                        ) {
                                            if (item != null) {
                                                val isEnabled = item.id in enabledIds
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { toggleButton(item) },
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(iconSize)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isEnabled) accentColor.copy(alpha = 0.18f)
                                                                else keyTextColor.copy(alpha = 0.06f)
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        ToolbarButtonIcon(
                                                            item = item,
                                                            tint = if (isEnabled) accentColor
                                                            else keyTextColor.copy(alpha = 0.72f),
                                                            modifier = Modifier.size(glyphSize),
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(5.dp))
                                                    Text(
                                                        text = item.label,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = keyTextColor.copy(alpha = 0.76f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.fillMaxWidth(),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (pages.size > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(pages.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == pagerState.currentPage) keyTextColor.copy(alpha = 0.72f)
                                            else keyTextColor.copy(alpha = 0.22f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else bottomPaddingDp.dp))
    }
}
