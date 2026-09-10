package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iskcon.bhagavaddarshan.ui.theme.BdShape
import com.iskcon.bhagavaddarshan.ui.theme.BdSpace
import com.iskcon.bhagavaddarshan.ui.theme.BdType
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.MotionSpecs
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron
import com.iskcon.bhagavaddarshan.ui.theme.customerContentWidth

@Composable
fun CustomerMagazinesScreen(language: CustomerLanguage = CustomerLanguage.ENGLISH) {
    var year by remember { mutableIntStateOf(CustomerCatalog.magazineYears.first()) }
    var selected by remember { mutableStateOf<MagazineIssue?>(null) }
    val issues = remember(year) { CustomerCatalog.magazines.filter { it.year == year } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditCream)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .customerContentWidth(),
            contentPadding = PaddingValues(
                start = BdSpace.ScreenHorizontal,
                end = BdSpace.ScreenHorizontal,
                top = BdSpace.ScreenTop,
                bottom = BdSpace.BottomNavClearance
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column {
                    Text(
                        tr(
                            language,
                            "Physical copies posted to your home. Digital reading is coming soon.",
                            "ముద్రిత ప్రతులు మీ ఇంటికి పంపబడతాయి. డిజిటల్ పఠనం త్వరలో వస్తుంది."
                        ),
                        style = BdType.PageSubtitle
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomerCatalog.magazineYears.forEach { y ->
                            val selectedYear = y == year
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selectedYear) EditTerracotta else Color.White)
                                    .border(
                                        1.dp,
                                        if (selectedYear) EditTerracotta else UxGold200,
                                        CircleShape
                                    )
                                    .clickable { year = y }
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    y.toString(),
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = BdType.Body.fontSize,
                                    color = if (selectedYear) Color.White else EditTerracotta.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }
                }
            }
            items(issues, key = { "${it.year}-${it.monthLabel}" }) { issue ->
                MagazineCoverCard(issue = issue, onClick = { selected = issue })
            }
        }
    }

    selected?.let { issue ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(issue.title, fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${issue.monthLabel} is a physical copy of Bhagavad Darshan. Paid subscribers receive it by post at the registered address. Digital reading is coming soon.",
                    fontFamily = Montserrat
                )
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) {
                    Text("Hare Krishna", color = UxSaffron, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun MagazineCoverCard(
    issue: MagazineIssue,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "magPress"
    )
    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, UxGold200, RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            AsyncImage(
                model = issue.imageUrl,
                contentDescription = issue.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            issue.monthLabel.uppercase(),
            style = BdType.MagazineMonth
        )
        Text(
            issue.title,
            style = BdType.MagazineTitle
        )
    }
}
