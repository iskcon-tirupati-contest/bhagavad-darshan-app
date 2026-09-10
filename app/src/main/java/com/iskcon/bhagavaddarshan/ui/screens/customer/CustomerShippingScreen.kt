package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.EmergedButton
import com.iskcon.bhagavaddarshan.ui.theme.EditChocolate
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.TulsiGreen
import com.iskcon.bhagavaddarshan.ui.theme.customerContentWidth

enum class ShippingMode(val feeRupees: Int) {
    STANDARD(0),
    EXPRESS(40);

    val feePaise: Int get() = feeRupees * 100
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerShippingScreen(
    planYears: Int,
    language: CustomerLanguage = CustomerLanguage.ENGLISH,
    onBack: () -> Unit,
    onContinue: (shippingPaise: Int) -> Unit
) {
    val plan = SubscriptionPlan.fromYears(planYears)
    var mode by rememberSaveable { mutableStateOf(ShippingMode.STANDARD.name) }
    val selected = ShippingMode.entries.firstOrNull { it.name == mode } ?: ShippingMode.STANDARD
    val total = plan.totalRupees + selected.feeRupees

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        containerColor = EditCream,
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        tr(language, "Shipping", "షిప్పింగ్"),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditCream)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                EmergedButton(
                    text = tr(
                        language,
                        "Confirm & Pay  ₹$total  →",
                        "నిర్ధారించి చెల్లించండి  ₹$total  →"
                    ),
                    onClick = { onContinue(selected.feePaise) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .customerContentWidth()
                        .align(Alignment.Center)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .customerContentWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                tr(language, "Choose delivery speed", "డెలివరీ వేగాన్ని ఎంచుకోండి"),
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = EditChocolate
            )
            Text(
                tr(
                    language,
                    "Plan price already includes standard postage. Express is an optional upgrade.",
                    "ప్లాన్ ధరలో స్టాండర్డ్ పోస్టేజ్ ఉంది. ఎక్స్‌ప్రెస్ ఐచ్ఛిక అప్‌గ్రేడ్."
                ),
                fontFamily = Inter,
                fontSize = 15.sp,
                color = EditMuted,
                lineHeight = 22.sp
            )

            ShippingOptionCard(
                selected = selected == ShippingMode.STANDARD,
                icon = Icons.Default.LocalShipping,
                title = tr(language, "Standard shipping", "స్టాండర్డ్ షిప్పింగ్"),
                subtitle = tr(language, "Included with your plan", "మీ ప్లాన్‌లో కలిపి ఉంది"),
                priceLabel = "+ ₹0",
                priceHint = "₹${plan.totalRupees} + ₹0 = ₹${plan.totalRupees}",
                onClick = { mode = ShippingMode.STANDARD.name }
            )

            ShippingOptionCard(
                selected = selected == ShippingMode.EXPRESS,
                icon = Icons.Default.Speed,
                title = tr(language, "Express shipping", "ఎక్స్‌ప్రెస్ షిప్పింగ్"),
                subtitle = tr(language, "Faster dispatch · +₹40", "వేగవంతమైన డిస్పాచ్ · +₹40"),
                priceLabel = "+ ₹40",
                priceHint = "₹${plan.totalRupees} + ₹40 = ₹${plan.totalRupees + 40}",
                highlight = true,
                onClick = { mode = ShippingMode.EXPRESS.name }
            )

            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8DCCD), RoundedCornerShape(18.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    tr(language, "Order summary", "ఆర్డర్ సారాంశం"),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = EditMuted
                )
                SummaryRow(
                    label = "${plan.months} ${tr(language, "Month Plan", "నెలల ప్లాన్")}",
                    value = "₹${plan.totalRupees}"
                )
                SummaryRow(
                    label = if (selected == ShippingMode.EXPRESS)
                        tr(language, "Express shipping", "ఎక్స్‌ప్రెస్ షిప్పింగ్")
                    else
                        tr(language, "Standard shipping", "స్టాండర్డ్ షిప్పింగ్"),
                    value = if (selected.feeRupees == 0) "₹0" else "₹${selected.feeRupees}",
                    valueColor = if (selected.feeRupees == 0) TulsiGreen else EditChocolate
                )
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE8DCCD))
                )
                SummaryRow(
                    label = tr(language, "Total to pay", "మొత్తం చెల్లింపు"),
                    value = "₹$total",
                    bold = true
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ShippingOptionCard(
    selected: Boolean,
    icon: ImageVector,
    title: String,
    subtitle: String,
    priceLabel: String,
    priceHint: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val border = when {
        selected -> EditTerracotta
        highlight -> Color(0xFFE8DCCD)
        else -> Color(0xFFE8DCCD)
    }
    val bg = when {
        selected -> Color(0xFFFFF6EF)
        else -> Color.White
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (selected) EditTerracotta.copy(alpha = 0.12f) else Color(0xFFF5EEE4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = EditTerracotta, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = EditChocolate)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontFamily = Inter, fontSize = 13.sp, color = EditMuted)
            Spacer(Modifier.height(4.dp))
            Text(priceHint, fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = EditChocolate)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(EditTerracotta),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(
                priceLabel,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (selected) EditTerracotta else EditMuted
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    bold: Boolean = false,
    valueColor: Color = EditChocolate
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            fontFamily = Inter,
            fontSize = if (bold) 16.sp else 15.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (bold) EditChocolate else EditMuted
        )
        Text(
            value,
            fontFamily = if (bold) PlayfairDisplay else Inter,
            fontSize = if (bold) 22.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
