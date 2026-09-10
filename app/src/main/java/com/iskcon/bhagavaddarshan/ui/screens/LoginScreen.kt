package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.R
import com.iskcon.bhagavaddarshan.network.ApiException
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.EmergedButton
import com.iskcon.bhagavaddarshan.ui.components.OtpSixBoxes
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.theme.CormorantFace
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditPopularBg
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.SacredGold
import com.iskcon.bhagavaddarshan.ui.theme.SacredGoldLight
import com.iskcon.bhagavaddarshan.ui.theme.TextPrimaryDark
import com.iskcon.bhagavaddarshan.ui.theme.TextSecondaryDark
import com.iskcon.bhagavaddarshan.ui.theme.UxCream
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron
import com.iskcon.bhagavaddarshan.util.FormValidators
import kotlinx.coroutines.launch
import org.json.JSONObject

private val LoginHeadingBrown = Color(0xFF5A170E)
private val LoginGoldDeep = Color(0xFFC4890A)
private val LoginFormIvory = Color(0xFFFFFCF7)
private val LoginGoldGradient = listOf(SacredGold, LoginGoldDeep)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    app: BhagavadDarshanApp,
    onLoggedIn: (needsProfileComplete: Boolean) -> Unit,
    onRegister: () -> Unit
) {
    val session = app.session
    val api = remember { BdApi() }
    var phone by remember { mutableStateOf(if (session.rememberMe) session.agentPhone else "") }
    var otp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(session.rememberMe) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val phoneFieldRequester = remember { BringIntoViewRequester() }
    val otpFieldRequester = remember { BringIntoViewRequester() }
    val otpFocusRequesters = remember { List(6) { FocusRequester() } }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val loginTextScale = (screenWidthDp / 360f).coerceIn(0.88f, 1.05f)
    val headingFontSize = (22 * loginTextScale).sp
    val headingLineHeight = (26 * loginTextScale).sp
    val subtitleFontSize = (14 * loginTextScale).sp
    val subtitleLineHeight = (20 * loginTextScale).sp

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
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cap the whole login experience so tablets don't stretch a phone layout.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val density = LocalDensity.current
                val headingBlockHeight = with(density) { headingLineHeight.toDp() } + 4.dp
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    // Banner height = old 3:2 size + space down to where "Please login" starts
                    // (Hare Krishna line + spacer).
                    val bannerHeight = maxWidth * 2f / 3f + headingBlockHeight
                    coil.compose.AsyncImage(
                        model = com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerCatalog.LOGIN_TOP_SECTION,
                        contentDescription = "Bhagavad Darshan",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bannerHeight)
                            .background(UxCream),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                    Text(
                        "HDG A.C.Bhakthivedanta Srila Swami Prabhupada",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (11 * loginTextScale).sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .padding(horizontal = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ceremonial bridge between shining banner and the form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        SacredGoldLight.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(top = 2.dp, bottom = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Hare Krishna",
                            fontFamily = CormorantFace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = headingFontSize,
                            lineHeight = headingLineHeight,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.4.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Image(
                            painter = painterResource(R.drawable.lotus_divider),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 2.dp, bottom = 4.dp)
                                .width(132.dp)
                                .height(14.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            "Please login to access your magazine\nsubscription",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Normal,
                            fontSize = subtitleFontSize,
                            lineHeight = subtitleLineHeight,
                            color = EditMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.88f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    val formShape = RoundedCornerShape(22.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 16.dp,
                                shape = formShape,
                                spotColor = SacredGold.copy(alpha = 0.42f),
                                ambientColor = SacredGold.copy(alpha = 0.22f)
                            )
                            .border(
                                width = 1.6.dp,
                                color = SacredGold,
                                shape = formShape
                            )
                            .clip(formShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(LoginFormIvory, EditPopularBg.copy(alpha = 0.55f))
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Mobile Number",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = LoginHeadingBrown
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = {
                                    phone = it.filter(Char::isDigit).take(10)
                                    error = null
                                },
                                placeholder = {
                                    Text(
                                        "10-digit number",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                },
                                prefix = {
                                    Text(
                                        "+91  ",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = LoginHeadingBrown
                                    )
                                },
                                textStyle = TextStyle(
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = LoginHeadingBrown
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(phoneFieldRequester)
                                    .onFocusChanged { state ->
                                        if (state.isFocused) scope.launch { phoneFieldRequester.bringIntoView() }
                                    },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = customerFieldColors(),
                                enabled = !otpSent
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!otpSent) {
                                    Image(
                                        painter = painterResource(R.drawable.whatsapp_icon),
                                        contentDescription = "WhatsApp",
                                        modifier = Modifier.size(22.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "OTP will be sent to your WhatsApp",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = EditMuted
                                    )
                                } else {
                                    Text(
                                        "OTP sent successfully",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = UxSaffron,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = UxSaffron,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(visible = otpSent, enter = fadeIn() + expandVertically()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(otpFieldRequester),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
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
                                            otpFieldRequester.bringIntoView()
                                            otpFocusRequesters.firstOrNull()?.requestFocus()
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { rememberMe = !rememberMe }
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SacredGold,
                                        uncheckedColor = TextSecondaryDark,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Text(
                                    "Remember me",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = TextPrimaryDark
                                )
                            }

                            AnimatedVisibility(visible = error != null, enter = fadeIn() + expandVertically()) {
                                Text(
                                    error.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = Montserrat,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (!otpSent) {
                                EmergedButton(
                                    text = if (loading) "Please wait…" else "Send OTP  →",
                                    onClick = {
                                        val phoneErr = FormValidators.phone(phone)
                                        if (phoneErr != null) {
                                            error = phoneErr
                                            return@EmergedButton
                                        }
                                        loading = true
                                        scope.launch {
                                            val result = api.sendOtp(phone, "login")
                                            loading = false
                                            result.onSuccess {
                                                otpSent = true
                                                error = null
                                            }.onFailure { error = friendlyError(it) }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !loading,
                                    gradient = LoginGoldGradient,
                                    textColor = Color.White
                                )
                            } else {
                                EmergedButton(
                                    text = if (loading) "Please wait…" else "Verify & Login  →",
                                    onClick = {
                                        if (otp.length != 6) {
                                            error = "Enter the 6-digit OTP"
                                            return@EmergedButton
                                        }
                                        loading = true
                                        scope.launch {
                                            val result = api.verifyLogin(phone, otp)
                                            loading = false
                                            result.onSuccess { json ->
                                                applyAuthSession(session, json, rememberMe)
                                                val needsComplete =
                                                    json.optString("role") == "customer" &&
                                                        !json.optBoolean("profileComplete", true)
                                                onLoggedIn(needsComplete)
                                            }.onFailure { error = friendlyError(it) }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !loading,
                                    gradient = LoginGoldGradient,
                                    textColor = Color.White
                                )
                                OutlinedButton(
                                    onClick = {
                                        otpSent = false
                                        otp = ""
                                        error = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Change number", fontFamily = Montserrat, color = UxSaffron)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "New devotee? ",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = EditMuted
                        )
                        Text(
                            "Create Account",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = UxSaffron,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onRegister)
                                .padding(horizontal = 2.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        "kṛṣṇa—sūrya-sama; māyā haya andhakāra",
                        fontFamily = CormorantFace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (13 * loginTextScale).sp,
                        color = LoginHeadingBrown,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Wherever Krishna is present, there cannot be any darkness or ignorance",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = (12 * loginTextScale).sp,
                        color = EditMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = (16 * loginTextScale).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
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
