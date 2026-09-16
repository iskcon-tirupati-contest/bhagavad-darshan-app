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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.ui.theme.IvoryDeep
import com.iskcon.bhagavaddarshan.ui.theme.TempleGreen
import com.iskcon.bhagavaddarshan.ui.theme.TempleMaroon

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
        containerColor = Color.Transparent,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    consumeStatusBars: Boolean = true,
    contentHeight: Dp = CompactTopBarContentHeight
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                spotColor = TempleMaroon.copy(alpha = 0.14f),
                ambientColor = Color(0xFF3E2723).copy(alpha = 0.08f)
            )
            .background(ChromeTopBrush)
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
        PremiumGoldHairline()
    }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                spotColor = TempleMaroon.copy(alpha = 0.16f),
                ambientColor = Color(0xFF3E2723).copy(alpha = 0.08f)
            )
            .background(brush)
    ) {
        TopAppBar(
            title = title,
            modifier = modifier
                .then(if (consumeStatusBars) Modifier.statusBarsPadding() else Modifier),
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
        PremiumGoldHairline()
    }
}

@Composable
fun CompactBottomBar(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                spotColor = TempleMaroon.copy(alpha = 0.20f),
                ambientColor = Color(0xFF3E2723).copy(alpha = 0.12f)
            )
            .background(
                if (containerColor == Color.Transparent || containerColor == IvoryDeep) {
                    ChromeBottomBrush
                } else {
                    Brush.verticalGradient(listOf(containerColor.copy(alpha = 0.96f), containerColor))
                }
            )
    ) {
        PremiumGoldHairline()
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(CompactBottomBarTopPad)
        )
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(CompactBottomBarHeight),
            containerColor = Color.Transparent,
            contentColor = contentColor,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
            content = content
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
        )
    }
}
