package com.iskcon.bhagavaddarshan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.SacredGold
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxInk

@Composable
fun OtpSixBoxes(
    otp: String,
    focusRequesters: List<FocusRequester>,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val digits = List(6) { index -> otp.getOrNull(index)?.toString().orEmpty() }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        digits.forEachIndexed { index, digit ->
            BasicTextField(
                value = digit,
                onValueChange = { raw ->
                    val cleaned = raw.filter(Char::isDigit)
                    when {
                        cleaned.isEmpty() -> {
                            val updated = otp.take(index) + otp.drop(index + 1)
                            onOtpChange(updated)
                            if (index > 0) focusRequesters[index - 1].requestFocus()
                        }
                        cleaned.length == 1 -> {
                            val updated = buildString {
                                append(otp.take(index))
                                append(cleaned)
                                append(otp.drop(index + 1))
                            }.filter(Char::isDigit).take(6)
                            onOtpChange(updated)
                            if (index < 5) focusRequesters[index + 1].requestFocus()
                        }
                        else -> {
                            val updated = (otp.take(index) + cleaned).filter(Char::isDigit).take(6)
                            onOtpChange(updated)
                            focusRequesters[updated.length.coerceAtMost(5)].requestFocus()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .focusRequester(focusRequesters[index]),
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = UxInk,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = SolidColor(SacredGold),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.5.dp,
                                color = if (digit.isNotEmpty()) SacredGold else UxGold200,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        inner()
                    }
                }
            )
        }
    }
}

/** Indian States and Union Territories for registration/profile dropdowns. */
val IndianStatesAndUts = listOf(
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chhattisgarh",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
    "Andaman and Nicobar Islands",
    "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi",
    "Jammu and Kashmir",
    "Ladakh",
    "Lakshadweep",
    "Puducherry"
)
