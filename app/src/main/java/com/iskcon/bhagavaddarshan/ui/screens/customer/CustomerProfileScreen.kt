package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.screens.friendlyError
import com.iskcon.bhagavaddarshan.ui.theme.EditCard
import com.iskcon.bhagavaddarshan.ui.theme.EditChocolate
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditPeach
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun CustomerProfileScreen(
    app: BhagavadDarshanApp,
    language: CustomerLanguage = CustomerLanguage.ENGLISH,
    onLogout: () -> Unit,
    onHelp: () -> Unit = {},
    onLanguageChange: (CustomerLanguage) -> Unit = {}
) {
    val api = remember { BdApi() }
    val token = app.session.authToken
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(app.session.agentName) }
    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var villageTown by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Andhra Pradesh") }
    var message by remember { mutableStateOf<String?>(null) }
    var payments by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var editing by remember { mutableStateOf(false) }
    var showPayments by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    fun reload() {
        if (token.isBlank()) return
        scope.launch {
            api.getMe(token).onSuccess { json ->
                val c = json.optJSONObject("customer") ?: return@onSuccess
                name = c.optString("name")
                houseNo = c.optString("house_no")
                street = c.optString("street")
                villageTown = c.optString("village_town")
                district = c.optString("district")
                pincode = c.optString("pincode")
                state = c.optString("state").ifBlank { "Andhra Pradesh" }
                val sub = json.optJSONObject("subscription")
                if (sub != null) app.session.bindSubscription(sub.optLong("id"), c.optString("name"))
            }
            api.paymentHistory(token).onSuccess { json ->
                val arr = json.optJSONArray("payments")
                payments = buildList {
                    if (arr != null) for (i in 0 until arr.length()) add(arr.getJSONObject(i))
                }
            }
        }
    }

    LaunchedEffect(token) { reload() }

    val addressLines = listOf(
        listOf(houseNo, street).filter { it.isNotBlank() }.joinToString(", "),
        villageTown,
        listOf(district, state, pincode).filter { it.isNotBlank() }.joinToString(", ")
    ).filter { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EditCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .padding(bottom = 28.dp)
    ) {
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
            tr(language, "Profile", "ప్రొఫైల్"),
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = EditChocolate
        )
        Spacer(Modifier.height(6.dp))
        Text(
            name.ifBlank { "Devotee" },
            fontFamily = Inter,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = EditChocolate
        )
        Text(
            "+91 ${app.session.agentPhone}",
            fontFamily = Inter,
            fontSize = 14.sp,
            color = EditMuted
        )

        Spacer(Modifier.height(22.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x18000000))
                .clip(RoundedCornerShape(22.dp))
                .background(EditCard)
                .border(1.dp, Color(0xFFE8DDD0), RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EditPeach),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = EditTerracotta)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Delivery address",
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EditChocolate
                    )
                    Spacer(Modifier.height(6.dp))
                    if (addressLines.isEmpty()) {
                        Text(
                            "Add your delivery address so the magazine can be posted to your home.",
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = EditMuted
                        )
                    } else {
                        addressLines.forEach {
                            Text(it, fontFamily = Inter, fontSize = 14.sp, color = EditMuted, lineHeight = 20.sp)
                        }
                    }
                }
            }

            if (editing) {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = customerFieldColors())
                    OutlinedTextField(value = houseNo, onValueChange = { houseNo = it }, label = { Text("House / flat") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = customerFieldColors())
                    OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = customerFieldColors())
                    OutlinedTextField(value = villageTown, onValueChange = { villageTown = it }, label = { Text("Village / town") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = customerFieldColors())
                    OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = customerFieldColors())
                    OutlinedTextField(value = pincode, onValueChange = { pincode = it.filter(Char::isDigit).take(6) }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = customerFieldColors())
                    OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = customerFieldColors())
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EditTerracotta)
                    .clickable {
                        if (!editing) {
                            editing = true
                            return@clickable
                        }
                        scope.launch {
                            val body = JSONObject()
                                .put("name", name.trim())
                                .put("houseNo", houseNo.trim())
                                .put("street", street.trim())
                                .put("villageTown", villageTown.trim())
                                .put("district", district.trim())
                                .put("pincode", pincode.trim())
                                .put("state", state.trim())
                            api.updateProfile(token, body).onSuccess {
                                app.session.updateProfile(name.trim(), app.session.agentPhone)
                                message = "Address saved"
                                editing = false
                            }.onFailure {
                                message = friendlyError(it)
                            }
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (editing) "Save address" else "Edit & save address",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
            if (editing) {
                Text(
                    "Cancel",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = EditTerracotta,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                        .clickable { editing = false }
                )
            }
        }
        message?.let {
            Text(it, fontFamily = Inter, color = EditTerracotta, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(EditPeach)
                .clickable { showLanguageMenu = true }
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = EditTerracotta)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tr(language, "Language", "భాష"),
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = EditChocolate
                    )
                    Text(
                        if (language == CustomerLanguage.ENGLISH) "English" else "తెలుగు",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = EditMuted
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EditTerracotta)
            }
            DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                DropdownMenuItem(
                    text = { Text("English") },
                    onClick = {
                        showLanguageMenu = false
                        onLanguageChange(CustomerLanguage.ENGLISH)
                    }
                )
                DropdownMenuItem(
                    text = { Text("తెలుగు") },
                    onClick = {
                        showLanguageMenu = false
                        onLanguageChange(CustomerLanguage.TELUGU)
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE8DDD0), RoundedCornerShape(20.dp))
                .clickable { showPayments = !showPayments }
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EditPeach),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = EditTerracotta)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Payment history",
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = EditChocolate
                    )
                    Text("Your renewals and seva", fontFamily = Inter, fontSize = 13.sp, color = EditMuted)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFE8DDD0))
            }
            if (showPayments) {
                Spacer(Modifier.height(12.dp))
                if (payments.isEmpty()) {
                    Text("No transactions yet.", fontFamily = Inter, color = EditMuted)
                } else {
                    payments.forEach { p ->
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Text(
                                "₹${p.optInt("amount_paise") / 100} · ${p.optString("status")}",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                color = EditChocolate
                            )
                            Text(p.optString("order_id"), fontFamily = Inter, fontSize = 12.sp, color = EditMuted)
                        }
                    }
                }
            }
        }
    }
}
