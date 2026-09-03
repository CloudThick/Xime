package com.kingzcheung.xime.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.util.SubcharHelper

/**
 * 九宫格数字键盘布局
 * 左列：+−×÷ 合并为一枚带隔断的大键，返回在页左下角
 * 中列：123 / 456 / 789 / 符号 0 .
 * 右列：退格 / 空格 / 表情 / 确定
 */
@Composable
fun NumberKeyboardLayout(
    onKeyPress: (String) -> Unit,
    keyBackgroundColor: Color,
    keyTextColor: Color,
    specialKeyBackgroundColor: Color,
    bubbleBackgroundColor: Color = keyBackgroundColor,
    keyboardBackgroundColor: Color = Color.Transparent,
    shadowEnabled: Boolean = true,
    shadowElevation: Dp = 1.dp,
    shadowShapeRadius: Dp = 8.dp,
    keyCornerRadius: Dp = 8.dp,
    modifier: Modifier = Modifier,
    onKeyPressDown: ((String) -> Unit)? = null,
    isFloatingMode: Boolean = false,
    specialKeyTextColor: Color = Color.White,
    useSplitLandscape: Boolean = true,
    enterKeyText: String = "确定",
) {

    val configuration = LocalConfiguration.current
    val isLandscape = !isFloatingMode && configuration.screenWidthDp > configuration.screenHeightDp
    val landscapePanelSymbols = listOf(
        "。", "？", "！", "…", "“", "”",
        "：", "~", "(", ")", "、", "#",
        "@", "%", "×", "=", "·", ",",
        ".", ";", "'", "\"", "<", ">",
    )
    val swipeBubble = rememberSwipeBubbleController()
    var keyboardBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }

    val isDarkTheme = keyTextColor == Color(0xFFE8EAED)

    val bubbleData = rememberSwipeBubbleDrawData(
        swipeState = swipeBubble.state,
        keyBounds = swipeBubble.keyBounds,
        keyBackgroundColor = bubbleBackgroundColor,
        keyTextColor = keyTextColor,
        accentColor = specialKeyTextColor,
        keyWidth = if (swipeBubble.state.isSwiping || swipeBubble.state.isPressed) swipeBubble.keyBounds.width else 0f,
        keyboardWidth = keyboardBounds.width
    )

    fun processSwipeState(state: SwipeState, bounds: Rect) {
        val newState = if (state.isSwipeDown && state.swipeText != null) {
            state.copy(charInfos = SubcharHelper.parseSwipeDownText(state.swipeText))
        } else state
        swipeBubble.update(
            newState,
            Rect(
                left = bounds.left - keyboardBounds.left,
                top = bounds.top - keyboardBounds.top,
                right = bounds.right - keyboardBounds.left,
                bottom = bounds.bottom - keyboardBounds.top
            )
        )
    }

    ProvidePanelKeyGeometry(
        isLandscape = isLandscape,
        isFloatingMode = isFloatingMode,
        configuredCornerRadiusDp = keyCornerRadius.value,
        configuredShadowElevationDp = shadowElevation.value,
    ) {
    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                keyboardBounds = coordinates.boundsInRoot()
            }
            .drawWithContent {
                drawContent()
                bubbleData?.let { drawSwipeBubble(it) }
            }
            .padding(bottom = if (isFloatingMode || isLandscape) 0.dp else 0.dp)) {
        if (isLandscape) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
            Row(
                modifier = if (useSplitLandscape) {
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 2.dp, horizontal = 50.dp)
                } else {
                    Modifier
                        .fillMaxHeight()
                        .widthIn(max = QWERTY_FULL_LANDSCAPE_MAX_WIDTH_DP.dp)
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 8.dp)
                },
            ) {
                val corner = LocalKeyCornerRadius.current
                val divider = keyTextColor.copy(alpha = 0.12f)
                val panelPad = LocalKeyVisualPadding.current
                Column(
                    modifier = Modifier
                        .weight(0.40f)
                        .fillMaxHeight()
                        .padding(
                            top = panelPad.calculateTopPadding(),
                            bottom = panelPad.calculateBottomPadding(),
                        )
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(corner))
                        .background(keyBackgroundColor),
                ) {
                    landscapePanelSymbols.chunked(6).forEach { rowSymbols ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            rowSymbols.forEach { sym ->
                                NumberPanelSymbol(
                                    text = sym,
                                    textColor = keyTextColor,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, divider),
                                    onClick = { onKeyPress(sym) },
                                    onPress = { onKeyPressDown?.invoke(sym) },
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(0.60f)
                        .fillMaxHeight()
                ) {
                    NumberRows(
                        onKeyPress = onKeyPress,
                        keyBackgroundColor = keyBackgroundColor,
                        keyTextColor = keyTextColor,
                        specialKeyBackgroundColor = specialKeyBackgroundColor,
                        shadowEnabled = shadowEnabled,
                        shadowElevation = shadowElevation,
                        shadowShapeRadius = shadowShapeRadius,
                        onKeyPressDown = onKeyPressDown,
                        compactMode = true,
                        specialKeyTextColor = specialKeyTextColor,
                        onSwipeStateChange = ::processSwipeState,
                        enterKeyText = enterKeyText,
                    )
                }
            }
            }
        } else {
            // 竖屏：原有布局
            CompositionLocalProvider(
                LocalKeyVisualPadding provides LocalKeyVisualPadding.current
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
            ) {
                NumberRows(
                    onKeyPress = onKeyPress,
                    keyBackgroundColor = keyBackgroundColor,
                    keyTextColor = keyTextColor,
                    specialKeyBackgroundColor = specialKeyBackgroundColor,
                    shadowEnabled = shadowEnabled,
                    shadowElevation = shadowElevation,
                    shadowShapeRadius = shadowShapeRadius,
                    onKeyPressDown = onKeyPressDown,
                    specialKeyTextColor = specialKeyTextColor,
                    onSwipeStateChange = ::processSwipeState,
                    enterKeyText = enterKeyText,
                )
            }
            }
        }

    }
    }
}

