package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.ui.components.BdCard
import com.iskcon.bhagavaddarshan.ui.components.EmergedButton
import com.iskcon.bhagavaddarshan.ui.theme.BdSpace
import com.iskcon.bhagavaddarshan.ui.theme.BdType
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.customerContentWidth

@Composable
fun CustomerPlansScreen(
    app: BhagavadDarshanApp,
    language: CustomerLanguage = CustomerLanguage.ENGLISH,
    onCheckout: (planYears: Int) -> Unit
) {
    val hasSub = app.session.subscriptionId > 0L
    val plans = SubscriptionPlan.activePlans
    var selectedYears by rememberSaveable { mutableIntStateOf(SubscriptionPlan.THIRTY_MONTHS.years) }
    val selected = plans.firstOrNull { it.years == selectedYears } ?: plans.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditCream)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .customerContentWidth(),
            verticalArrangement = Arrangement.spacedBy(BdSpace.CardGap),
            contentPadding = PaddingValues(
                start = BdSpace.ScreenHorizontal,
                end = BdSpace.ScreenHorizontal,
                top = BdSpace.ScreenTop,
                bottom = 108.dp
            )
        ) {
            item {
                Text(
                    if (hasSub)
                        tr(
                            language,
                            "Your new term will be added after the current plan ends. Postage is included.",
                            "మీ ప్రస్తుత ప్లాన్ ముగిసిన తరువాత కొత్త కాలం జత అవుతుంది. పోస్టేజ్ కలిపి ఉంటుంది."
                        )
                    else
                        tr(
                            language,
                            "Monthly magazine at your door. Postage is included.",
                            "ప్రతి నెల మీ ఇంటి వద్దకు పత్రిక చేరుతుంది. పోస్టేజ్ కలిపి ఉంటుంది."
                        ),
                    style = BdType.PageSubtitle
                )
            }

            items(plans, key = { it.years }) { plan ->
                val chosen = plan.years == selectedYears
                val monthly = plan.totalRupees / plan.months.toFloat()
                BdCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { selectedYears = plan.years },
                    selected = chosen,
                    highlighted = plan.isPopular && !chosen
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${plan.months} ${tr(language, "Month Plan", "నెలల ప్లాన్")}",
                                    style = BdType.CardTitle
                                )
                                if (plan.isPopular) {
                                    Spacer(Modifier.size(8.dp))
                                    Text(
                                        "MOST POPULAR",
                                        fontFamily = Montserrat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = EditTerracotta,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.85f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            if (plan == SubscriptionPlan.THIRTY_MONTHS) {
                                Text(
                                    tr(
                                        language,
                                        "24 Months + 6 Months FREE",
                                        "24 నెలలు + 6 నెలలు ఉచితం"
                                    ),
                                    style = BdType.Caption
                                )
                            }
                            Text(
                                "${plan.months} ${tr(language, "monthly issues", "నెలవారీ సంచికలు")}",
                                style = BdType.Caption
                            )
                            Text(
                                tr(language, "Postage included", "పోస్టేజ్ కలిపి"),
                                style = BdType.Caption
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (chosen) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(EditTerracotta),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            Text("₹${plan.totalRupees}", style = BdType.Price)
                            Text(
                                "₹${"%.2f".format(monthly)}/month",
                                fontFamily = Montserrat,
                                fontSize = 14.sp,
                                color = EditMuted
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(EditCream)
                .padding(start = BdSpace.ScreenHorizontal, end = BdSpace.ScreenHorizontal, bottom = 16.dp, top = 8.dp)
        ) {
            EmergedButton(
                text = tr(
                    language,
                    "Continue to shipping  →",
                    "షిప్పింగ్‌కు కొనసాగించండి  →"
                ),
                onClick = { onCheckout(selected.years) },
                modifier = Modifier
                    .fillMaxWidth()
                    .customerContentWidth()
                    .align(Alignment.Center)
            )
        }
    }
}
