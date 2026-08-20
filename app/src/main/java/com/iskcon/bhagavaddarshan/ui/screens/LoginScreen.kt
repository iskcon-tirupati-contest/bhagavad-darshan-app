package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.network.ApiException
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.RadiantGoldButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.theme.CustomTypography
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.TextPrimaryDark
import com.iskcon.bhagavaddarshan.ui.theme.TextSecondaryDark
import com.iskcon.bhagavaddarshan.ui.theme.UxCream
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxGold500
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron
import com.iskcon.bhagavaddarshan.util.FormValidators
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    app: BhagavadDarshanApp,
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit
) {
    val session = app.session
    val api = remember { BdApi() }
    var phone by remember { mutableStateOf(if (session.rememberMe) session.agentPhone else "") }
    var otp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var remember by remember { mutableStateOf(session.rememberMe) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val phoneFieldRequester = remember { BringIntoViewRequester() }
    val otpFieldRequester = remember { BringIntoViewRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UxCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UxSaffron.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = UxSaffron
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Bhagavad Darshan",
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = UxInk
                    )
                    Text(
                        "ISKCON TIRUPATI",
                        fontFamily = Inter,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = UxSaffron
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                coil.compose.AsyncImage(
                    model = com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerCatalog.LOGIN_HERO,
                    contentDescription = "ISKCON Tirupati",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                        .clip(CircleShape)
                        .background(UxSaffron)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Monthly Spiritual Wisdom",
                        color = Color.White,
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Hare Krishna",
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = UxInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Please login to access your magazine subscription",
                fontFamily = Inter,
                fontSize = 15.sp,
                color = EditMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, UxGold200)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Mobile Number", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = UxInk)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it.filter(Char::isDigit).take(10)
                            error = null
                        },
                        placeholder = { Text("10-digit number") },
                        prefix = { Text("+91  ", fontWeight = FontWeight.Bold, color = UxInk) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(phoneFieldRequester)
                            .onFocusChanged { state ->
                                if (state.isFocused) {
                                    scope.launch { phoneFieldRequester.bringIntoView() }
                                }
                            },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = customerFieldColors(),
                        enabled = !otpSent
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0FDF4))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF16A34A))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "OTP will be sent to your WhatsApp for a faster and secure login.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF166534)
                        )
                    }

                    AnimatedVisibility(visible = otpSent, enter = fadeIn() + expandVertically()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = otp,
                                onValueChange = {
                                    otp = it.filter(Char::isDigit).take(6)
                                    error = null
                                },
                                label = { Text("OTP") },
                                leadingIcon = { Icon(Icons.Default.Sms, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(otpFieldRequester)
                                    .onFocusChanged { state ->
                                        if (state.isFocused) {
                                            scope.launch { otpFieldRequester.bringIntoView() }
                                        }
                                    },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors()
                            )
                            OutlinedButton(
                                onClick = {
                                    otp = clipboardManager.getText()?.text.orEmpty()
                                        .filter(Char::isDigit).take(6)
                                    error = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Paste OTP", color = UxSaffron) }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = remember,
                            onCheckedChange = { remember = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = UxSaffron,
                                uncheckedColor = TextSecondaryDark,
                                checkmarkColor = Color.White
                            )
                        )
                        Text("Remember me", style = CustomTypography.bodyLarge, color = TextPrimaryDark)
                    }

                    AnimatedVisibility(visible = error != null, enter = fadeIn() + expandVertically()) {
                        Text(
                            error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (!otpSent) {
                        RadiantGoldButton(
                            text = if (loading) "Please wait…" else "Send OTP",
                            onClick = {
                                val phoneErr = FormValidators.phone(phone)
                                if (phoneErr != null) {
                                    error = phoneErr
                                    return@RadiantGoldButton
                                }
                                loading = true
                                scope.launch {
                                    val result = api.sendOtp(phone, "login")
                                    loading = false
                                    result.onSuccess {
                                        otpSent = true
                                        error = null
                                    }.onFailure {
                                        error = friendlyError(it)
                                    }
                                }
                            },
                            enabled = !loading
                        )
                    } else {
                        RadiantGoldButton(
                            text = if (loading) "Please wait…" else "Verify & Login",
                            onClick = {
                                if (otp.length != 6) {
                                    error = "Enter the 6-digit OTP"
                                    return@RadiantGoldButton
                                }
                                loading = true
                                scope.launch {
                                    val result = api.verifyLogin(phone, otp)
                                    loading = false
                                    result.onSuccess { json ->
                                        applyAuthSession(session, json, remember)
                                        onLoggedIn()
                                    }.onFailure {
                                        error = friendlyError(it)
                                    }
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
                        ) { Text("Change number", color = UxSaffron) }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("New devotee? Create Account", color = UxSaffron, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(UxGold200)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "ISKCON TIRUPATI DEVOTIONAL TRUST",
                fontFamily = Inter,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = UxGold500,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun applyAuthSession(
    session: com.iskcon.bhagavaddarshan.data.SessionManager,
    json: JSONObject,
    remember: Boolean
) {
    val role = json.optString("role")
    val token = json.optString("token")
    if (role == "customer") {
        val customer = json.optJSONObject("customer") ?: JSONObject()
        val sub = json.optJSONObject("subscription")
        session.loginFromApi(
            role = "customer",
            token = token,
            name = customer.optString("name"),
            phone = customer.optString("phone"),
            customerId = customer.optLong("id"),
            subscriptionId = sub?.optLong("id") ?: 0L,
            agentId = 0L,
            remember = remember
        )
    } else {
        val agent = json.optJSONObject("agent") ?: JSONObject()
        session.loginFromApi(
            role = role,
            token = token,
            name = agent.optString("name"),
            phone = agent.optString("phone"),
            customerId = 0L,
            subscriptionId = 0L,
            agentId = agent.optLong("id"),
            remember = remember
        )
    }
}

fun friendlyError(t: Throwable): String {
    if (t is ApiException && t.statusCode in 500..599) {
        return "Server is temporarily unavailable. Please try again."
    }
    val raw = when (t) {
        is ApiException -> t.message
        else -> t.message
    }.orEmpty()
    val lower = raw.lowercase()
    if (
        lower.contains("failed to connect") ||
        lower.contains("timeout") ||
        lower.contains("timed out") ||
        lower.contains("unable to resolve host") ||
        lower.contains("network is unreachable")
    ) {
        return "Cannot reach the server. Check internet and try again."
    }
    if (
        lower.contains("<html") ||
        lower.contains("bad gateway") ||
        lower.contains("502") ||
        lower.contains("503")
    ) {
        return "Server is temporarily unavailable. Please try again."
    }
    if (raw.contains("BAD_REQUEST_ERROR") || raw.trimStart().startsWith("{")) {
        return "Payment was cancelled or not completed. You can try again."
    }
    return raw.ifBlank { "Something went wrong. Please try again." }
}

@Composable
private fun fieldColors() = customerFieldColors()