@Composable
private fun NumberRows(
    onKeyPress: (String) -> Unit,
    keyBackgroundColor: Color,
    keyTextColor: Color,
    specialKeyBackgroundColor: Color,
    shadowEnabled: Boolean = true,
    shadowElevation: Dp = 1.dp,
    shadowShapeRadius: Dp = 8.dp,
    onKeyPressDown: ((String) -> Unit)? = null,
    onSwipeStateChange: ((SwipeState, Rect) -> Unit)? = null,
    compactMode: Boolean = false,
    specialKeyTextColor: Color = Color.White,
    enterKeyText: String = "确定",
) {
    val keyFontSize = if (compactMode) 16.sp else androidx.compose.ui.unit.TextUnit.Unspecified
    val ctrlFontSize = if (compactMode) 12.sp else androidx.compose.ui.unit.TextUnit.Unspecified
    val suppressCursorMove = LocalSuppressCursorMove.current
    val operators = listOf("+", "-", "*", "/")
    val visualPadding = LocalKeyVisualPadding.current
    val corner = LocalKeyCornerRadius.current
    val groupShadow = rememberKeyShadowModifier(
        enabled = shadowEnabled,
        backgroundColor = specialKeyBackgroundColor,
    )
    val dividerColor = specialKeyTextColor.copy(alpha = 0.22f)

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxHeight(),
        ) {
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth()
                    .padding(visualPadding)
                    .then(groupShadow)
                    .clip(RoundedCornerShape(corner))
                    .background(specialKeyBackgroundColor),
            ) {
                operators.forEachIndexed { index, symbol ->
                    NumberSymbolKey(
                        text = symbol,
                        onClick = { onKeyPress(symbol) },
                        textColor = specialKeyTextColor,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        onPress = { onKeyPressDown?.invoke(symbol) },
                        fontSize = if (compactMode) 14.sp else 18.sp,
                        showDivider = index < operators.lastIndex,
                        dividerColor = dividerColor,
                    )
                }
            }
            IconKeyButton(
                icon = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack),
                onClick = { onKeyPress("abc") },
                backgroundColor = specialKeyBackgroundColor,
                iconColor = specialKeyTextColor,
                modifier = Modifier.weight(1f),
                onPress = { onKeyPressDown?.invoke("abc") },
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
            )
        }

        Column(
            modifier = Modifier
                .weight(3.4f)
                .fillMaxHeight(),
        ) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
            ).forEach { rowKeys ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    rowKeys.forEach { key ->
                        KeyButton(
                            text = key,
                            onClick = { onKeyPress(key) },
                            backgroundColor = keyBackgroundColor,
                            textColor = keyTextColor,
                            onPress = { onKeyPressDown?.invoke(key) },
                            shadowEnabled = shadowEnabled,
                            shadowElevation = shadowElevation,
                            shadowShapeRadius = shadowShapeRadius,
                            modifier = Modifier.weight(1f),
                            fontSize = keyFontSize,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                KeyButton(
                    text = "符号",
                    onClick = { onKeyPress("symbol") },
                    backgroundColor = specialKeyBackgroundColor,
                    textColor = specialKeyTextColor,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPressDown?.invoke("symbol") },
                    shadowEnabled = shadowEnabled,
                    shadowElevation = shadowElevation,
                    shadowShapeRadius = shadowShapeRadius,
                    fontSize = ctrlFontSize,
                )
                KeyButton(
                    text = "0",
                    onClick = { onKeyPress("0") },
                    backgroundColor = keyBackgroundColor,
                    textColor = keyTextColor,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPressDown?.invoke("0") },
                    shadowEnabled = shadowEnabled,
                    shadowElevation = shadowElevation,
                    shadowShapeRadius = shadowShapeRadius,
                    fontSize = keyFontSize,
                )
                KeyButton(
                    text = ".",
                    onClick = { onKeyPress(".") },
                    backgroundColor = keyBackgroundColor,
                    textColor = keyTextColor,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPressDown?.invoke(".") },
                    shadowEnabled = shadowEnabled,
                    shadowElevation = shadowElevation,
                    shadowShapeRadius = shadowShapeRadius,
                    fontSize = keyFontSize,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxHeight(),
        ) {
            SwipeableIconKeyButton(
                icon = rememberVectorPainter(Icons.AutoMirrored.Filled.Backspace),
                onClick = { onKeyPress("delete") },
                backgroundColor = specialKeyBackgroundColor,
                iconColor = specialKeyTextColor,
                modifier = Modifier.weight(1f),
                swipeText = "清空",
                onSwipe = { onKeyPress("clear_composition") },
                onLongClick = { onKeyPress("delete") },
                onPress = { onKeyPressDown?.invoke("delete") },
                swipeUpLabel = "上滑清空",
                swipeDownLabel = "下滑撤回",
                onSwipeUp = { onKeyPress("clear_all") },
                onSwipeDown = { onKeyPress("undo_clear") },
                onSwipeLeft = {
                    suppressCursorMove.value = true
                    onKeyPress("clear_composition")
                },
                onSwipeStateChange = onSwipeStateChange,
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
            )
            KeyButton(
                text = "空格",
                onClick = { onKeyPress("space") },
                backgroundColor = specialKeyBackgroundColor,
                textColor = specialKeyTextColor,
                modifier = Modifier.weight(1f),
                onPress = { onKeyPressDown?.invoke("space") },
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                fontSize = ctrlFontSize,
            )
            IconKeyButton(
                icon = rememberVectorPainter(Icons.Default.EmojiEmotions),
                onClick = { onKeyPress("emoji") },
                backgroundColor = specialKeyBackgroundColor,
                iconColor = specialKeyTextColor,
                modifier = Modifier.weight(1f),
                onPress = { onKeyPressDown?.invoke("emoji") },
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
            )
            KeyButton(
                text = enterKeyText,
                onClick = { onKeyPress("enter") },
                backgroundColor = specialKeyBackgroundColor,
                textColor = specialKeyTextColor,
                modifier = Modifier.weight(1f),
                onPress = { onKeyPressDown?.invoke("enter") },
                shadowEnabled = shadowEnabled,
                shadowElevation = shadowElevation,
                shadowShapeRadius = shadowShapeRadius,
                fontSize = ctrlFontSize,
            )
        }
    }
}

@Composable
private fun NumberPanelSymbol(
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPress: (() -> Unit)? = null,
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnPress by rememberUpdatedState(onPress)
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    currentOnPress?.invoke()
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { currentOnClick() },
            )
        },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = if (isPressed) 0.5f else 1f),
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun NumberSymbolKey(
    text: String,
    onClick: () -> Unit,
    textColor: Color,
    modifier: Modifier = Modifier,
    onPress: (() -> Unit)? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    showDivider: Boolean = false,
    dividerColor: Color = Color.Transparent,
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnPress by rememberUpdatedState(onPress)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isPressed) Color.Black.copy(alpha = 0.12f) else Color.Transparent)
            .drawWithContent {
                drawContent()
                if (showDivider && dividerColor.alpha > 0f) {
                    val inset = size.width * 0.22f
                    drawLine(
                        color = dividerColor,
                        start = Offset(inset, size.height),
                        end = Offset(size.width - inset, size.height),
                        strokeWidth = 1.5f,
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    isPressed = true
                    currentOnPress?.invoke()
                    tryAwaitRelease()
                    isPressed = false
                }, onTap = { currentOnClick() })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(vertical = 2.dp),
        )
    }
}
