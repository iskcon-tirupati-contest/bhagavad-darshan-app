package com.iskcon.bhagavaddarshan.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Content height only — status-bar inset is added by windowInsets (do not also fix total height). */
val CompactTopBarContentHeight = 52.dp
val CompactBottomBarHeight = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    consumeStatusBars: Boolean = true,
    contentHeight: Dp = CompactTopBarContentHeight
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        windowInsets = if (consumeStatusBars) {
            TopAppBarDefaults.windowInsets
        } else {
            WindowInsets(0, 0, 0, 0)
        },
        expandedHeight = contentHeight
    )
}

@Composable
fun CompactBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier.height(CompactBottomBarHeight),
        tonalElevation = 1.dp,
        windowInsets = WindowInsets(0, 0, 0, 0),
        content = content
    )
}
