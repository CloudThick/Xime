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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.keyboard.ToolbarButton
import com.kingzcheung.xime.keyboard.ToolbarButtonItem
import com.kingzcheung.xime.ui.keyboard.ToolbarButtonIcon

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
    val itemById = remember(allButtons) { allButtons.associateBy { it.id } }
    var enabledIds by remember(toolbarButtons) { mutableStateOf(toolbarButtons.toSet()) }

    fun toggleButton(item: ToolbarButtonItem) {
        val nextEnabled = if (item.id in enabledIds) enabledIds - item.id else enabledIds + item.id
        enabledIds = nextEnabled
        val newList = toolbarButtons.toMutableList()
        if (item.id in newList) newList.remove(item.id) else newList.add(item.id)
        onUpdateToolbarButtons?.invoke(newList)
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val iconButtonContainer = androidx.compose.ui.graphics.lerp(
        keyBgColor,
        accentColor,
        0.25f
    )
    val sidePad = if (isLandscape) 50.dp else 8.dp
    val barIconSize = 32.dp
    val barGlyphSize = 20.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = sidePad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(barIconSize)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "确定",
                    tint = accentColor,
                    modifier = Modifier.size(barGlyphSize)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val previewButtons = toolbarButtons.mapNotNull { itemById[it] }
                previewButtons.forEach { button ->
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(barIconSize)
                            .clip(CircleShape)
                            .background(iconButtonContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        ToolbarButtonIcon(
                            item = button,
                            tint = keyTextColor.copy(0.6f),
                            modifier = Modifier.size(barGlyphSize),
                        )
                    }
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = sidePad, vertical = 4.dp),
        ) {
            val columns = when {
                maxWidth >= 900.dp -> 8
                maxWidth >= 700.dp -> 6
                maxWidth >= 500.dp -> 5
                else -> 4
            }
            val rows = 2
            val itemsPerPage = columns * rows
            val pages = remember(allButtons, itemsPerPage) {
                if (allButtons.isEmpty()) {
                    listOf(emptyList())
                } else {
                    allButtons.chunked(itemsPerPage)
                }
            }
            val pagerState = rememberPagerState(pageCount = { pages.size })
            val iconSize = if (maxWidth >= 500.dp) 52.dp else 48.dp
            val glyphSize = if (maxWidth >= 500.dp) 24.dp else 22.dp
            val labelHeight = 18.dp
            val cellHeight = iconSize + 6.dp + labelHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(keyBgColor)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    val pageItems = pages[page]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Top,
                    ) {
                        repeat(rows) { rowIndex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight),
                                verticalAlignment = Alignment.Top,
                            ) {
                                repeat(columns) { colIndex ->
                                    val item = pageItems.getOrNull(rowIndex * columns + colIndex)
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.TopCenter,
                                    ) {
                                        if (item != null) {
                                            val isEnabled = item.id in enabledIds
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(iconSize)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isEnabled) accentColor.copy(alpha = 0.2f)
                                                            else keyTextColor.copy(alpha = 0.06f)
                                                        )
                                                        .clickable { toggleButton(item) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    ToolbarButtonIcon(
                                                        item = item,
                                                        tint = if (isEnabled) accentColor
                                                        else keyTextColor.copy(alpha = 0.75f),
                                                        modifier = Modifier.size(glyphSize),
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = item.label,
                                                    fontSize = 12.sp,
                                                    color = keyTextColor.copy(alpha = 0.8f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center,
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
                    Spacer(modifier = Modifier.height(8.dp))
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
                                        if (index == pagerState.currentPage) keyTextColor
                                        else keyTextColor.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else bottomPaddingDp.dp))
    }
}
