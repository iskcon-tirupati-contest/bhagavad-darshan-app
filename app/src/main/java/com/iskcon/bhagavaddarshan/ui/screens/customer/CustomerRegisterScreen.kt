package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.AnimatedCard
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.RadiantGoldButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.screens.applyAuthSession
import com.iskcon.bhagavaddarshan.ui.screens.friendlyError
import com.iskcon.bhagavaddarshan.ui.theme.CustomTypography
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.GoldLight
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.TextSecondaryDark
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.util.FormValidators
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRegisterScreen(
    app: BhagavadDarshanApp,
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    val api = remember { BdApi() }
    val scope = rememberCoroutineScope()

    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var villageTown by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Chittoor") }
    var pincode by remember { mutableStateOf("517501") }
    var state by remember { mutableStateOf("Andhra Pradesh") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

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
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = EditCream,
                    titleContentColor = UxInk,
                    navigationIconContentColor = UxInk
                )
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(EditCream)
                .padding(padding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !otpSent,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name*") },
                modifier = Modifier.fillMaxWidth(),
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
                label = { Text("Village / Town*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                label = { Text("District") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            OutlinedTextField(
                value = pincode,
                onValueChange = { pincode = it.filter(Char::isDigit).take(6) },
                label = { Text("Pincode*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )

            AnimatedVisibility(visible = otpSent, enter = fadeIn() + expandVertically()) {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                    label = { Text("WhatsApp OTP*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
                )
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (!otpSent) {
                RadiantGoldButton(
                    text = if (loading) "Please wait…" else "Send OTP",
                    onClick = {
                        FormValidators.phone(phone)?.let { error = it; return@RadiantGoldButton }
                        if (name.trim().length < 2) { error = "Enter full name"; return@RadiantGoldButton }
                        if (villageTown.isBlank()) { error = "Village / town is required"; return@RadiantGoldButton }
                        if (pincode.length != 6) { error = "Enter 6-digit pincode"; return@RadiantGoldButton }
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
                        if (otp.length != 6) { error = "Enter 6-digit OTP"; return@RadiantGoldButton }
                        loading = true
                        scope.launch {
                            val result = api.verifyRegister(
                                phone = phone,
                                otp = otp,
                                name = name.trim(),
                                houseNo = houseNo.trim(),
                                street = street.trim(),
                                villageTown = villageTown.trim(),
                                district = district.trim(),
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
                    onClick = { otpSent = false; otp = ""; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Edit details", color = GoldLight) }
            }
            }
            }
        }
        }
    }
}
