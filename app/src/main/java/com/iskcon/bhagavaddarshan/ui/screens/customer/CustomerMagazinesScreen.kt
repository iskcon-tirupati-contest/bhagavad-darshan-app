package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.UxCream
import com.iskcon.bhagavaddarshan.ui.theme.UxGold100
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxGold600
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron

@Composable
fun CustomerMagazinesScreen(language: CustomerLanguage = CustomerLanguage.ENGLISH) {
    var year by remember { mutableIntStateOf(CustomerCatalog.magazineYears.first()) }
    var selected by remember { mutableStateOf<MagazineIssue?>(null) }
    val issues = remember(year) { CustomerCatalog.magazines.filter { it.year == year } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UxCream)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "BHAGAVAD DARSHAN",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            color = UxSaffron
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            tr(language, "Past magazines", "గత మ్యాగజైన్స్"),
                            fontFamily = PlayfairDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = UxInk
                        )
                        Text(
                            tr(
                                language,
                                "Physical copies posted to your home. Digital reading is coming soon.",
                                "ముద్రిత ప్రతులు మీ ఇంటికి పంపబడతాయి. డిజిటల్ పఠనం త్వరలో వస్తుంది."
                            ),
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = UxInk.copy(alpha = 0.62f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CustomerCatalog.magazineYears.forEach { y ->
                            val selectedYear = y == year
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selectedYear) UxSaffron else Color.White)
                                    .border(
                                        1.dp,
                                        if (selectedYear) UxSaffron else UxGold200,
                                        CircleShape
                                    )
                                    .clickable { year = y }
                                    .padding(horizontal = 22.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    y.toString(),
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedYear) Color.White else UxInk.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
            items(issues, key = { "${it.year}-${it.monthLabel}" }) { issue ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, UxGold200, RoundedCornerShape(16.dp))
                            .clickable { selected = issue }
                    ) {
                        AsyncImage(
                            model = issue.imageUrl,
                            contentDescription = issue.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.92f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "View", tint = UxSaffron)
                        }
                    }
                    Text(
                        issue.monthLabel.uppercase(),
                        fontFamily = Inter,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = UxGold600
                    )
                    Text(
                        issue.title,
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = UxInk
                    )
                }
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
                    fontFamily = Inter
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
