package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.AnimatedCard
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.IndianStatesAndUts
import com.iskcon.bhagavaddarshan.ui.components.OtpSixBoxes
import com.iskcon.bhagavaddarshan.ui.components.RadiantGoldButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.screens.applyAuthSession
import com.iskcon.bhagavaddarshan.ui.screens.friendlyError
import com.iskcon.bhagavaddarshan.ui.theme.CustomTypography
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.GoldLight
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.TextSecondaryDark
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.util.FormValidators
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomerRegisterScreen(
    app: BhagavadDarshanApp,
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    val api = remember { BdApi() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var villageTown by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Andhra Pradesh") }
    var stateMenuOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val otpFocusRequesters = remember { List(6) { FocusRequester() } }
    val otpBringIntoView = remember { BringIntoViewRequester() }
    val fieldBringIntoView = remember { BringIntoViewRequester() }

    Scaffold(
        containerColor = EditCream,
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        "Create account",
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        color = UxInk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = UxInk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EditCream,
                    titleContentColor = UxInk,
                    navigationIconContentColor = UxInk
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EditCream)
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Create your devotee account. Phone number is your login ID.",
                        style = CustomTypography.bodyLarge,
                        color = TextSecondaryDark
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter(Char::isDigit).take(10); error = null },
                        label = { Text("Phone Number*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(fieldBringIntoView)
                            .onFocusChanged { if (it.isFocused) scope.launch { fieldBringIntoView.bringIntoView() } },
                        singleLine = true,
                        enabled = !otpSent,
                        shape = RoundedCornerShape(12.dp),
                        colors = customerFieldColors()
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) scope.launch { fieldBringIntoView.bringIntoView() } },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = customerFieldColors()
                    )
                    OutlinedTextField(
                        value = houseNo,
                        onValueChange = { houseNo = it },
                        label = { Text("House / Flat") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = customerFieldColors()
                    )
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Street / Area") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = customerFieldColors()
                    )
                    OutlinedTextField(
                        value = villageTown,
                        onValueChange = { villageTown = it },
                        label = { Text("City / Village*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(fieldBringIntoView)
                            .onFocusChanged { if (it.isFocused) scope.launch { fieldBringIntoView.bringIntoView() } },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = customerFieldColors()
                    )
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it.filter(Char::isDigit).take(6) },
                        label = { Text("Pincode*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) scope.launch { fieldBringIntoView.bringIntoView() } },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = customerFieldColors()
                    )

                    Text("State*", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, color = UxInk)
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, UxGold200, RoundedCornerShape(12.dp))
                                .background(ColorWhiteSoft)
                                .clickable { stateMenuOpen = true }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(state, fontFamily = Montserrat, color = UxInk)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EditMuted)
                        }
                        DropdownMenu(
                            expanded = stateMenuOpen,
                            onDismissRequest = { stateMenuOpen = false }
                        ) {
                            IndianStatesAndUts.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontFamily = Montserrat) },
                                    onClick = {
                                        state = option
                                        stateMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = otpSent, enter = fadeIn() + expandVertically()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(otpBringIntoView),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "WhatsApp OTP*",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                color = UxInk
                            )
                            OtpSixBoxes(
                                otp = otp,
                                focusRequesters = otpFocusRequesters,
                                onOtpChange = {
                                    otp = it
                                    error = null
                                }
                            )
                            LaunchedEffect(otpSent) {
                                if (otpSent) {
                                    otpBringIntoView.bringIntoView()
                                    otpFocusRequesters.firstOrNull()?.requestFocus()
                                }
                            }
                        }
                    }

                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    if (!otpSent) {
                        RadiantGoldButton(
                            text = if (loading) "Please wait…" else "Send OTP",
                            onClick = {
                                FormValidators.phone(phone)?.let { error = it; return@RadiantGoldButton }
                                if (name.trim().length < 2) {
                                    error = "Enter full name"
                                    return@RadiantGoldButton
                                }
                                if (villageTown.isBlank()) {
                                    error = "City / village is required"
                                    return@RadiantGoldButton
                                }
                                if (pincode.length != 6) {
                                    error = "Enter 6-digit pincode"
                                    return@RadiantGoldButton
                                }
                                if (state.isBlank()) {
                                    error = "Select state"
                                    return@RadiantGoldButton
                                }
                                loading = true
                                scope.launch {
                                    val result = api.sendOtp(phone, "register")
                                    loading = false
                                    result.onSuccess {
                                        otpSent = true
                                        error = null
                                    }.onFailure { error = friendlyError(it) }
                                }
                            },
                            enabled = !loading
                        )
                    } else {
                        RadiantGoldButton(
                            text = if (loading) "Please wait…" else "Verify & Create account",
                            onClick = {
                                if (otp.length != 6) {
                                    error = "Enter 6-digit OTP"
                                    return@RadiantGoldButton
                                }
                                loading = true
                                scope.launch {
                                    val result = api.verifyRegister(
                                        phone = phone,
                                        otp = otp,
                                        name = name.trim(),
                                        houseNo = houseNo.trim(),
                                        street = street.trim(),
                                        villageTown = villageTown.trim(),
                                        district = "",
                                        pincode = pincode,
                                        state = state.trim()
                                    )
                                    loading = false
                                    result.onSuccess { json ->
                                        applyAuthSession(app.session, json, remember = true)
                                        onRegistered()
                                    }.onFailure { error = friendlyError(it) }
                                }
                            },
                            enabled = !loading
                        )
                        OutlinedButton(
                            onClick = {
                                otpSent = false
                                otp = ""
                                error = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Edit details", color = GoldLight) }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private val ColorWhiteSoft = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f)
