package com.iskcon.bhagavaddarshan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.ui.theme.IvoryDeep
import com.iskcon.bhagavaddarshan.ui.theme.TempleGreen

val CompactTopBarContentHeight = 56.dp
val CompactBottomBarHeight = 62.dp
val CompactBottomBarTopPad = 8.dp
val ListBottomSafeGap = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = IvoryDeep,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    consumeStatusBars: Boolean = true,
    contentHeight: Dp = CompactTopBarContentHeight
) {
    TopAppBar(
        title = title,
        modifier = if (consumeStatusBars) modifier.statusBarsPadding() else modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        windowInsets = WindowInsets(0, 0, 0, 0),
        expandedHeight = contentHeight
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTopBar(
    title: @Composable () -> Unit,
    brush: Brush,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    consumeStatusBars: Boolean = true,
    contentHeight: Dp = CompactTopBarContentHeight
) {
    TopAppBar(
        title = title,
        modifier = modifier
            .then(if (consumeStatusBars) Modifier.statusBarsPadding() else Modifier)
            .background(brush),
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = TempleGreen,
            actionIconContentColor = TempleGreen,
            navigationIconContentColor = TempleGreen
        ),
        windowInsets = WindowInsets(0, 0, 0, 0),
        expandedHeight = contentHeight
    )
}

@Composable
fun CompactBottomBar(
    modifier: Modifier = Modifier,
    containerColor: Color = IvoryDeep,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable RowScope.() -> Unit
) {
    // Keep a fixed content height, then paint an inset strip below so system
    // navigation padding cannot crush icons/labels (which made the bar look gone).
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(CompactBottomBarTopPad)
                .background(containerColor)
        )
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(CompactBottomBarHeight),
            containerColor = containerColor,
            contentColor = contentColor,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
            content = content
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(containerColor)
        )
    }
}
