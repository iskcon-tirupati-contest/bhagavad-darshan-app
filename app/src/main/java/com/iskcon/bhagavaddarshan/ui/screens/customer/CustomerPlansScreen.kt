package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.ui.theme.EditChocolate
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditPopularBg
import com.iskcon.bhagavaddarshan.ui.theme.EditPopularBorder
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay

@Composable
fun CustomerPlansScreen(
    app: BhagavadDarshanApp,
    language: CustomerLanguage = CustomerLanguage.ENGLISH,
    onCheckout: (planYears: Int) -> Unit
) {
    val hasSub = app.session.subscriptionId > 0L
    val plans = SubscriptionPlan.entries.filter { it.years in listOf(1, 2, 3, 5) }
    var selectedYears by rememberSaveable { mutableIntStateOf(3) }
    val selected = plans.firstOrNull { it.years == selectedYears } ?: plans.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditCream)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 108.dp)
        ) {
            item {
                Column {
                    Text(
                        "BHAGAVAD DARSHAN",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        color = EditTerracotta
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        tr(language, "Choose a plan for your home", "మీ ఇంటికి సరైన ప్లాన్ ఎంచుకోండి"),
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = EditChocolate,
                        lineHeight = 34.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (hasSub)
                            tr(
                                language,
                                "Your new term will be added after the current plan ends. Gift books and postage are included.",
                                "మీ ప్రస్తుత ప్లాన్ ముగిసిన తరువాత కొత్త కాలం జత అవుతుంది. గిఫ్ట్ పుస్తకాలు మరియు పోస్టేజ్ కలిపి ఉంటాయి."
                            )
                        else
                            tr(
                                language,
                                "Monthly magazine at your door. Gift books and postage are included.",
                                "ప్రతి నెల మీ ఇంటి వద్దకు పత్రిక చేరుతుంది. గిఫ్ట్ పుస్తకాలు మరియు పోస్టేజ్ కలిపి ఉంటాయి."
                            ),
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = EditMuted,
                        lineHeight = 20.sp
                    )
                }
            }

            items(plans, key = { it.years }) { plan ->
                val chosen = plan.years == selectedYears
                val shape = RoundedCornerShape(20.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (chosen) 6.dp else 2.dp, shape, spotColor = Color(0x14000000))
                        .clip(shape)
                        .background(if (chosen || plan.isPopular) EditPopularBg else Color.White)
                        .border(
                            width = if (chosen || plan.isPopular) 1.5.dp else 1.dp,
                            color = if (chosen || plan.isPopular) EditPopularBorder else Color(0xFFE8DDD0),
                            shape = shape
                        )
                        .clickable { selectedYears = plan.years }
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                                            append(plan.years.toString())
                                        }
                                        withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                                            append(tr(language, " Year Plan", " సంవత్సరాల ప్లాన్"))
                                        }
                                    },
                                    fontFamily = PlayfairDisplay,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = EditChocolate
                                )
                                if (plan.isPopular) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "MOST POPULAR",
                                        fontFamily = Inter,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = EditTerracotta,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.8f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "• ${plan.years * 12} monthly issues of Bhagavad Darshan",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = EditMuted
                            )
                            Text(
                                "• ${plan.giftBooks} gift book${if (plan.giftBooks == 1) "" else "s"} included",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = EditMuted
                            )
                            Text(
                                "• Postage ₹${plan.postageRupees} included in total",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = EditMuted
                            )
                        }
                        Text(
                            "₹${plan.totalRupees}",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = EditChocolate
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(EditCream)
                .padding(start = 22.dp, end = 22.dp, bottom = 16.dp, top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EditTerracotta)
                    .clickable { onCheckout(selected.years) }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        selected.isPopular && hasSub -> tr(language, "Renew most popular", "ప్రసిద్ధ ప్లాన్ రిన్యూ చేయండి")
                        selected.isPopular -> tr(language, "Choose most popular", "ప్రసిద్ధ ప్లాన్ ఎంచుకోండి")
                        hasSub -> tr(language, "Renew ${selected.years} year plan", "${selected.years} సంవత్సరాల ప్లాన్ రిన్యూ చేయండి")
                        else -> tr(language, "Continue with ${selected.years} year", "${selected.years} సంవత్సరాల ప్లాన్ కొనసాగించండి")
                    },
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}
